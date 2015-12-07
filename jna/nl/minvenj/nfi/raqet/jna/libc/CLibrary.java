/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libc;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public interface CLibrary extends Library {
    CLibrary LIBC = Platform.isWindows() ? null : (CLibrary) Native.loadLibrary("c", CLibrary.class);;

    // constants defined in 'bits/poll.h'
    int POLLIN = 0x001;
    int POLLPRI = 0x002;
    int POLLOUT = 0x004;
    int POLLERR = 0x008;
    int POLLHUP = 0x010;
    int POLLNVAL = 0x020;
    int POLLRDNORM = 0x040;
    int POLLRDBAND = 0x080;
    int POLLWRNORM = 0x100;
    int POLLWRBAND = 0x200;

    int poll(Pollfd pfd, int nfds, int timeout);
}