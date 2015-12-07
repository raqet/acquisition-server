/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

import static nl.minvenj.nfi.raqet.jna.libiscsi.IScsiLibrary.LIBISCSI;

import org.apache.log4j.Logger;

import com.sun.jna.Pointer;

import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiContextByReference;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiTask;

abstract class IScsiObject implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(IScsiObject.class);

    protected final ScsiContextByReference _iScsi;

    IScsiObject(final ScsiContextByReference iScsi) {
        _iScsi = iScsi;
    }

    @Override
    public abstract void close() throws IScsiException;

    protected Pointer datainUnmarshall(final ScsiTask task) throws IScsiException {
        final Pointer p = LIBISCSI.scsi_datain_unmarshall(task);
        LOG.debug(String.format("scsi_datain_unmarshall(%s); result=%s", task, p));

        checkPointer(p, "Failed to unmarshall datain blob");
        return p;
    }

    protected void checkResult(final int result) throws IScsiException {
        if (result < 0) {
            throw new IScsiException(getError());
        }
    }

    protected void checkResult(final int result, final String message) throws IScsiException {
        if (result < 0) {
            throw new IScsiException(message, getError());
        }
    }

    protected void checkPointer(final Object p) throws IScsiException {
        if (p == null) {
            throw new IScsiException(getError());
        }
    }

    protected void checkPointer(final Object p, final String message) throws IScsiException {
        if (p == null) {
            throw new IScsiException(message, getError());
        }
    }

    protected String getError() {
        return LIBISCSI.iscsi_get_error(_iScsi);
    }
}