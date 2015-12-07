/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.remote;

import java.io.Closeable;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.raqet.iscsi.IScsiContext;
import org.raqet.iscsi.IScsiException;


final class RemoteConnectionCloseable implements Closeable {
    private static final Logger LOG = Logger.getLogger(RemoteConnectionCloseable.class);

    private final IScsiContext _context;
    private final String _targetName;
    private final int _lun;

    RemoteConnectionCloseable(final IScsiContext context, final String targetName, final int lun) {
        _context = context;
        _targetName = targetName;
        _lun = lun;
    }

    @Override
    public void close() throws IOException {
        LOG.info(String.format("Closing connection to iSCSI target %s, Lun:%04d", _targetName, _lun));

        try {
            _context.close();
        }
        catch (final IScsiException e) {
            throw new IOException(e);
        }
    }
}