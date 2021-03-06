/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.clientapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "clientid",
    "hardwareid",
    "hostname",
    "diskinformation",
})
public class DiskInfoMessage {

    @JsonProperty("clientid")
    private String _clientid;
    @JsonProperty("hardwareid")
    private String _hardwareid;
    @JsonProperty("hostname")
    private String _hostname;
    @JsonProperty("diskinformation")
    private List<DiskInfoDeviceLink> _diskInfo = new ArrayList<DiskInfoDeviceLink>();
    @JsonIgnore
    private final Map<String, Object> _additionalProperties = new HashMap<String, Object>();

    /**
    *
    * @return
    *     The clientid
    */
    @JsonProperty("clientid")
    public String getClientid() {
        return _clientid;
    }

    /**
     *
     * @param clientid
     *     The clientid
     */
    @JsonProperty("clientid")
    public void setClientid(final String clientid) {
        this._clientid = clientid;
    }

    /**
    *
    * @return
    *     The hardwareId
    */
    @JsonProperty("hardwareid")
    public String getHardwareid() {
        return _hardwareid;
    }

    /**
     *
     * @param hardwareId
     *     The hardwareId
     */
    @JsonProperty("hardwareid")
    public void setHardwareid(final String hardwareid) {
        this._hardwareid = hardwareid;
    }

    /**
       *
       * @return
       *     The hostname
       */
    @JsonProperty("hostname")
    public String getHostname() {
        return _hostname;
    }

    /**
     *
     * @param hostname
     *     The hostname
     */
    @JsonProperty("hostname")
    public void setHostname(final String hostname) {
        this._hostname = hostname;
    }

    @JsonProperty("diskinformation")
    public List<DiskInfoDeviceLink> getDiskInformation() {
        return _diskInfo;
    }

    @JsonProperty("diskinformation")
    public void setDiskInformation(final List<DiskInfoDeviceLink> diskInfo) {
        _diskInfo = diskInfo;
    }


    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this._additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(final String name, final Object value) {
        this._additionalProperties.put(name, value);
    }

}
