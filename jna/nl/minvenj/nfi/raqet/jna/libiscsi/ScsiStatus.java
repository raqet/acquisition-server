/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libiscsi;

public interface ScsiStatus {
    int SCSI_STATUS_GOOD = 0;
    int SCSI_STATUS_CHECK_CONDITION = 2;
    int SCSI_STATUS_CONDITION_MET = 4;
    int SCSI_STATUS_BUSY = 8;
    int SCSI_STATUS_RESERVATION_CONFLICT = 0x18;
    int SCSI_STATUS_TASK_SET_FULL = 0x28;
    int SCSI_STATUS_ACA_ACTIVE = 0x30;
    int SCSI_STATUS_TASK_ABORTED = 0x40;
    int SCSI_STATUS_REDIRECT = 0x101;
    int SCSI_STATUS_CANCELLED = 0x0f000000;
    int SCSI_STATUS_ERROR = 0x0f000001;
    int SCSI_STATUS_TIMEOUT = 0x0f000002;
}