/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libiscsi;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public final class ScsiReportLunsList extends Structure {
    public int num;
    public short[] luns;

    public ScsiReportLunsList(final Pointer pointer) {
        super(pointer);

        luns = new short[1];
        luns = new short[Math.max(1, (Integer) readField("num"))];

        read();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
        return Arrays.asList("num", "luns");
    }
}