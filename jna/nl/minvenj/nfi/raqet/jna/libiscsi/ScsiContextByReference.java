/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libiscsi;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

public final class ScsiContextByReference extends com.sun.jna.ptr.ByReference {
    public ScsiContextByReference() {
        super(Native.POINTER_SIZE);
    }

    public ScsiContextByReference(final Pointer p) {
        this();

        super.setPointer(p);
    }

    @Override
    public String toString() {
        return toNative().toString().replace("native@", "ScsiContext@");
    }
}