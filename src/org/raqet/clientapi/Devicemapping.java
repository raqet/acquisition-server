/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.clientapi;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "device",
    "lun",
    "success"
})
public class Devicemapping {

    @JsonProperty("device")
    private String _device;
    @JsonProperty("lun")
    private Integer _lun;
    @JsonProperty("success")
    private Boolean _success;
    @JsonIgnore
    private final Map<String, Object> _additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     *     The device
     */
    @JsonProperty("device")
    public String getDevice() {
        return _device;
    }

    /**
     *
     * @param device
     *     The device
     */
    @JsonProperty("device")
    public void setDevice(final String device) {
        _device = device;
    }

    /**
     *
     * @return
     *     The lun
     */
    @JsonProperty("lun")
    public Integer getLun() {
        return _lun;
    }

    /**
     *
     * @param lun
     *     The lun
     */
    @JsonProperty("lun")
    public void setLun(final Integer lun) {
        this._lun = lun;
    }

    /**
     *
     * @return
     *     The success
     */
    @JsonProperty("success")
    public Boolean getSuccess() {
        return _success;
    }

    /**
     *
     * @param success
     *     The success
     */
    @JsonProperty("success")
    public void setSuccess(final Boolean success) {
        this._success = success;
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
