/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libws2;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public final class WsaPollfd extends Structure {
    public long fd;
    public short events;
    public short revents;

    public WsaPollfd() {
    }

    public WsaPollfd(final Pointer p) {
        super(p);

        read();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
        return Arrays.asList("fd", "events", "revents");
    }
}