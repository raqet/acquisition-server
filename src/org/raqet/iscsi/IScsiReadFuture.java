/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

/**
 * Provides a callback interface for the asynchronous iSCSI read method.
 * <p>
 * This interface is similar to the {@link java.util.concurrent.Future Future}
 * interface available in the JDK.
 *
 * @see IScsiLogicalUnit#read(int, byte[], int, int, int)
 */
public interface IScsiReadFuture {
    /**
     * Returns whether the iSCSI read has completed.
     *
     * @return {@code true} if the read has completed or an error has occurred
     */
    boolean isDone();

    /**
     * Completes the asynchronous iSCSI read request.
     *
     * @return the number of bytes that were read
     *
     * @throws IScsiException when an error has occurred during the read operation 
     */
    int complete() throws IScsiException;
}