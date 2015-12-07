/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.test;

import org.raqet.test.testobjects.ClientsTester;
import org.raqet.test.testobjects.ServerTester;

public final class RaqetConfig {
    public static final String MOUNTPATH = "/tmp/raqettest";
    public static final String MOUNTPATH2 = "/tmp/raqettest2";
    public static final String STORAGEPATH = "/var/raqet";


    private final ServerTester _server;
    private final ClientsTester _clients;

    public RaqetConfig() {
        _server = new ServerTester();
        _clients = new ClientsTester();
    }

    public ServerTester getServer() {
        return _server;
    }

    public ClientsTester getClients() {
        return _clients;
    }
}