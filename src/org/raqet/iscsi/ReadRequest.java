/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

final class ReadRequest {
    private final IScsiReadFuture _future;
    private final byte[] _buffer;

    ReadRequest(final IScsiReadFuture future, final byte[] buffer) {
        _future = future;
        _buffer = buffer;
    }

    public boolean isDone() {
        return _future.isDone();
    }

    public byte[] complete() throws IScsiException {
        _future.complete();
        return _buffer;
    }
}