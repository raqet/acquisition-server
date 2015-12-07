/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libws2;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public interface Ws2Library extends Library {
    Ws2Library LIBWS2 = Platform.isWindows() ? (Ws2Library) Native.loadLibrary("ws2_32.dll", Ws2Library.class) : null;

    int WSAPoll(WsaPollfd fdarray, long nfds, int timeout);
}