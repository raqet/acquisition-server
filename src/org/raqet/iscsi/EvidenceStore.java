/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

import static com.javaforge.reifier.check.ArgumentChecker.argIndex;
import static com.javaforge.reifier.check.ArgumentChecker.argNotNegative;
import static com.javaforge.reifier.check.ArgumentChecker.argNotNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

// Evidence file of blocks
public class EvidenceStore implements Closeable {
    private static final Logger LOG = Logger.getLogger(EvidenceStore.class);
    private static final int BLOCKS_TO_CACHE = 256;

    private final EvidenceStoreFactory _evidenceStoreFactory;

    private final RandomAccessFile _evidenceFile;
    private final RandomAccessFile _bitmapFile;
    private final long _imageLength;
    private final int _blockSize;
    private final int _blockCount;
    private final BitSet _bitmap;
    private final Map<Integer, Block> _cachedBlocks;

    /** The path to the storage file. It is used as hashmap key to open it only once. */
    private final String _evidenceFilePath;
    /** This reference counter makes sure the files are closed when the last user of EvidenceStore closes its reference. */
    private int _refCount;

    protected EvidenceStore(final EvidenceStoreFactory evidenceStoreFactory, final File evidenceFile, final long length, final int blockSize) throws IOException {
        _evidenceStoreFactory = argNotNull("evidenceStoreFactory", evidenceStoreFactory);
        argNotNull("evidenceFile", evidenceFile);
        argNotNegative("length", length);
        _refCount = 0;
        _blockSize = argNotNegative("blockSize", blockSize);

        _evidenceFilePath = evidenceFile.getPath();
        _evidenceFile = new RandomAccessFile(evidenceFile, "rw");
        _bitmapFile = new RandomAccessFile(new File(evidenceFile.getParentFile(), evidenceFile.getName() + ".bitmap"), "rw");
        _blockCount = (int) ((length + blockSize - 1) / blockSize);
        _imageLength = length;

        // Extend the evidence file if necessary, but never truncate (in case of errors)
        if (_evidenceFile.length() < length) {
            LOG.info("Extending evidence file that does not (yet) exist or is truncated: " + evidenceFile);

            _evidenceFile.setLength(length);
        }

        final byte[] bitmap = new byte[(_blockCount + 7) / 8];

        // If the bitmap is incomplete, extend the file with 0's
        if (_bitmapFile.length() < bitmap.length) {
            final int originalBitmapFileLength = (int) _bitmapFile.length();
            _bitmapFile.seek(originalBitmapFileLength);
            _bitmapFile.write(bitmap, originalBitmapFileLength, (bitmap.length - originalBitmapFileLength));
        }

        _bitmapFile.seek(0L);
        _bitmapFile.readFully(bitmap);
        _bitmap = BitSet.valueOf(bitmap);

        _cachedBlocks = new LinkedHashMap<>(BLOCKS_TO_CACHE, 0.5f, true);
    }

    @Override
    public void close() throws IOException {
        _evidenceStoreFactory.closeEvidenceStore(this);
    }

    /**Save all bitmap data that is stored in memory
     * Called from the evideceStoreFactory.
     */
    protected void synctodisk() throws IOException {
        // Flush bitmap to disk
        _bitmapFile.seek(0L);
        _bitmapFile.write(_bitmap.toByteArray());
    }

    /** Close the file handles.
     * Called from the evideceStoreFactory when releasing the last reference.
     */
    protected void doclose() throws IOException {
        // TODO: Check IOException(s) that may occur ...
        _evidenceFile.close();
        _bitmapFile.close();
    }

    public boolean containsBlock(final int blockNumber) {
        return _bitmap.get(blockNumber);
    }

    public synchronized Block retrieveBlock(final int blockNumber) throws IOException {
        argIndex("blockNumber", blockNumber, _blockCount);

        if (!containsBlock(blockNumber)) {
            throw new IllegalArgumentException("block " + blockNumber + " is not available");
        }

        final Block cachedBlock = _cachedBlocks.get(blockNumber);
        if (cachedBlock != null) {
            return cachedBlock;
        }

        // TODO: (see RAQET-60) the last block may be smaller than blockSize

        seekToBlock(blockNumber);

        final byte[] buffer = new byte[_blockSize];
        //At end of evidence file, read the expected size
        if ((blockNumber + 1) * _blockSize > _imageLength) {
            final int maxlen = (int) (((blockNumber + 1) * _blockSize) - _imageLength);
            _evidenceFile.readFully(buffer, 0, maxlen);
        }
        else {
            _evidenceFile.readFully(buffer);
        }

        if (_cachedBlocks.size() == BLOCKS_TO_CACHE) {
            evictCachedBlock();
        }

        final Block block = new Block(buffer);
        _cachedBlocks.put(blockNumber, block);

        return block;
    }

    public synchronized void storeBlock(final int blockNumber, final Block block) throws IOException {
        // TODO: (see RAQET-57) write blocks on a separate thread

        seekToBlock(blockNumber);

        if (_cachedBlocks.size() == BLOCKS_TO_CACHE) {
            evictCachedBlock();
        }

        _cachedBlocks.put(blockNumber, block);
        //At end of evidence file, do not extend but keep same length
        if ((blockNumber + 1) * _blockSize > _imageLength) {
            final int maxlen = (int) (((blockNumber + 1) * _blockSize) - _imageLength);
            _evidenceFile.write(block.getData(), 0, maxlen);
        } else {
            _evidenceFile.write(block.getData());
        }
        _bitmap.set(blockNumber);
    }

    private void seekToBlock(final int blockNumber) throws IOException {
        _evidenceFile.seek((long) blockNumber * _blockSize);
    }

    private void evictCachedBlock() {
        if (!_cachedBlocks.isEmpty()) {
            _cachedBlocks.remove(_cachedBlocks.keySet().iterator().next());
        }
    }

    /** For multiple access from several CIFS clients, the evicence store will be shared
     * This requires the manual refcounting of objects to close it.
     * These two functions should only be called from the EvidenceStoreFactory.
     */
    public void incRefCount() {
        _refCount++;
        LOG.info("incRefCount of " + getPath() + " " + _refCount);
    }

    public int decRefCount() {
        _refCount--;
        LOG.info("decRefCount of " + getPath() + " " + _refCount);
        return _refCount;
    }

    public String getPath() {
        return _evidenceFilePath;
    }

}