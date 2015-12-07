/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

public final class IScsiLogicalUnitCapacity {
    /**
     * Highest valid LBA (thus 1 for a disk with 2 blocks).
     */
    private final long _logicalBlockAddress;
    private final int _blockSize;

    IScsiLogicalUnitCapacity(final long logicalBlockAddress, final int blockSize) {
        _logicalBlockAddress = logicalBlockAddress;
        _blockSize = blockSize;
    }

    public long getLogicalBlockAddress() {
        return _logicalBlockAddress;
    }

    public int getBlockSize() {
        return _blockSize;
    }
}