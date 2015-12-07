/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

import static nl.minvenj.nfi.raqet.jna.libiscsi.IScsiLibrary.LIBISCSI;

import com.sun.jna.Pointer;

import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiCommandCallback;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiContextByReference;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiStatus;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiTask;

final class IScsiRead10Callback implements ScsiCommandCallback, IScsiReadFuture {
    private final IScsiConnection _connection;
    private final byte[] _buffer;
    private final int _offset;
    private boolean _finished;
    private IScsiException _exception;
    private int _bytesRead;

    IScsiRead10Callback(final IScsiConnection connection, final byte[] buffer, final int offset) {
        _connection = connection;
        _buffer = buffer;
        _offset = offset;
    }

    @Override
    public void callback(final ScsiContextByReference iScsi, final int status, final Pointer commandData, final Pointer privateData) {
        final ScsiTask task = cast(commandData);
        try {
            if (task.status != ScsiStatus.SCSI_STATUS_GOOD) {
                suppressException(new IScsiException("READ10 command failed", _connection.getError()));
                return;
            }

            _bytesRead = task.datain.size;

            if (_bytesRead > 0) {
                task.datain.data.read(0L, _buffer, _offset, _bytesRead);
            }
        }
        finally {
            LIBISCSI.scsi_free_scsi_task(task);

            _finished = true;
        }
    }

    private static ScsiTask cast(final Pointer commandData) {
        return new ScsiTask(commandData);
    }

    private void suppressException(final IScsiException e) {
        if (_exception == null) {
            _exception = e;
        }
        else {
            _exception.addSuppressed(e);
        }
    }

    @Override
    public boolean isDone() {
        return _finished;
    }

    @Override
    public int complete() throws IScsiException {
        rethrowSuppressedExceptions();

        while (!_finished) {
            _connection.sync();
        }
        return _bytesRead;
    }

    private void rethrowSuppressedExceptions() throws IScsiException {
        final IScsiException e = _exception;
        _exception = null;

        if (e != null) {
            throw e;
        }
    }
}