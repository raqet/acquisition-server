/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.http;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.raqet.iscsi.IScsiLogicalUnitCapacity;
import org.raqet.remote.RemoteDeviceInfo;
import org.raqet.util.FileUtilities;


@Path("/remotes")
@WebService
public final class RaqetService {
    private static final Logger LOG = Logger.getLogger(RaqetService.class);

    @Inject
    private RaqetControl _raqetControll;

    //TODO: Remove old API
    @GET
    @Path("/list")
    @Produces("text/html")
    public String getDeviceList() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        final Map<String, List<RemoteDeviceInfo>> portalMap = _raqetControll.getRemoteDeviceManager().getPortalMap();

        for (final Entry<String, List<RemoteDeviceInfo>> portalInfos : portalMap.entrySet()) {
            sb.append("<ul>");
            sb.append(portalInfos.getKey());
            for (final RemoteDeviceInfo remoteDevice : portalInfos.getValue()) {
                sb.append("<p>");
                sb.append("<ul>");
                sb.append("<li>").append(remoteDevice.getTargetName()).append("</li>");
                sb.append("<li>").append("LUN:  ").append(remoteDevice.getLun()).append("</li>");
                sb.append("<li>").append("Type: ").append(remoteDevice.getDeviceType()).append("</li>");
                sb.append("<li>").append("URL:  ").append(remoteDevice.getPortal()).append("</li>");

                final IScsiLogicalUnitCapacity capacity = remoteDevice.getCapacity();
                final long size = (capacity.getLogicalBlockAddress() + 1) * capacity.getBlockSize();
                sb.append("<li>").append("Size: ").append(FileUtilities.toUserFriendlyByteString(size)).append("</li>");
                sb.append("<li>").append("Blk:  ").append(capacity.getBlockSize()).append("</li>");
                sb.append("</ul>");
                sb.append("</p>");

            }
            sb.append("</li>");
            sb.append("</ul>");
        }
        sb.append("</html>");

        return sb.toString();
    }

    //TODO: Remove old API
    @GET
    @Path("/json/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<RemoteDeviceInfo>> jsonGetDeviceList() {
        return _raqetControll.getRemoteDeviceManager().getPortalMap();
    }

    //TODO: Remove old API
    @GET
    @Path("/add/{target}")
    @Produces(MediaType.APPLICATION_JSON)
    public String addDevice(@PathParam("target") final String target) {
        try {
            _raqetControll.addRemoteDevice(target);
        }
        catch (final Exception e) {
            LOG.warn("Failed to access target:" + target, e);
        }
        return "Added";
    }

    @GET
    @Path("/json/cases")
    @Produces(MediaType.APPLICATION_JSON)
    public List<InvestigationCase> jsonGetInvestigationCaseList() {
        return _raqetControll.getInvestigationCases();
    }

    @POST
    @Path("/json/cases/{caseName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String jsonCreateInvestigationCase(@PathParam("caseName") final String caseName, final InvestigationCase investigationCase) {
        try {
            _raqetControll.createInvestigationCase(investigationCase);
            return "\"Success\"";
        }
        catch (final Exception e) {
            LOG.info("Failed to create case", e);
        }
        return "\"Failed\"";
    }

    @PUT
    @Path("/json/cases/{caseName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String jsonUpdateInvestigationCase(@PathParam("caseName") final String caseName, final InvestigationCase investigationCase) {
        LOG.info("Updating case not implemented");
        return "\"Success\"";
    }

    @DELETE
    @Path("/json/cases/{caseName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String jsonDeleteInvestigationCase(@PathParam("caseName") final String caseName, final InvestigationCase investigationCase) {
        LOG.info("Delete case not implemented");
        return "\"Success\"";
    }



    @GET
    @Path("/json/cases/{caseName}")
    @Produces(MediaType.APPLICATION_JSON)
    public InvestigationCase jsonGetInvestigationCase(@PathParam("caseName") final String caseName) {
        try {
            return _raqetControll.getInvestigationCase(caseName);
        }
        catch (final IOException e) {
            LOG.info("Unable to get case for " + caseName, e);
        }
        return null;
    }

    @GET
    @Path("/json/cases/{caseName}/computers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Computer> jsonGetComputerList(@PathParam("caseName") final String caseName) {
        return _raqetControll.getComputers(caseName);
    }

    @POST
    @Path("/json/cases/{caseName}/computers/{computerName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String jsonCreateComputer(@PathParam("caseName") final String caseName, @PathParam("computerName") final String computerName, final Computer computer) {
        try {
            _raqetControll.createComputer(caseName, computer);
            return "\"Success\"";
        }
        catch (final Exception e) {
            LOG.info("Failed to create computer", e);
        }
        return "\"Failed\"";
    }

    @GET
    @Path("/json/cases/{caseName}/computers/{computerName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Computer jsonGetComputer(@PathParam("caseName") final String caseName, @PathParam("computerName") final String computerName) {
        try {
            return _raqetControll.getComputer(caseName, computerName);
        }
        catch (final Exception e) {
            LOG.info("Failed to get " + caseName + " computer " + computerName, e);
        }
        return null;
    }

    @GET
    @Path("/json/cases/{caseName}/computers/{computerName}/disks")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> jsonGetDiskList(@PathParam("caseName") final String caseName, @PathParam("computerName") final String computerName) {
        try {
            return _raqetControll.getDisks(caseName, computerName);
        }
        catch (final Exception e) {
            LOG.info("Failed to list disks for " + caseName + " computer " + computerName, e);
            return null;
        }
    }

    @GET
    @Path("/json/cases/{caseName}/computers/{computerName}/acquisitionsystem/{type}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response jsonGetAcquisitionSystem(@PathParam("caseName") final String caseName, @PathParam("computerName") final String computerName, @PathParam("type") final String imageType) {
        if (imageType.equals("pxezip") || imageType.equals("iso")) {
            String extension = "iso";
            if (imageType.equals("pxezip")) extension = "zip";
            try {
                final File osfile = _raqetControll.getGetAcquisitionSystem(caseName, computerName, imageType);
                return Response.ok(osfile, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + caseName + "_" + computerName + "." + extension + "\"")
                    .build();
            }
            catch (final Exception e) {
                LOG.info("Failed to create acquisition OS for  " + caseName + " computer " + computerName, e);
                return null;
            }
        } else {
                LOG.info("Invalid image OS type " +imageType+ " for  " + caseName + " computer " + computerName);
        }
        return null;
    }
}
