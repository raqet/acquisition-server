/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "urlcomputername",
    "computername",
    "clientid",
    "hardwareid",
    "disks"})
public class ComputerDbEntry {
    @JsonProperty("urlcomputername")
    private String _urlcomputerName;
    @JsonProperty("computername")
    private String _computerName;
    @JsonProperty("clientid")
    private String _clientId;
    @JsonProperty("hardwareid")
    private String _hardwareId;
    @JsonProperty("disks")
    private ArrayList<String> _disks;
    private File _baseStorageFolder;
    private String _caseAndComputer;


    public ComputerDbEntry() {
        setComputerName("Unknown computer");
        setUrlcomputerName("unknowncomputer");
        _clientId = "";
        _hardwareId = "";
        _disks = new ArrayList<String>();
    }

    @JsonProperty("urlcomputername")
    public String getUrlComputerName() {
        return _urlcomputerName;
    }

    @JsonProperty("urlcomputername")
    public void setUrlcomputerName(final String urlcomputerName) {
        _urlcomputerName = urlcomputerName;
    }

    @JsonProperty("computername")
    public String getComputerName() {
        return _computerName;
    }

    @JsonProperty("computername")
    public void setComputerName(final String computerName) {
        _computerName = computerName;
    }

    @JsonProperty("clientid")
    public String getClientId() {
        return _clientId;
    }

    @JsonProperty("clientid")
    public void setClientId(final String clientId) {
        _clientId = clientId;
    }

    @JsonProperty("hardwareid")
    public String getHardwareId() {
        return _hardwareId;
    }

    @JsonProperty("hardwareid")
    public void setHardwareId(final String hardwareId) {
        _hardwareId = hardwareId;
    }

    @JsonIgnore
    public File getBaseStorageFolder() {
        return _baseStorageFolder;
    }

    @JsonIgnore
    public void setBaseStorageFolder(final File baseFolder) {
        _baseStorageFolder = baseFolder;
    }

    @JsonIgnore
    public String getCaseAndComputer() {
        return _caseAndComputer;
    }

    @JsonIgnore
    public void setCaseAndComputer(final String caseAndComputer) {
        _caseAndComputer = caseAndComputer;
    }

    @JsonProperty("disks")
    public List<String> getDisks() {
        return _disks;
    }

    @JsonProperty("disks")
    public void setDisks(final ArrayList<String> disks) {
        _disks = disks;
    }

}
