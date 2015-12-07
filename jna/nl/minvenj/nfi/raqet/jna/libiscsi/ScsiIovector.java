/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libiscsi;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public final class ScsiIovector extends Structure {
    public Pointer iov;
    public int niov;
    public int nalloc;

    public long reserved_1;
    public int reserved_2;

    public ScsiIovector(final Pointer p) {
        super(p);

        read();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
        return Arrays.asList("iov", "niov", "nalloc", "reserved_1", "reserved_2");
    }
}