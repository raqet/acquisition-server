/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.http;

import javax.annotation.Generated;

import org.raqet.database.InvestigationCaseDB;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "urlcasename",
    "casename"})
public class InvestigationCase {
    @JsonProperty("urlcasename")
    private String _urlCaseName;
    @JsonProperty("casename")
    private String _caseName;

    public InvestigationCase() {
        setCaseName("Unknown case");
        setUrlCaseName(getCaseName());
    }

    public InvestigationCase(final String caseName) {
        setCaseName(caseName);
        setUrlCaseName(caseName);
    }

    @JsonProperty("urlcasename")
    public String getUrlCaseName() {
        return _urlCaseName;
    }

    @JsonProperty("urlcasename")
    public void setUrlCaseName(final String urlCaseName) {
        _urlCaseName = InvestigationCaseDB.escapeName(urlCaseName);
    }

    @JsonProperty("casename")
    public String getCaseName() {
        return _caseName;
    }

    @JsonProperty("casename")
    public void setCaseName(final String caseName) {
        _caseName = caseName;
    }

}
