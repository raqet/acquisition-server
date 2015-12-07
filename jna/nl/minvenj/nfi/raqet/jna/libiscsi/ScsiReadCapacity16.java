/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libiscsi;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public final class ScsiReadCapacity16 extends Structure {
    public long returned_lba;
    public int block_length;
    public byte p_type;
    public byte prot_en;
    public byte p_i_exp;
    public byte lbppbe;
    public byte lbpme;
    public byte lbprz;
    public short lalba;

    public ScsiReadCapacity16(final Pointer pointer) {
        super(pointer);

        read();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
        return Arrays.asList("returned_lba", "block_length", "p_type", "prot_en", "p_i_exp", "lbppbe", "lbpme", "lbprz", "lalba");
    }
}