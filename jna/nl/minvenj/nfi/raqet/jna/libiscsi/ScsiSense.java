/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libiscsi;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public final class ScsiSense extends Structure {
    public byte error_type;
    public int key;
    public int ascq;

    public ScsiSense(final Pointer p) {
        super(p);

        read();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
        return Arrays.asList("error_type", "key", "ascq");
    }
}