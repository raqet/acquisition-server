/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

/**
 * Thrown on error performing an iSCSI operation.
 */
public class IScsiException extends Exception {
    private static final long serialVersionUID = 4396860369973540753L;

    /**
     * Creates a new {@link IScsiException} with given detail {@code message}.
     * 
     * @param message the detail message
     */
    public IScsiException(final String message) {
        super(message);
    }

    /**
     * Creates a new {@link IScsiException} with given detail {@code message}
     * and the specified libiscsi {@code error}.
     * 
     * @param message the detail message
     * @param error the internal error of libiscsi
     */
    public IScsiException(final String message, final String error) {
        super(message + "; libiscsi error '" + error + '\'');
    }
}