/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

import static com.javaforge.reifier.check.ArgumentChecker.argNotNegative;
import static com.javaforge.reifier.check.ArgumentChecker.argNotNull;
import static com.javaforge.reifier.check.ArgumentChecker.argNotNullAndNotEmpty;

import static nl.minvenj.nfi.raqet.jna.libiscsi.IScsiLibrary.LIBISCSI;

import org.apache.log4j.Logger;

import nl.minvenj.nfi.raqet.jna.libiscsi.ScsiContextByReference;

public final class IScsiContext extends IScsiObject implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(IScsiContext.class);

    private IScsiContext(final ScsiContextByReference iScsi) {
        super(iScsi);
    }

    /**
     * Creates a new {@link IScsiContext context} for an iSCSI session.
     *
     * @param initiatorName the iqn name to identify to the target as
     * @return the new {@link IScsiContext}
     *
     * @throws IScsiException on error creating the context
     */
    public static IScsiContext create(final String initiatorName) throws IScsiException {
        final ScsiContextByReference iscsi = LIBISCSI.iscsi_create_context(initiatorName);
        LOG.debug(String.format("iscsi_create_context('%s'); result=%s", initiatorName, iscsi));

        if (iscsi == null) {
            throw new IScsiException("Failed to create iSCSI context");
        }

        return new IScsiContext(iscsi);
    }

    @Override
    public void close() throws IScsiException {
        final int result = LIBISCSI.iscsi_destroy_context(_iScsi);
        LOG.debug(String.format("iscsi_destroy_context(%s); result=%s", _iScsi, result));

        if (result < 0) {
            throw new IScsiException("Failed to destroy iSCSI context " + this);
        }
    }

    public void setTimeout(final int timeout) {
        argNotNegative("timeout", timeout);

        final int result = LIBISCSI.iscsi_set_timeout(_iScsi, timeout);
        LOG.debug(String.format("iscsi_set_timeout(%s, %d); result=%d", _iScsi, timeout, result));

        // Setting the timeout never fails.
    }

    public void setTargetName(final String targetName) throws IScsiException {
        argNotNull("targetName", targetName);

        final int result = LIBISCSI.iscsi_set_targetname(_iScsi, targetName);
        LOG.debug(String.format("iscsi_set_targetname(%s, '%s'); result=%d", _iScsi, targetName, result));

        checkResult(result, "Failed to set target name to '" + targetName + '\'');
    }

    public void setSessionType(final IScsiSessionType sessionType) throws IScsiException {
        argNotNull("sessionType", sessionType);

        final int result = LIBISCSI.iscsi_set_session_type(_iScsi, sessionType.getIntValue());
        LOG.debug(String.format("iscsi_set_session_type(%s, %s); result=%d", _iScsi, sessionType, result));

        checkResult(result, "Failed to set iSCSI session type to " + sessionType);
    }

    public void setInitiatorUsernamePwd(final String username, final String password) throws IScsiException {
        argNotNull("username", username);
        argNotNull("password", password);

        final int result = LIBISCSI.iscsi_set_initiator_username_pwd(_iScsi, username, password);
        LOG.debug(String.format("iscsi_set_initiator_username_pwd(%s, '%s', ***); result=%d" + result, _iScsi, username, result));

        checkResult(result);
    }

    public IScsiConnection connect(final String portal) throws IScsiException {
        argNotNullAndNotEmpty("portal", portal);

        final int result = LIBISCSI.iscsi_connect_sync(_iScsi, portal);
        LOG.debug(String.format("iscsi_connect_sync(%s, %s); result=%d", _iScsi, portal, result));

        checkResult(result, "Failed to connect to '" + portal + "'");

        return new IScsiConnection(_iScsi);
    }

    public IScsiConnection fullConnect(final String portal, final int lun) throws IScsiException {
        argNotNullAndNotEmpty("portal", portal);

        final int result = LIBISCSI.iscsi_full_connect_sync(_iScsi, portal, lun);
        LOG.debug(String.format("iscsi_full_connect_sync(%s, %s, %d); result=%d", _iScsi, portal, lun, result));

        checkResult(result, "Failed to connect to '" + portal + "' lun " + lun);

        return new IScsiConnection(_iScsi);
    }
    
    public IScsiLogicalUnit fullConnectLogicalUnit(final String portal, final int lun) throws IScsiException {
        argNotNegative("lun", lun);

        final IScsiConnection connection = fullConnect(portal, lun);
        return new IScsiLogicalUnit(_iScsi, connection, lun);
    }
}