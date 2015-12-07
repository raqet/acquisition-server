/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

import static com.javaforge.reifier.check.ArgumentChecker.argIndex;
import static com.javaforge.reifier.check.ArgumentChecker.argNotNegative;
import static com.javaforge.reifier.check.ArgumentChecker.argNotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class IScsiBlockReader implements BlockReader {
    private static final int MAX_REQUESTS_IN_FLIGHT = 64;
    private static final int MAX_READ_AHEAD_BLOCKS = 16; // 1 Gbit/s with 8ms latency | 100 Mbit/s with 30ms latency

    private final IScsiLogicalUnit _logicalUnit;
    private final Closeable _connection;
    private final EvidenceStore _evidenceStore;
    private final Map<Integer, ReadRequest> _requestsInFlight;

    private final int _readBlockSize;
    private final int _iScsiBlockSize;
    /** Number of LBA's: 2 for a disk with 2 sectors. */
    private final long _maxLogicalBlockAddress;
    private final int _iScsiBlocksPerReadRequest;
    private final int _totalBlockCount;

    public IScsiBlockReader(final IScsiLogicalUnit logicalUnit, final IScsiLogicalUnitCapacity capacity, final Closeable connection, final EvidenceStore evidenceStore, final int readBlockSize) {
        _logicalUnit = argNotNull("logicalUnit", logicalUnit);
        _connection = argNotNull("connection", connection);
        _evidenceStore = argNotNull("evidenceStore", evidenceStore);
        _readBlockSize = argNotNegative("readBlockSize", readBlockSize);

        _requestsInFlight = new HashMap<>();
        _iScsiBlockSize = capacity.getBlockSize();
        _maxLogicalBlockAddress = capacity.getLogicalBlockAddress() + 1;
        _iScsiBlocksPerReadRequest = (_readBlockSize / _iScsiBlockSize);

        if ((_iScsiBlocksPerReadRequest * _iScsiBlockSize) != _readBlockSize) {
            throw new IllegalArgumentException(String.format("readBlockSize (%d) is not a multiple of the iSCSI block size (%d)", _readBlockSize, _iScsiBlockSize));
        }

        final long sizeInBytes = (capacity.getLogicalBlockAddress() + 1L) * _iScsiBlockSize;
        _totalBlockCount = (int) ((sizeInBytes + _readBlockSize - 1L) / _readBlockSize);
    }

    @Override
    public void close() throws IOException {
        _connection.close();
        _evidenceStore.close();
    }

    @Override
    public Block retrieveBlock(final int blockNumber) throws IOException {
        argIndex("blockNumber", blockNumber, _totalBlockCount);

        // Request the block if it is not yet in the evidence store
        if (!_evidenceStore.containsBlock(blockNumber)) {
            try {
                if (!hasBlockBeenRequested(blockNumber)) {
                    waitForFreeSlot();

                    requestBlock(blockNumber);
                }

                final int readahead = determineReadAheadBlocks(blockNumber);
                requestBlocksForReadAhead(blockNumber, Math.min((blockNumber + readahead), _totalBlockCount) - 1);

                // Wait for the block request to complete
                while (_requestsInFlight.containsKey(blockNumber)) {
                    sync();

                    completeRequests();
                }
            }
            catch (final IScsiException e) {
                throw new IOException(e);
            }
        }

        // The block should now be available in the evidence store (and most likely the cache)
        return _evidenceStore.retrieveBlock(blockNumber);
    }

    private boolean hasBlockBeenRequested(final int blockNumber) {
        return _requestsInFlight.containsKey(blockNumber) || _evidenceStore.containsBlock(blockNumber);
    }

    private void waitForFreeSlot() throws IScsiException, IOException {
        while (hasMaxRequestsInFlight()) {
            completeRequests();

            if (hasMaxRequestsInFlight()) {
                sync();
            }
        }
    }

    private boolean hasMaxRequestsInFlight() {
        return (_requestsInFlight.size() >= MAX_REQUESTS_IN_FLIGHT);
    }

    private void completeRequests() throws IOException, IScsiException {
        // Uses an iterator to avoid concurrent modification exceptions
        final Iterator<Integer> iter = _requestsInFlight.keySet().iterator();
        while (iter.hasNext()) {
            final int blockNumber = iter.next();
            final ReadRequest request = _requestsInFlight.get(blockNumber);
            if (request.isDone()) {
                iter.remove();
                // TODO: (see RAQET-57) Use a write queue for writing blocks on a separate thread
                _evidenceStore.storeBlock(blockNumber, new Block(request.complete()));
            }
        }
    }

    private void requestBlock(final int blockNumber) throws IScsiException {
        final byte[] buffer = new byte[_readBlockSize];
        final long lba = blockNumber * _iScsiBlocksPerReadRequest;
        final int blockToRead = (int) Math.min(_iScsiBlocksPerReadRequest, (_maxLogicalBlockAddress - lba));

        final IScsiReadFuture future = _logicalUnit.read(lba, blockToRead, _iScsiBlockSize, buffer, 0);
        final ReadRequest request = new ReadRequest(future, buffer);
        _requestsInFlight.put(blockNumber, request);
    }

    private int determineReadAheadBlocks(final int blockNumber) {
        // Double the read-ahead (in blocks) on every sequential read, but stop at MAX_READ_AHEAD_BLOCKS
        // This will ensure that we use all the available bandwidth, yet minimise the penalty of random reads.
        for (int i = 0; i < ((MAX_READ_AHEAD_BLOCKS + 1) / 2); i++) {
            if (!hasBlockBeenRequested(blockNumber + i)) {
                return (i * 2); // Double the number of read-ahead blocks on every sequential read
            }
        }
        return MAX_READ_AHEAD_BLOCKS;
    }

    private void requestBlocksForReadAhead(final int firstBlockNumber, final int lastBlockNumber) throws IScsiException, IOException {
        completeRequests();

        for (int i = firstBlockNumber; i <= lastBlockNumber; i++) {
            if (hasMaxRequestsInFlight()) {
                break;
            }
            if (!hasBlockBeenRequested(i)) {
                requestBlock(i);
            }
        }
    }

    @SuppressWarnings("resource")
    private void sync() throws IScsiException {
        // TODO: (see RAQET-59) Move sync() method so we do not have to create a temporary connection object
        final IScsiConnection connection = new IScsiConnection(_logicalUnit._iScsi);
        connection.sync();
    }
}