/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

import static com.javaforge.reifier.check.ArgumentChecker.argIndexRange;
import static com.javaforge.reifier.check.ArgumentChecker.argNotNegative;
import static com.javaforge.reifier.check.ArgumentChecker.argNotNull;

public final class Block {
    private final byte[] _data;

    public Block(final byte[] data) {
        _data = argNotNull("data", data);
    }

    /**
     * Returns the data of this {@link Block}.
     * <p>
     * <strong>Note:</strong> Do no modify the array returned by this method,
     * since this is the underlying array of this {@link Block}.
     *
     * @return the array containing the data
     */
    public byte[] getData() {
        return _data;
    }

    /**
     * Reads (copies) data from this block into {@code buffer}.
     *
     * @param offsetInBlock the offset in this block of the data to copy
     * @param buffer the buffer to copy the data to
     * @param offset the offset in {@code buffer} to copy the data to
     * @param count the number of bytes to copy
     * @return the size of the block in bytes
     *
     * @throws IllegalArgumentException if the given arguments are outside the valid range
     *      of this block and/or the given {@code buffer}
     */
    public int read(final int offsetInBlock, final byte[] buffer, final int offset, final int count) {
        argNotNull("buffer", buffer);
        argIndexRange("offset", offset, "count", count, buffer.length);
        argNotNegative("offsetInBlock", offsetInBlock);

        final int bytesRead = Math.min(count, (_data.length - offsetInBlock));
        if (bytesRead <= 0) {
            return 0; // block doesn't contain data after 'offsetInBlock'
        }

        System.arraycopy(_data, offsetInBlock, buffer, offset, bytesRead);
        return bytesRead;
    }
}