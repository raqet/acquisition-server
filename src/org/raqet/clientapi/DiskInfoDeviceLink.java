/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.clientapi;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "device",
    "diskinfo"
})
public class DiskInfoDeviceLink {

    @JsonProperty("device")
    private String _device;
    @JsonProperty("diskinfo")
    private DiskInfo _diskinfo;
    @JsonIgnore
    private final Map<String, Object> _additionalProperties = new HashMap<String, Object>();


    @JsonProperty("device")
    public String getDevice() {
        return _device;
    }

    @JsonProperty("device")
    public void setDevice(final String device) {
        _device = device;
    }

    @JsonProperty("diskinfo")
    public DiskInfo getDiskinfo() {
        return _diskinfo;
    }

    @JsonProperty("diskinfo")
    public void setDiskinfo(final DiskInfo diskinfo) {
        _diskinfo = diskinfo;
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
