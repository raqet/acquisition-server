/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet;

import java.io.File;
import java.io.FileInputStream;
import java.net.Inet4Address;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

import org.alfresco.jlan.netbios.server.NetBIOSNameServer;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.smb.server.SMBServer;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.raqet.clientapi.AcquisitionClientApi;
import org.raqet.http.RaqetBinder;
import org.raqet.http.RaqetControl;
import org.raqet.http.RaqetService;
import org.raqet.iscsi.IScsiException;
import org.raqet.smb.SmbServerConfiguration;


public final class Main {
    private static final Logger LOG = Logger.getLogger(Main.class);

    private static final String INITIATOR_NAME = "iqn.2015-04.org.raqet:hkqg55j";
    private static final String EVIDENCE_DIRECTORY = "/var/raqet";

    private static final String SMB_HOSTNAME = "127.0.0.1";
    private static final String SMB_DOMAIN = "RAQETDOMAIN";
    private static final String SMB_SHARE = "DDSHARE";

    private static final String HTTP_ACQUISITIONSERVER_URL = "http://0.0.0.0:5555";
    private static final String HTTP_MANAGEMENTSERVER_URL = "http://0.0.0.0:5556";


    private final RaqetControl _raqetControll;
    private HttpServer _httpAcquisitionServer;
    private HttpServer _httpManagementServer;
    private ServerConfiguration _serverConfiguration;
    private final Properties _properties;


    public Main() throws IScsiException, DeviceContextException {
        _properties = readConfig();
        String machineIP;
        try {
            machineIP = Inet4Address.getLocalHost().getHostAddress();
        }
        catch (final Exception e) {
            LOG.error("Error determining local IP address", e);
            machineIP = "127.0.0.1";
        }
        final String serverIPAddress = _properties.getProperty("serverip", machineIP );
        final String evidenceDirectory = _properties.getProperty("evidencedirectory", EVIDENCE_DIRECTORY);
        final String initiatorName = _properties.getProperty("initiatorname", INITIATOR_NAME);

        final String vPNserverIPAddress = _properties.getProperty("vpnserveripaddress", "");
        final String vPNserversubnet = _properties.getProperty("vpnserversubnet", "");
        final String vPNclientUser = _properties.getProperty("vpnclientuser", "");
        final String vPNclientSecret = _properties.getProperty("vpnclientsecret", "");
        final String vPNserverUser = _properties.getProperty("vpnserveruser", "");
        final String vPNserverSecret = _properties.getProperty("vpnserversecret", "");

        LOG.info("Using " + serverIPAddress + " as server IP address");

        _raqetControll = new RaqetControl(initiatorName,
                                          serverIPAddress,
                                          vPNserverIPAddress,
                                          vPNserversubnet,
                                          vPNclientUser,
                                          vPNclientSecret,
                                          vPNserverUser,
                                          vPNserverSecret,
                                          new File(evidenceDirectory));
        _raqetControll.setOSPassword(_properties.getProperty("ospassword", UUID.randomUUID().toString()));
    }

    public Properties readConfig() {
        File propertieFile = new File("raqet.conf");
        if (!propertieFile.exists()) {
            propertieFile = new File("/etc/raqet.conf");
            if (!propertieFile.exists()) {
                return new Properties();
            }
        }
        final Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(propertieFile));
        }
        catch (final Exception e) {
            LOG.error("Error reading configfile", e);
        }

        return prop;
    }

    public void start() throws Exception {
        startSmbServer();
        startAcquisitionHttpServer();
        startManagementHttpServer();
    }

    public void stop() throws Exception {
        stopSmbServer();
        stopAcquisitionHttpServer();
        stopManagementHttpServer();
    }

    private void startSmbServer() throws Exception {
        final String smbHostname = _properties.getProperty("smbhostname", SMB_HOSTNAME);
        final String smbDomain = _properties.getProperty("smbdomain", SMB_DOMAIN);
        final String smbShare = _properties.getProperty("smbshare", SMB_SHARE);
        final String evidenceDirectory = _properties.getProperty("evidencedirectory", EVIDENCE_DIRECTORY);

        _serverConfiguration = new SmbServerConfiguration(smbHostname, smbDomain,
                                                          evidenceDirectory,
                                                          smbShare,
                                                          _raqetControll.getRaqetDiskDriver());

        _serverConfiguration.addServer(new NetBIOSNameServer(_serverConfiguration));
        _serverConfiguration.addServer(new SMBServer(_serverConfiguration));

        // Start the servers
        for (int i = 0; i < _serverConfiguration.numberOfServers(); i++) {
            _serverConfiguration.getServer(i).startServer();
        }

        LOG.info("JLAN server is running.");
    }

    private void stopSmbServer() throws Exception {
        for (int i = 0; i < _serverConfiguration.numberOfServers(); i++) {
            _serverConfiguration.getServer(i).shutdownServer(true);
        }
    }


    private void startAcquisitionHttpServer() throws Exception {
        final String httpAcquisitionUrl = _properties.getProperty("acquisitionurl", HTTP_ACQUISITIONSERVER_URL);
        final ResourceConfig config = new ResourceConfig().register(AcquisitionClientApi.class).register(new RaqetBinder(_raqetControll));
        _httpAcquisitionServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(httpAcquisitionUrl), config);
        _httpAcquisitionServer.start();
    }

    private void stopAcquisitionHttpServer() throws Exception {
        _httpAcquisitionServer.shutdownNow();
    }

    private void startManagementHttpServer() throws Exception {
        final String httpManagementServerUrl = _properties.getProperty("managementurl", HTTP_MANAGEMENTSERVER_URL);
        final ResourceConfig config = new ResourceConfig().register(RaqetService.class).register(new RaqetBinder(_raqetControll));
        final CLStaticHttpHandler staticHttpHandler = new CLStaticHttpHandler(this.getClass().getClassLoader(), "/static/");


        _httpManagementServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(httpManagementServerUrl), config);
        _httpManagementServer.getServerConfiguration().addHttpHandler(staticHttpHandler, "/gui/");
        /* Redirect users to the GUI */
        _httpManagementServer.getServerConfiguration().addHttpHandler(new HttpHandler() {
            @Override
            public void service(final Request request, final Response response) throws Exception {
                response.sendRedirect("/gui/");
            }
        }, "/index.html");
        _httpManagementServer.start();
    }

    private void stopManagementHttpServer() throws Exception {
        _httpManagementServer.shutdownNow();
    }

    public static Main runMain(final String[] args) throws Exception {
        LOG.info("Raqet version 0.1.3");
        if (args.length >= 1 && args[0].equals("-h")) {
            System.out.println("Syntax:  java -jar raqet.jar [-h] [[<username>:<password>@]<hostname>:<port>]");
            System.out.println();
            System.out.println("Example:");
            System.out.println("  java -jar raqet.jar");
            System.out.println("");
            System.out.println("Point browser to http://localhost:5556/gui/");
            System.out.println("Exact URL may be different based on content of raqet.conf");
            System.out.println("raqet.conf is searched in the working directory and secondly in /etc/raqet.conf");
            System.exit(1);
        }

        final URL resourceURL = Main.class.getResource("log4j.xml");
        if (resourceURL != null) {
            //TODO: Why is resource sometimes not available
            // RAQET-72
            DOMConfigurator.configure(resourceURL);
        }

        final Main main = new Main();
        main.start();

        LOG.info("Startup thread ended.");
        return main;
    }

    public static void main(final String[] args) throws Exception {
        runMain(args);
    }
}
