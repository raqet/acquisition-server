/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.smb;

import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFile;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFileInfo;
import org.raqet.iscsi.IScsiLogicalUnitCapacity;
import org.raqet.remote.RemoteDeviceInfo;
import org.raqet.remote.RemoteDeviceManager;


final class RemotePseudoFile extends PseudoFile {
    private final RemoteDeviceManager _remoteDeviceManager;
    private final RemoteDeviceInfo _removeDeviceInfo;

    RemotePseudoFile(final RemoteDeviceManager remoteDeviceManager, final RemoteDeviceInfo remoteDevice) {
        super(generateFilename(remoteDevice));

        _remoteDeviceManager = remoteDeviceManager;
        _removeDeviceInfo = remoteDevice;
    }

    private static String generateFilename(final RemoteDeviceInfo remoteDevice) {
        return "virtualevidence-" + remoteDevice.getEvidenceBaseFileName();
    }

    @Override
    public FileInfo getFileInfo() {
        final FileInfo existingFileInfo = getInfo();
        if (existingFileInfo != null) {
            return existingFileInfo;
        }

        final IScsiLogicalUnitCapacity capacity = _removeDeviceInfo.getCapacity();
        final long blockCount = capacity.getLogicalBlockAddress() + 1;
        final long sizeInBytes = (blockCount * capacity.getBlockSize());

        final FileInfo newFileInfo = new PseudoFileInfo(getFileName(), sizeInBytes, getAttributes());

        newFileInfo.setCreationDateTime(_creationDateTime);
        newFileInfo.setModifyDateTime(_creationDateTime);
        newFileInfo.setChangeDateTime(_creationDateTime);
        newFileInfo.setAllocationSize(blockCount * capacity.getBlockSize());

        setFileInfo(newFileInfo);

        return newFileInfo;
    }

    @Override
    public NetworkFile getFile(final String netPath) {
        final FileInfo fileInfo = getFileInfo();
        fileInfo.setPath(netPath);
        return new RemoteNetworkFile(getFileName(), fileInfo, _remoteDeviceManager, _removeDeviceInfo);
    }
}