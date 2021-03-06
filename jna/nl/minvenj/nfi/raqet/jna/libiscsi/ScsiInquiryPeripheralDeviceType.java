/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libiscsi;

public interface ScsiInquiryPeripheralDeviceType {
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_DIRECT_ACCESS = 0x00;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_SEQUENTIAL_ACCESS = 0x01;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_PRINTER = 0x02;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_PROCESSOR = 0x03;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_WRITE_ONCE = 0x04;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_MMC = 0x05;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_SCANNER = 0x06;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_OPTICAL_MEMORY = 0x07;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_MEDIA_CHANGER = 0x08;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_COMMUNICATIONS = 0x09;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_STORAGE_ARRAY_CONTROLLER = 0x0c;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_ENCLOSURE_SERVICES = 0x0d;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_SIMPLIFIED_DIRECT_ACCESS = 0x0e;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_OPTICAL_CARD_READER = 0x0f;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_BRIDGE_CONTROLLER = 0x10;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_OSD = 0x11;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_AUTOMATION = 0x12;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_SEQURITY_MANAGER = 0x13;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_WELL_KNOWN_LUN = 0x1e;
    int SCSI_INQUIRY_PERIPHERAL_DEVICE_TYPE_UNKNOWN = 0x1f;
}