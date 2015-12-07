/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libiscsi;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

public interface IScsiLibrary extends Library {
    IScsiLibrary LIBISCSI = (IScsiLibrary) Native.loadLibrary(Platform.isWindows() ? "libiscsi.dll" : "libiscsi.so.4", IScsiLibrary.class);;

    // "iscsi.h"
    int iscsi_get_fd(ScsiContextByReference iscsi);

    int iscsi_which_events(ScsiContextByReference iscsi);

    int iscsi_service(ScsiContextByReference iscsi, int revents);

    int iscsi_set_timeout(ScsiContextByReference iscsi, int timeout);

    String iscsi_get_error(ScsiContextByReference iscsi);

    ScsiContextByReference iscsi_create_context(String initiator_name);

    int iscsi_destroy_context(ScsiContextByReference iscsi);

    int iscsi_set_targetname(ScsiContextByReference iscsi, String target_name);

    int iscsi_set_session_type(ScsiContextByReference iscsi, int session_type);

    int iscsi_set_initiator_username_pwd(ScsiContextByReference iscsi, String user, String passwd);

    int iscsi_connect_sync(ScsiContextByReference iscsi, String portal);

    int iscsi_full_connect_sync(ScsiContextByReference iscsi, String portal, int lun);

    int iscsi_disconnect(ScsiContextByReference iscsi);

    int iscsi_login_sync(ScsiContextByReference iscsi);

    int iscsi_logout_sync(ScsiContextByReference iscsi);

    int iscsi_discovery_async(ScsiContextByReference iscsi, ScsiCommandCallback cb, Pointer private_data);

    ScsiTask iscsi_read10_task(ScsiContextByReference iscsi, int lun, int lba, int datalen, int blocksize, int rdprotect, int dpo, int fua, int fua_nv, int group_number, ScsiCommandCallback cb, Pointer private_data);

    ScsiTask iscsi_reportluns_sync(ScsiContextByReference iscsi, int report_type, int alloc_len);

    ScsiTask iscsi_testunitready_sync(ScsiContextByReference iscsi, int lun);

    ScsiTask iscsi_inquiry_sync(ScsiContextByReference iscsi, int lun, int evpd, int page_code, int maxsize);

    ScsiTask iscsi_read10_sync(ScsiContextByReference iscsi, int lun, int lba, int datalen, int blocksize, int rdprotect, int dpo, int fua, int fua_nv, int group_number);

    ScsiTask iscsi_readcapacity10_sync(ScsiContextByReference iscsi, int lun, int lba, int pmi);

    ScsiTask iscsi_readcapacity16_sync(ScsiContextByReference iscsi, int lun);

    // "scsi-lowlevel.h"
    void scsi_free_scsi_task(ScsiTask task);

    String scsi_devtype_to_str(int type);

    int scsi_datain_getfullsize(ScsiTask task);

    Pointer scsi_datain_unmarshall(ScsiTask task);
}