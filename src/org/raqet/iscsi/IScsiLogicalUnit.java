/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

import static com.javaforge.reifier.check.ArgumentChecker.argIndex;
import static com.javaforge.reifier.check.ArgumentChecker.argNotNegative;
import static com.javaforge.reifier.check.ArgumentChecker.argNotNull;

import static nl.minvenj.nfi.raqet.jna.libiscsi.IScsiLibrary.LIBISCSI;

import org.apache.log4j.Logger;

import com.sun.jna.Pointer;

import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiContextByReference;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiInquiryDeviceIdentification;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiReadCapacity10;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiSenseAscq;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiSenseKey;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiStatus;
import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiTask;

public final class IScsiLogicalUnit extends IScsiObject implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(IScsiLogicalUnit.class);

    private final IScsiConnection _connection;
    private final int _lun;

    IScsiLogicalUnit(final ScsiContextByReference iScsi, final IScsiConnection connection, final int lun) {
        super(iScsi);

        _connection = connection;
        _lun = lun;
    }

    @Override
    public void close() throws IScsiException {
        _connection.close();
    }

    public int getLun() {
        return _lun;
    }

    public boolean isReady() throws IScsiException {
        ScsiTask task;

        while (true) {
            task = LIBISCSI.iscsi_testunitready_sync(_iScsi, _lun);
            LOG.debug(String.format("iscsi_testunitready_sync(%s, %d); result=%s", _iScsi, _lun, task));

            checkPointer(task, "testunitready failed");

            if (task.status == ScsiStatus.SCSI_STATUS_CHECK_CONDITION) {
                if ((task.sense.key == ScsiSenseKey.SCSI_SENSE_UNIT_ATTENTION) && (task.sense.ascq == ScsiSenseAscq.SCSI_SENSE_ASCQ_BUS_RESET)) {
                    LIBISCSI.scsi_free_scsi_task(task);
                    continue; // Try again
                }
            }
            break;
        }

        try {
            if (task.status == ScsiStatus.SCSI_STATUS_GOOD) {
                return true;
            }
            if ((task.status == ScsiStatus.SCSI_STATUS_CHECK_CONDITION) &&
                (task.sense.key == ScsiSenseKey.SCSI_SENSE_NOT_READY) &&
                (task.sense.ascq == ScsiSenseAscq.SCSI_SENSE_ASCQ_MEDIUM_NOT_PRESENT)) {

                // Not an error, just a cdrom without a disk most likely
                return false;
            }

            throw new IScsiException("TESTUNITREADY failed", getError());
        }
        finally {
            LIBISCSI.scsi_free_scsi_task(task);
        }
    }

    public IScsiPeripheralDeviceType inquireDeviceType() throws IScsiException {
        final ScsiTask task = LIBISCSI.iscsi_inquiry_sync(_iScsi, _lun, 0, 0, 64);
        LOG.debug(String.format("iscsi_inquiry_sync(%s, %d, 0, 0, 64); result=%s", _iScsi, _lun, task));

        checkPointer(task, "Failed to send inquiry command");

        try {
            if (task.status != ScsiStatus.SCSI_STATUS_GOOD) {
                throw new IScsiException("Failed to send inquiry command", getError());
            }

            final Pointer pInq = datainUnmarshall(task);
            final ScsiInquiryDeviceIdentification inq = new ScsiInquiryDeviceIdentification(pInq);
            return IScsiPeripheralDeviceType.forDevtype(inq.device_type);
        }
        finally {
            LIBISCSI.scsi_free_scsi_task(task);
        }
    }

    // TODO: Make API more flexible for efficient access using IScsiInputData
    public int read(final byte[] buf, final int logicalBlockAddress, final int dataLength, final int blockSize) throws IScsiException {
        argNotNull("buf", buf);
        argNotNegative("logicalBlockAddress", logicalBlockAddress);
        argNotNegative("dataLength", dataLength);

        if (blockSize <= 0) {
            throw new IllegalArgumentException("blockSize must be positive");
        }
        if (dataLength > buf.length) {
            throw new IllegalArgumentException("buf is not large enough to store dataLength bytes");
        }

        final ScsiTask task = LIBISCSI.iscsi_read10_sync(_iScsi, getLun(), logicalBlockAddress, dataLength, blockSize, 0, 0, 0, 0, 0);
        LOG.debug(String.format("iscsi_read10_sync(%s, %d, %d, %d, %d, 0, 0, 0, 0, 0); result=%s", _iScsi, getLun(), logicalBlockAddress, dataLength, blockSize, task));

        checkPointer(task, "Failed to send READ10 command");

        try {
            if (task.status != ScsiStatus.SCSI_STATUS_GOOD) {
                throw new IScsiException("READ10 command failed", getError());
            }

            final int bytesRead = task.datain.size;
            if (bytesRead > 0) {
                task.datain.data.read(0, buf, 0, bytesRead);
            }

            return bytesRead;
        }
        finally {
            LIBISCSI.scsi_free_scsi_task(task);
        }
    }

    public IScsiReadFuture read(final long firstBlock, final int blocksToRead, final int blockSize, final byte[] buffer, final int offset) throws IScsiException {
        argNotNegative("firstBlock", firstBlock);
        argNotNegative("blockCount", blocksToRead);
        argNotNull("buffer", buffer);
        argIndex("offset", offset, buffer.length);

        if (blockSize <= 0) {
            throw new IllegalArgumentException("blockSize must be positive");
        }

        final int dataLength = (blocksToRead * blockSize);
        if (dataLength > (buffer.length - offset)) {
            throw new IllegalArgumentException("buffer is not large enough (" + buffer.length + ") to hold " + blocksToRead + " blocks, starting at " + offset);
        }

        final IScsiRead10Callback callback = new IScsiRead10Callback(_connection, buffer, offset);

        final ScsiTask task = LIBISCSI.iscsi_read10_task(_iScsi, getLun(), (int) firstBlock, dataLength, blockSize, 0, 0, 0, 0, 0, callback, null);
        LOG.debug(String.format("iscsi_read10_task(%s, %d, %d, %d, %d, 0, 0, 0, 0, 0, %s, null); result=%s", _iScsi, getLun(), firstBlock, dataLength, blockSize, callback, task));

        checkPointer(task, "Failed to send READ10 command");

        return callback;
    }

    public IScsiLogicalUnitCapacity readCapacity() throws IScsiException {
        final ScsiTask task = LIBISCSI.iscsi_readcapacity10_sync(_iScsi, getLun(), 0, 0);
        LOG.debug(String.format("iscsi_readcapacity10_sync(%s, %d, 0, 0); result=%s", _iScsi, getLun(), task));

        checkPointer(task, "Failed to send readcapacity command");

        try {
            if (task.status != ScsiStatus.SCSI_STATUS_GOOD) {
                throw new IScsiException("Failed to send readcapacity command");
            }

            final ScsiReadCapacity10 rc10 = new ScsiReadCapacity10(datainUnmarshall(task));
            return new IScsiLogicalUnitCapacity((rc10.lba & 0xffffffffL), rc10.block_size);
        }
        finally {
            LIBISCSI.scsi_free_scsi_task(task);
        }
    }

    public void sync() throws IScsiException {
        _connection.sync();
    }

    @Override
    public String toString() {
        return String.format("Lun:%04d", getLun());
    }
}
