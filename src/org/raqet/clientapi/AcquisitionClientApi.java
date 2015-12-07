/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.clientapi;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.Request;
import org.raqet.ClientInfo;
import org.raqet.http.RaqetControl;

/**
 * Implements the HTTP interface from the remote acquisition operating system. Mainly informs the server of
 * the existence of the client (based on IP).
 * The client also reports additional information such as:
 * * logging of the client side application
 * * hardware information like:
 * ** disk-id's
 * ** hwclock
 *
 * @author schramp
 *
 */
@Path("/clientapi")
@WebService
public final class AcquisitionClientApi {
    private static final Logger LOG = Logger.getLogger(AcquisitionClientApi.class);

    @Inject
    private RaqetControl _raqetControl;

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> register(@Context final Request request, final Register target) {
        final ClientInfo clientInfo = new ClientInfo();
        clientInfo.setIpAddress(request.getRemoteAddr());
        clientInfo.setClientid(target.getClientid());
        clientInfo.setHostname(target.getHostname());
        clientInfo.addLogging(target.getLogging());
        clientInfo.setDevicemapping(target.getDevicemapping());
        clientInfo.setHardwareid(target.getHardwareid());

        LOG.info("register " + request.getRemoteAddr() + " " + target.getClientid() + " " + target.getHardwareid());
        try {
            _raqetControl.addClient(clientInfo);
        }
        catch (final Exception e) {
            LOG.info("register failed", e);
            LOG.info("addRemoteDevice failed during client registration", e);
        }

        final List<String> result= new ArrayList<String>();
        return result;
    }

    @POST
    @Path("/diskinfo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> diskinfo(@Context final Request request, final DiskInfoMessage diskInformation) {
        final ClientInfo clientInfo = new ClientInfo();
        clientInfo.setIpAddress(request.getRemoteAddr());
        clientInfo.setClientid(diskInformation.getClientid());
        clientInfo.setHostname(diskInformation.getHostname());
        clientInfo.setHardwareid(diskInformation.getHardwareid());

        LOG.info("diskinfo " + request.getRemoteAddr() + " " + diskInformation.getClientid() + " " + diskInformation.getHardwareid());
        try {
            _raqetControl.diskInformation(clientInfo, diskInformation);
        }
        catch (final Exception e) {
            LOG.info("register failed", e);
            LOG.info("addRemoteDevice failed during client registration", e);
        }

        final List<String> result = new ArrayList<String>();
        return result;
    }


}
