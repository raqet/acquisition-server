/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.smb;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.alfresco.jlan.debug.Debug;
import org.alfresco.jlan.debug.DebugInterfaceBase;
import org.alfresco.jlan.smb.server.SMBServer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public final class Log4jLoggingDebug extends DebugInterfaceBase {

    private static final Logger LOG = Logger.getLogger(SMBServer.class);
    private static final Map<Integer, Level> LOG4J_LEVELS = new HashMap<>();

    private final AtomicReference<String> _printBuffer;

    static {
        LOG4J_LEVELS.put(Debug.Debug, Level.DEBUG);
        LOG4J_LEVELS.put(Debug.Info, Level.INFO);
        LOG4J_LEVELS.put(Debug.Warn, Level.WARN);
        LOG4J_LEVELS.put(Debug.Error, Level.ERROR);
        LOG4J_LEVELS.put(Debug.Fatal, Level.FATAL);
    }

    public Log4jLoggingDebug() {
        _printBuffer = new AtomicReference<String>("");
    }

    @Override
    public void debugPrint(final String str, final int level) {
        if ((level <= getLogLevel()) && (str != null)) {
            String s;
            do {
                s = _printBuffer.get();
            } while (!_printBuffer.compareAndSet(s, s + str));
        }
    }

    @Override
    public void debugPrintln(final String str, final int level) {
        if ((level <= getLogLevel()) && (str != null)) {
            final Level log4jLogLevel = LOG4J_LEVELS.get(level);
            if (log4jLogLevel != null) {
                LOG.log(log4jLogLevel, _printBuffer.getAndSet("") + str);
            }
        }
    }

    @Override
    public void debugPrintln(final Exception ex, final int level) {
        if (level <= getLogLevel()) {
            final Level log4jLogLevel = LOG4J_LEVELS.get(level);
            if (log4jLogLevel != null) {
                LOG.log(log4jLogLevel, "", ex);
            }
        }
    }
}