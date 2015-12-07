/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libiscsi;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public final class ScsiTask extends Structure {
    private static final int SCSI_CDB_MAX_SIZE = 16;

    public int status;

    public int cdb_size;
    public int xfer_dir;
    public int expxferlen;
    public byte[] cdb = new byte[SCSI_CDB_MAX_SIZE];

    public int residual_status;
    public long residual;
    public ScsiSense sense;
    public ScsiData datain;
    public Pointer mem;

    public Pointer ptr;

    public int itt;
    public int cmdsn;
    public int lun;

    public ScsiIovector iovector_in;
    public ScsiIovector iovector_out;

    public ScsiTask(final Pointer p) {
        super(p);

        read();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
        return Arrays.asList("status",
                             "cdb_size", "xfer_dir", "expxferlen", "cdb",
                             "residual_status", "residual", "sense", "datain", "mem",
                             "ptr",
                             "itt", "cmdsn", "lun",
                             "iovector_in", "iovector_out");
    }

    @Override
    public String toString() {
        return getPointer().toString().replace("native@", ScsiTask.class.getSimpleName() + "@");
    }
}