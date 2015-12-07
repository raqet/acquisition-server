/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libiscsi;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface ScsiCommandCallback extends Callback {

    void callback(ScsiContextByReference iscsi, int status, Pointer command_data, Pointer private_data);
}