/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.remote;

import static com.javaforge.reifier.check.ArgumentChecker.argNotNull;
import static com.javaforge.reifier.check.ArgumentChecker.argNotNullAndNotEmpty;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.raqet.iscsi.BlockReader;
import org.raqet.iscsi.EvidenceStore;
import org.raqet.iscsi.EvidenceStoreFactory;
import org.raqet.iscsi.IScsiBlockReader;
import org.raqet.iscsi.IScsiConnection;
import org.raqet.iscsi.IScsiContext;
import org.raqet.iscsi.IScsiException;
import org.raqet.iscsi.IScsiInputData;
import org.raqet.iscsi.IScsiLogicalUnit;
import org.raqet.iscsi.IScsiLogicalUnitCapacity;
import org.raqet.iscsi.IScsiPeripheralDeviceType;
import org.raqet.iscsi.IScsiSessionType;
import org.raqet.iscsi.TargetAddress;

import nl.minvenj.nfi.streams.inputdata.InputData;

public final class RemoteDeviceManager {
    private static final Logger LOG = Logger.getLogger(RemoteDeviceManager.class);
    private static final int READ_BLOCK_SIZE = 65536;

    private final String _initiatorName;

    private final EvidenceStoreFactory _evidenceStoreFactory;
    /** Maps the portalUrl to discovered  RemoteDeviceInfo.
     * For now all discovered RemoteDevices are remembered here.
     * TODO: Move upto caller RaqetControl?
     */
    private final Map<String, List<RemoteDeviceInfo>> _portalMap = new HashMap<>();

    private final File _evidenceDirectory;

    public RemoteDeviceManager(final String initiatorName, final File evidenceDirectory) throws IScsiException {
        _initiatorName = argNotNullAndNotEmpty("initiatorName", initiatorName);
        _evidenceDirectory = argNotNull("evidenceDirectory", evidenceDirectory);
        _evidenceStoreFactory = new EvidenceStoreFactory();
        _evidenceDirectory.mkdirs();
    }

    public List<RemoteDeviceInfo> listTargets(final TargetAddress targetAddress) throws IScsiException {

        final List<RemoteDeviceInfo> deviceList = new ArrayList<>();

        // Retrieve the iSCSI targets and corresponding portals (servers that can be used to access the target)
        final Map<String, List<String>> addressMap;
        try (IScsiContext context = IScsiContext.create(_initiatorName)) {
            context.setSessionType(IScsiSessionType.DISCOVERY);
            context.setInitiatorUsernamePwd(targetAddress.getUsername(), targetAddress.getPassword());

            try (IScsiConnection connection = context.connect(targetAddress.getPortalUrl())) {
                connection.login();

                addressMap = connection.discovery();

                connection.logout();
            }
        }

        // Retrieve additional device information on each target
        for (final Map.Entry<String, List<String>> address : addressMap.entrySet()) {
            try (IScsiContext context = IScsiContext.create(_initiatorName)) {
                final String targetName = address.getKey();
                context.setSessionType(IScsiSessionType.NORMAL);
                context.setTargetName(targetName);
                context.setInitiatorUsernamePwd(targetAddress.getUsername(), targetAddress.getPassword());

                // 'portals' is the list of portals (servers) where the iSCSI target is available
                final List<String> portals = address.getValue();
                final List<RemoteDeviceInfo> devices = queryRemoteDevices(context, targetName, targetAddress, portals);
                deviceList.addAll(devices);
            }
        }

        _portalMap.put(targetAddress.getPortalUrl(), deviceList);
        return deviceList;
    }

    @SuppressWarnings("unused")
    private RemoteDeviceInfo queryRemoteDevice(final IScsiContext context, final String targetName, final TargetAddress targetAddress, final List<String> portals, final int lun) {
        for (final String portalUrl : portals) {

            try {
                final IScsiLogicalUnit logicalUnit = context.fullConnectLogicalUnit(portalUrl, lun);
                // Next call will ensure that the device is as ready as it can be
                logicalUnit.isReady(); // Ignored, remote device will be reported either way

                final IScsiPeripheralDeviceType deviceType = logicalUnit.inquireDeviceType();
                final IScsiLogicalUnitCapacity capacity = logicalUnit.readCapacity();
                return new RemoteDeviceInfo(portalUrl, targetName, logicalUnit.getLun(), deviceType, capacity, targetAddress);
            }
            catch (final IScsiException e) {
                LOG.warn("Portal unavailable: " + portalUrl, e);
            }
        }

        // Failed to determine remote device
        LOG.warn("Remote device unavailable: " + targetName);
        return null;
    }

