/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.remote;

import java.io.File;

import org.raqet.iscsi.IScsiLogicalUnitCapacity;
import org.raqet.iscsi.IScsiPeripheralDeviceType;
import org.raqet.iscsi.TargetAddress;


public final class RemoteDeviceInfo {
    /** The iscsi portal URL
     * TODO: Check whether can factored out to TargetAddress, probably not as it seems
     * a single IP and port can represent multiple portal groups.
     * */
    private final String _portal;
    private final int _lun;
    private final IScsiPeripheralDeviceType _deviceType;
    private final IScsiLogicalUnitCapacity _capacity;
    private final TargetAddress _targetAddress;
    private final String _targetName;
    private File _evidenceDirectory;
    private String _evidenceBaseFileName;

    RemoteDeviceInfo(final String portal, final String targetName, final int lun,
                     final IScsiPeripheralDeviceType deviceType, final IScsiLogicalUnitCapacity capacity,
                     final TargetAddress targetAddress) {
        _portal = portal;
        _targetName = targetName;
        _lun = lun;
        _deviceType = deviceType;
        _capacity = capacity;
        _targetAddress = targetAddress;
    }

    public String getPortal() {
        return _portal;
    }

    public int getLun() {
        return _lun;
    }

    public IScsiPeripheralDeviceType getDeviceType() {
        return _deviceType;
    }

    public IScsiLogicalUnitCapacity getCapacity() {
        return _capacity;
    }

    public TargetAddress getTargetAddress() {
        return _targetAddress;
    }

    public String getTargetName() {
        return _targetName;
    }

    public File getEvidenceDirectory() {
        return _evidenceDirectory;
    }

    public void setEvidenceDirectory(final File evidenceDirectory) {
        _evidenceDirectory = evidenceDirectory;
    }

    public String getEvidenceBaseFileName() {
        return _evidenceBaseFileName;
    }

    public void setEvidenceBaseFileName(final String evidenceBaseFilename) {
        _evidenceBaseFileName = evidenceBaseFilename;
    }

}