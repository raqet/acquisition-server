/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

import static org.raqet.iscsi.SocketUtils.poll;

import static com.javaforge.reifier.check.ArgumentChecker.argNotNegative;

import static nl.minvenj.nfi.raqet.jna.libiscsi.IScsiLibrary.LIBISCSI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.sun.jna.Pointer;

import nl.minvenj.nfi.raqet.jna.libc.Pollfd;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiCommandCallback;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiContextByReference;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiReportLunsList;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiTask;

public final class IScsiConnection extends IScsiObject implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(IScsiConnection.class);
    /** Default timeout in milliseconds. */
    private static final int DEFAULT_TIMEOUT = 1000;

    IScsiConnection(final ScsiContextByReference iScsi) {
        super(iScsi);
    }

    @Override
    public void close() throws IScsiException {
        final int result = LIBISCSI.iscsi_disconnect(_iScsi);
        LOG.debug(String.format("iscsi_disconnect(%s); result=%d", _iScsi, result));

        checkResult(result);
    }

    public void login() throws IScsiException {
        final int result = LIBISCSI.iscsi_login_sync(_iScsi);
        LOG.debug(String.format("iscsi_login_sync(%s); result=%d", _iScsi, result));

        checkResult(result);
    }

    public void logout() throws IScsiException {
        final int result = LIBISCSI.iscsi_logout_sync(_iScsi);
        LOG.debug(String.format("iscsi_logout_sync(%s); result=%d", _iScsi, result));

        checkResult(result);
    }

    public Map<String, List<String>> discovery() throws IScsiException {
        final Map<String, List<String>> addresses = new HashMap<>();

        final AtomicBoolean finished = new AtomicBoolean();
        final ScsiCommandCallback callback = new IScsiDiscoveryCallback(addresses, finished);

        final int result = LIBISCSI.iscsi_discovery_async(_iScsi, callback, null);
        LOG.debug(String.format("iscsi_discovery_async(%s, %s, null); result=%d", _iScsi, callback, result));

        checkResult(result);

        while (!finished.get()) {
            sync();
        }

        return addresses;
    }

    public List<IScsiLogicalUnit> reportLuns() throws IScsiException {
        ScsiTask task = LIBISCSI.iscsi_reportluns_sync(_iScsi, 0, 16);
        LOG.debug(String.format("iscsi_reportluns_sync(%s, 0, 16); result=%s", _iScsi, task));

        checkPointer(task);

        final int fullReportSize = LIBISCSI.scsi_datain_getfullsize(task);
        if (fullReportSize > task.datain.size) {
            LIBISCSI.scsi_free_scsi_task(task);

            // We need more data for the full list
            task = LIBISCSI.iscsi_reportluns_sync(_iScsi, 0, fullReportSize);
            LOG.debug(String.format("iscsi_reportluns_sync(%s, 0, %d); result=%s", _iScsi, fullReportSize, task));

            checkPointer(task);
        }

        try {
            final Pointer pList = datainUnmarshall(task);
            final ScsiReportLunsList list = new ScsiReportLunsList(pList);

            final List<IScsiLogicalUnit> luns = new ArrayList<>();
            for (int i = 0; i < list.num; i++) {
                final int lun = (list.luns[i] & 0xFFFF);
                luns.add(new IScsiLogicalUnit(_iScsi, this, lun));
            }
            return luns;
        }
        finally {
            LIBISCSI.scsi_free_scsi_task(task);
        }
    }

    public void sync() throws IScsiException {
        sync(DEFAULT_TIMEOUT);
    }

    public void sync(final int timeout) throws IScsiException {
        argNotNegative("timeout", timeout);

        final Pollfd pfd = new Pollfd();
        pfd.fd = LIBISCSI.iscsi_get_fd(_iScsi);
        LOG.debug(String.format("iscsi_get_fd(%s); result=%d", _iScsi, pfd.fd));

        pfd.events = (short) LIBISCSI.iscsi_which_events(_iScsi);
        LOG.debug(String.format("iscsi_which_events(%s); result=%s", _iScsi, pfd.events));

        if (poll(pfd, 1, timeout) < 0) {
            // FIXME: It should actually set the iscsi error and return
            throw new IScsiException("Poll failed");
        }

        final int result = LIBISCSI.iscsi_service(_iScsi, pfd.revents);
        LOG.debug(String.format("iscsi_service(%s, %d); result=%s", _iScsi, pfd.revents, result));

        if (result < 0) {
            // FIXME: It should actually set the iscsi error and return
            throw new IScsiException("iscsi_service failed with : " + LIBISCSI.iscsi_get_error(_iScsi));
        }
    }
}