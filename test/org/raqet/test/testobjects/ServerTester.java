/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.test.testobjects;

import org.raqet.Main;

public final class ServerTester {
    boolean _running;

    final ThreadGroup _serverThreads = new ThreadGroup("ServerThreads");
    Main _server;


    public boolean isRunning() {
        return _server != null;
    }

    public void start() throws Exception {
        final String[] args = new String[0];
        _server = Main.runMain(args);
    }

    public void stop() throws Exception {
        _server.stop();
        _server = null;
        System.out.println("java server stopped");
    }

    public boolean clientHasRegistered(final String name) {
        //TODO
        return name.equals("testclient");
    }
}