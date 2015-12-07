/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

import static com.javaforge.reifier.check.ArgumentChecker.argNotNegative;
import static com.javaforge.reifier.check.ArgumentChecker.argNotNull;

import java.io.IOException;

import nl.minvenj.nfi.streams.inputdata.AbstractInputData;
import nl.minvenj.nfi.streams.inputdata.InputData;
import nl.minvenj.nfi.streams.transformation.Transformation;

public class IScsiInputData extends AbstractInputData implements InputData {
    private final BlockReader _blockReader;
    private final long _size;
    private final int _readBlockSize;
    private long _position;

    public IScsiInputData(final BlockReader blockReader, final long size, final int readBlockSize) {
        _blockReader = argNotNull("blockReader", blockReader);
        _size = argNotNegative("size", size);
        _readBlockSize = argNotNegative("readBlockSize", readBlockSize);
    }

    @Override
    public void close() throws IOException {
        if (isOpen()) {
            try {
                _blockReader.close();
            }
            finally {
                super.close();
            }
        }
    }

    @Override
    public Transformation transformation() {
        return null;
    }

    @Override
    public long getPosition() {
        return _position;
    }

    @Override
    public void seek(final long position) {
        _position = validatePosition(position);
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int count) throws IOException {
        int bytesToRead = validateReadParameters(buffer, offset, count);
        if (bytesToRead <= 0) {
            return bytesToRead;
        }

        final long firstBlock = (_position / _readBlockSize);
        final long lastBlock = (_position + bytesToRead - 1) / _readBlockSize;

        // Copy cached blocks and gather blocks to request
        int totalBytesRead = 0;
        for (long blockNumber = firstBlock; blockNumber <= lastBlock; blockNumber++) {
            final Block block = _blockReader.retrieveBlock((int) blockNumber);
            final int offsetInBlock = (int) (_position - (blockNumber * _readBlockSize));

            final int bytesRead = block.read(offsetInBlock, buffer, (offset + totalBytesRead), bytesToRead);
            _position += bytesRead;

            totalBytesRead += bytesRead;
            bytesToRead -= bytesRead;
        }

        return totalBytesRead;
    }

    @Override
    public long getSize() {
        return _size;
    }
}