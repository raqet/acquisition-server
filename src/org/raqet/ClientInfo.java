/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.raqet.clientapi.Devicemapping;
import org.raqet.database.ComputerDbEntry;


public class ClientInfo {

    private String _hostname;
    private final List<String> _logging = new ArrayList<String>();
    private String _clientId;
    private String _hardwareId;
    private String _ipAddress;
    private final Map<Integer, Devicemapping> _devicemapping = new HashMap<Integer, Devicemapping>();
    private final Date _lastSeen = new Date();

    /** This folder stores all data related to this client. */
    private ComputerDbEntry _computerDbEntry;

    public void setDevicemapping(final List<Devicemapping> devicemapping) {
        for (final Devicemapping dev : devicemapping) {
            _devicemapping.put(dev.getLun(), dev);
        }
    }

    public Devicemapping getDeviceMapping(final int lun) {
        return _devicemapping.get(lun);
    }

    public String getHostname() {
        return _hostname;
    }

    public void setHostname(final String hostname) {
        _hostname = hostname;
    }

    public List<String> getLogging() {
        return _logging;
    }

    public void addLogging(final List<String> logging) {
        //TODO: RAQET-81 logging should be written to disk
        _logging.addAll(logging);
    }

    public String getClientid() {
        return _clientId;
    }

    public void setClientid(final String clientid) {
        _clientId = clientid;
    }

    public String getHardwareid() {
        return _hardwareId;
    }

    public void setHardwareid(final String hardwareid) {
        _hardwareId = hardwareid;
    }

    public String getIpAddress() {
        return _ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        _ipAddress = ipAddress;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ClientInfo)) {
            return false;
        }
        final ClientInfo target = (ClientInfo) obj;
        return _clientId.equals(target._clientId) && _ipAddress.equals(target._ipAddress) && _hardwareId.equals(target._hardwareId);
    }

    @Override
    public int hashCode() {
        return _clientId.hashCode() + _ipAddress.hashCode() + _hardwareId.hashCode();
    }

    public void seen() {
        _lastSeen.setTime(System.currentTimeMillis());
    }

    public void setComputerDbEntry(final ComputerDbEntry computer) {
        _computerDbEntry = computer;
    }

    public ComputerDbEntry getComputerDbEntry() {
        return _computerDbEntry;
    }

}
