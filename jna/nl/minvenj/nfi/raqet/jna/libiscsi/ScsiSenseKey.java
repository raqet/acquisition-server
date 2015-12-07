/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libiscsi;

// sense keys
public interface ScsiSenseKey {
    int SCSI_SENSE_NO_SENSE = 0x00;
    int SCSI_SENSE_RECOVERED_ERROR = 0x01;
    int SCSI_SENSE_NOT_READY = 0x02;
    int SCSI_SENSE_MEDIUM_ERROR = 0x03;
    int SCSI_SENSE_HARDWARE_ERROR = 0x04;
    int SCSI_SENSE_ILLEGAL_REQUEST = 0x05;
    int SCSI_SENSE_UNIT_ATTENTION = 0x06;
    int SCSI_SENSE_DATA_PROTECTION = 0x07;
    int SCSI_SENSE_BLANK_CHECK = 0x08;
    int SCSI_SENSE_VENDOR_SPECIFIC = 0x09;
    int SCSI_SENSE_COPY_ABORTED = 0x0a;
    int SCSI_SENSE_COMMAND_ABORTED = 0x0b;
    int SCSI_SENSE_OBSOLETE_ERROR_CODE = 0x0c;
    int SCSI_SENSE_OVERFLOW_COMMAND = 0x0d;
    int SCSI_SENSE_MISCOMPARE = 0x0e;
}