    private List<RemoteDeviceInfo> queryRemoteDevices(final IScsiContext context, final String targetName, final TargetAddress targetAddress, final List<String> portals) {
        final List<RemoteDeviceInfo> remoteDeviceInfoList = new ArrayList<RemoteDeviceInfo>();
        for (final String portalUrl : portals) {
            try (final IScsiConnection connection = context.fullConnect(portalUrl, -1)) {
                // Next call will ensure that the device is as ready as it can be
                final List<IScsiLogicalUnit> luns = connection.reportLuns();
                for (final IScsiLogicalUnit logicalUnit : luns) {

                    final IScsiPeripheralDeviceType deviceType = logicalUnit.inquireDeviceType();
                    final IScsiLogicalUnitCapacity capacity = logicalUnit.readCapacity();
                    remoteDeviceInfoList.add(
                        new RemoteDeviceInfo(portalUrl, targetName, logicalUnit.getLun(), deviceType, capacity, targetAddress));
                }
            }
            catch (final IScsiException e) {
                LOG.warn("Portal unavailable: " + targetName, e);
            }
        }
        return remoteDeviceInfoList;
    }


    public InputData openDevice(final RemoteDeviceInfo remoteDeviceInfo) throws IOException {
        final String portal = remoteDeviceInfo.getPortal();
        final String targetName = remoteDeviceInfo.getTargetName();
        final int lun = remoteDeviceInfo.getLun();
        final TargetAddress targetAddress = remoteDeviceInfo.getTargetAddress();

        LOG.info(String.format("Opening iSCSI target %s, Lun:%04d at %s", targetName, lun, portal, targetAddress.getUsername()));

        IScsiContext context = null;
        try {
            context = IScsiContext.create(_initiatorName);
            context.setTimeout(5);
            context.setSessionType(IScsiSessionType.NORMAL);
            context.setTargetName(targetName);
            context.setInitiatorUsernamePwd(targetAddress.getUsername(), targetAddress.getPassword());

            final IScsiLogicalUnit logicalUnit = context.fullConnectLogicalUnit(portal, lun);
            if (!logicalUnit.isReady()) {
                throw new IOException(String.format("Logical unit not ready: %s, Lun:%04d at %s", targetName, lun, portal));
            }

            // Use a separate folder for each portal
            final File portalEvidenceDirectory = remoteDeviceInfo.getEvidenceDirectory();
            final String evidenceFilename = remoteDeviceInfo.getEvidenceBaseFileName();
            portalEvidenceDirectory.mkdir();

            final IScsiLogicalUnitCapacity capacity = logicalUnit.readCapacity();
            final long sizeInBytes = (capacity.getLogicalBlockAddress() + 1) * capacity.getBlockSize();

            // Open the iSCSI block reader
            final Closeable connection = new RemoteConnectionCloseable(context, targetName, lun);
            final EvidenceStore evidenceStore = createEvidenceStore(portalEvidenceDirectory, evidenceFilename, sizeInBytes);
            final BlockReader blockReader = new IScsiBlockReader(logicalUnit, capacity, connection, evidenceStore, READ_BLOCK_SIZE);

            return new IScsiInputData(blockReader, sizeInBytes, READ_BLOCK_SIZE);
        }
        catch (final IScsiException e) {
            if (context != null) {
                try {
                    context.close();
                }
                catch (final IScsiException e1) {
                    e.addSuppressed(e1);
                }
            }
            throw new IOException("Failed to open remote device", e);
        }
    }

    public Map<String, List<RemoteDeviceInfo>> getPortalMap() {
        return _portalMap;
    }

    private EvidenceStore createEvidenceStore(final File directory, final String evidenceFilename, final long sizeInBytes) throws IOException {
        final File evidenceFile = new File(directory, evidenceFilename);
        final EvidenceStore evidenceStore = _evidenceStoreFactory.getEvidenceStore(evidenceFile, sizeInBytes, READ_BLOCK_SIZE);
        return evidenceStore;
    }
}