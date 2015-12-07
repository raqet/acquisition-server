/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

public enum IScsiPeripheralDeviceType {
    DIRECT_ACCESS(0x00),
    STORAGE_ARRAY_CONTROLLER(0x0c),
    UNKNOWN(-1);

    private final int _devtype;

    IScsiPeripheralDeviceType(final int devtype) {
        _devtype = devtype;
    }

    int devtype() {
        return _devtype;
    }

    static IScsiPeripheralDeviceType forDevtype(final int devtype) {
        for (final IScsiPeripheralDeviceType deviceType : values()) {
            if (deviceType.devtype() == devtype) {
                return deviceType;
            }
        }
        return null; // FIXME: is it okay to just return 'null' here!?
    }
}