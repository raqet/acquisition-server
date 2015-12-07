/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.sun.jna.Pointer;

import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiCommandCallback;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiContextByReference;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiDiscoveryAddress;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiTargetPortal;

final class IScsiDiscoveryCallback implements ScsiCommandCallback {
    private static final Logger LOG = Logger.getLogger(IScsiDiscoveryCallback.class);

    private final Map<String, List<String>> _addresses;
    private final AtomicBoolean _finished;

    IScsiDiscoveryCallback(final Map<String, List<String>> addresses, final AtomicBoolean finished) {
        _addresses = addresses;
        _finished = finished;
    }

    @Override
    public void callback(final ScsiContextByReference iscsi, final int status, final Pointer commandData, final Pointer privateData) {
        for (ScsiDiscoveryAddress sda = cast(commandData); sda != null; sda = sda.next) {
            final ArrayList<String> portals = new ArrayList<>();
            for (ScsiTargetPortal stp = sda.portals; stp != null; stp = stp.next) {
                portals.add(stp.portal);
            }

            _addresses.put(sda.target_name, portals);
        }

        _finished.set(true);

        LOG.debug("iscsi_discovery() finished with status " + status);
    }

    private static ScsiDiscoveryAddress cast(final Pointer commandData) {
        return new ScsiDiscoveryAddress(commandData);
    }
}