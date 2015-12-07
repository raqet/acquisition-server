/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.clientapi;

import java.io.IOException;
import java.io.StringWriter;
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
    "smartdiskinfo",
    "sddiskinfo",
    "hddiskinfo",
})
public class DiskInfo {

//    @JsonProperty("smartdiskinfo")
    private String _smartdiskinfo;
    @JsonProperty("sddiskinfo")
    private String _sddiskinfo;
    @JsonProperty("hddiskinfo")
    private String _hddiskinfo;
    @JsonIgnore
    private final Map<String, Object> _additionalProperties = new HashMap<String, Object>();

    @JsonProperty("smartdiskinfo")
    public String getSmartdiskinfo() {
        return _smartdiskinfo;
    }

    @JsonProperty("smartdiskinfo")
    public void setSmartdiskinfo(final String smartdiskinfo) {
        _smartdiskinfo = smartdiskinfo;
    }

    @JsonProperty("sddiskinfo")
    public String getSddiskinfo() {
        return _sddiskinfo;
    }

    @JsonProperty("sddiskinfo")
    public void setSddiskinfo(final String sddiskinfo) {
        _sddiskinfo = sddiskinfo;
    }

    @JsonProperty("hddiskinfo")
    public String getHddiskinfo() {
        return _hddiskinfo;
    }

    @JsonProperty("hddiskinfo")
    public void setHddiskinfo(final String hddiskinfo) {
        _hddiskinfo = hddiskinfo;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this._additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(final String name, final Object value) {
        this._additionalProperties.put(name, value);
    }

    @JsonIgnore
    @Override
    public String toString() {
        try (final StringWriter output = new StringWriter()) {
            output.append("smartdiskinfo\n");
            output.append(_smartdiskinfo);
            output.append("sddiskinfo\n");
            output.append(_sddiskinfo);
            output.append("hddiskinfo\n");
            output.append(_hddiskinfo);
            return output.toString();
        }
        catch (final IOException e) {
        }
        return _smartdiskinfo;
    }

}
