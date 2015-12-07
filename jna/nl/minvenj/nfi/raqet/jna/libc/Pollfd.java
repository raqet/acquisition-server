/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libc;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

// defined in 'sys/poll.h'
public final class Pollfd extends Structure {
    public int fd;
    public short events;
    public short revents;

    public Pollfd() {
    }

    public Pollfd(final Pointer p) {
        super(p);

        read();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
        return Arrays.asList("fd", "events", "revents");
    }
}