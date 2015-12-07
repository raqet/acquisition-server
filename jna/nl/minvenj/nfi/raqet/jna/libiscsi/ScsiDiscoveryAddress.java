/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.raqet.jna.libiscsi;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public final class ScsiDiscoveryAddress extends Structure implements Structure.ByReference {
    public ScsiDiscoveryAddress next;
    public String target_name;
    public ScsiTargetPortal portals;

    public ScsiDiscoveryAddress(final Pointer p) {
        super(p);

        read();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List getFieldOrder() {
        return Arrays.asList("next", "target_name", "portals");
    }

    public static class ByReference extends com.sun.jna.ptr.ByReference {
        public ByReference() {
            super(Native.POINTER_SIZE);
        }

        public ScsiDiscoveryAddress getValue() {
            return new ScsiDiscoveryAddress(getPointer());
        }

        @Override
        public String toString() {
            return toNative().toString().replace("native@", ScsiDiscoveryAddress.class.getSimpleName() + "@");
        }
    }
}