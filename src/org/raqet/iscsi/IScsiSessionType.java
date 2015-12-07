/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

public enum IScsiSessionType {
    DISCOVERY(1),
    NORMAL(2);

    private final int _intValue;

    IScsiSessionType(final int intValue) {
        _intValue = intValue;
    }

    int getIntValue() {
        return _intValue;
    }
}