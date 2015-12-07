/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libiscsi;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public final class ScsiInquiryDeviceIdentification extends Structure {
    public int qualifier;
    public int device_type;
    public int pagecode;

    public Pointer designators;

    public ScsiInquiryDeviceIdentification(final Pointer p) {
        super(p);

        read();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
        return Arrays.asList("qualifier", "device_type", "pagecode", "designators");
    }
}