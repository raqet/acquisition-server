/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.http;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "urlcomputername",
    "computername",
    "clientid"})
public class Computer {
    @JsonProperty("urlcomputername")
    private String _urlcomputerName;
    @JsonProperty("computername")
    private String _computerName;
    @JsonProperty("clientid")
    private String _clientid;

    public Computer() {
        setComputerName("Unknown computer");
        setUrlComputerName("unknown_computer");
        setClientId("UnknownClient");
    }

    public Computer(final String computerName, final String urlComputerName) {
        setComputerName(computerName);
        setUrlComputerName(urlComputerName);
    }

    @JsonProperty("urlcomputername")
    public String getUrlComputerName() {
        return _urlcomputerName;
    }

    @JsonProperty("urlcomputername")
    public void setUrlComputerName(final String urlComputerName) {
        _urlcomputerName = urlComputerName;
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
        return _clientid;
    }

    @JsonProperty("clientid")
    public void setClientId(final String clientid) {
        _clientid = clientid;
    }
}
