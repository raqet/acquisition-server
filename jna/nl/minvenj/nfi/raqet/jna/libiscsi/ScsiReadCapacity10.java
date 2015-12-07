/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libiscsi;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public final class ScsiReadCapacity10 extends Structure {
    public int lba;
    public int block_size;

    public ScsiReadCapacity10(final Pointer pointer) {
        super(pointer);

        read();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
        return Arrays.asList("lba", "block_size");
    }
}