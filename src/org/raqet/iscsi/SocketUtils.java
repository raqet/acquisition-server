/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

import static nl.minvenj.nfi.raqet.jna.libc.CLibrary.LIBC;
import static nl.minvenj.nfi.raqet.jna.libc.CLibrary.POLLPRI;
import static nl.minvenj.nfi.raqet.jna.libws2.Ws2Library.LIBWS2;

import org.apache.log4j.Logger;

import com.sun.jna.Platform;

import nl.minvenj.nfi.raqet.jna.libc.Pollfd;
import nl.minvenj.nfi.raqet.jna.libws2.WsaPollfd;

final class SocketUtils {
    private static final Logger LOG = Logger.getLogger(SocketUtils.class);

    static int poll(final Pollfd pfd, final int nfds, final int timeout) {
        int ready;
        if (Platform.isWindows()) {
            final WsaPollfd wpfd = new WsaPollfd();
            wpfd.fd = pfd.fd;
            wpfd.events = (short) (pfd.events & ~POLLPRI); // WSAPoll() doesn't support 'POLLPRI' according to the MSDN
            wpfd.revents = pfd.revents;

            ready = LIBWS2.WSAPoll(wpfd, nfds, timeout);
            LOG.debug(String.format("WSAPoll(%s, %d, %d); result=%d", wpfd, nfds, timeout, ready));

            pfd.revents = wpfd.revents;
        }
        else {
            ready = LIBC.poll(pfd, nfds, timeout);
            LOG.debug(String.format("poll(%s, %d, %d); result=%d", pfd, nfds, timeout, ready));
        }

        return ready;
    }
}