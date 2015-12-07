/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.test.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.raqet.test.RaqetConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class UserRestApiSteps {
    private final RaqetConfig _config;
    private final Client _client;
    private static final String BASEURL = "http://localhost:5556/";
    String _httpResponse;
    JsonNode _jsonResponse;

    public UserRestApiSteps(final RaqetConfig config) {
        _config = config;
        _client = ClientBuilder.newClient();
        _httpResponse = "";
    }

    public void jsonRequest(final String url) {
        final WebTarget target = _client.target(url);
        final Builder builder = target.request();
        final Response response = builder.get();
        _jsonResponse = response.readEntity(JsonNode.class);
    }

    public void jsonRequest(final String url, final Object entity) {
        final WebTarget target = _client.target(url);
        final Builder builder = target.request();
        final Response response = builder.post(Entity.json(entity));
        _jsonResponse = response.readEntity(JsonNode.class);
    }

    public void txtRequest(final String url) {
        final WebTarget target = _client.target(url);
        final Builder builder = target.request();
        final Response response = builder.get();
        _httpResponse = response.readEntity(String.class);
    }

    @When("requesting the list of active clients as text")
    public void listClientsText() {
        txtRequest(BASEURL + "remotes/list");
    }

    @When("requesting the list of active clients as json")
    public void listClientsJson() {
        jsonRequest(BASEURL + "remotes/json/list");
    }

    @Then("the list of active clients in html contains $name")
    public void checkClientHTML(final String name) {
        assertTrue(_httpResponse.contains(name));
    }

    @Then("the list of active clients in json contains $name")
    public void checkClientJson(final String name) {
        System.out.println(_jsonResponse);
        for (final JsonNode client : _jsonResponse) {
            System.out.println(client);
            final JsonNode targetName = client.get(0).path("targetName");
            if (targetName.toString().contains(name)) {
                return;
            }
        }
        fail("None of portal URL's contain " + name);
    }

    @When("requesting the list of investigationcases")
    public void listCases() {
        jsonRequest(BASEURL + "remotes/json/cases");
    }

    @Then(value = "the list of investigationcases contains $expectedNumber items", priority = 1)
    public void listCasesCount(final int expectedNumber) {
        System.out.println(_jsonResponse);
        assertEquals("InvestigationCases has incorrect number of items", expectedNumber, _jsonResponse.size());
    }

    @Then("the list of investigationcases is empty")
    public void listCasesEmpty() {
        listCasesCount(0);
    }

    public void checkStringListContentName(final String searchItemDescription, final String fieldname, final String fieldcontent) {
        for (final JsonNode investigationCase : _jsonResponse) {
            System.out.println(investigationCase);
            final JsonNode value = investigationCase.get(fieldname);
            if (value.asText().equals(fieldcontent)) {
                return;
            }
        }
        fail(searchItemDescription + " " + fieldcontent + "not present in " + _jsonResponse);
    }

    @Then("the list of investigationcases contains $casename")
    public void checkCaseName(final String casename) {
        checkStringListContentName("Case name", "urlcasename", casename);
    }

    @When("adding a investigationcase $casename")
    public void addCases(final String casename) {
        final ObjectNode investigationCase = JsonNodeFactory.instance.objectNode();
        investigationCase.put("urlcasename", casename);
        investigationCase.put("casename", casename);
        jsonRequest(BASEURL + "remotes/json/cases/" + casename, investigationCase);
    }

    @When("requesting the list of computers for investigationcase $casename")
    public void listComputers(final String casename) {
        jsonRequest(BASEURL + "remotes/json/cases/" + casename + "/computers");
    }

    @Then(value = "the list of computers contains $expectedNumber items", priority = 1)
    public void listComputersCount(final int expectedNumber) {
        System.out.println(_jsonResponse);
        assertEquals("Computers list has incorrect number of items", expectedNumber, _jsonResponse.size());
    }

    @Then("the list of computers contains $computername")
    public void checkComputerName(final String computername) {
    }

    @Then("the list of computers is empty")
    public void listComputersEmpty() {
        listComputersCount(0);
    }

    @When("adding a computer $computername with clientid $clientid to $casename")
    public void addComputer(final String computername, final String clientId, final String casename) {
        final ObjectNode computer = JsonNodeFactory.instance.objectNode();
        computer.put("urlcomputername", computername);
        computer.put("computername", computername);
        computer.put("clientid", clientId);
        jsonRequest(BASEURL + "remotes/json/cases/" + casename + "/computers/" + computername, computer);
    }

    @Then("no error must be given")
    public void checkStatusNotFail() {
        System.out.println(_jsonResponse);
        assertFalse("json response contains fail", _jsonResponse.asText().toLowerCase().contains("fail"));
    }

    @Then("the error $errortext must be given")
    public void checkStatusFail(final String errortext) {
        System.out.println(_jsonResponse);
        assertTrue("json response does not contain " + errortext + "(" + _jsonResponse + ")",
                   _jsonResponse.asText().contains(errortext));
    }

}
