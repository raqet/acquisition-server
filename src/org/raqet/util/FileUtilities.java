/*
 * Copyright (c) 2009-2012, 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.util;

import static com.javaforge.reifier.check.ArgumentChecker.argNotNegative;

import java.io.Closeable;
import java.io.IOException;

/**
 * Provides miscellaneous utility methods applicable to files and other types
 * of data sources or destinations.
 */
public final class FileUtilities {
    // Constructor to prevent instantiation of utility class
    private FileUtilities() {
    }

    /**
     * Closes the given {@code resource} if not {@code null}.
     * <p>
     * The following code illustrates how to use this method.
     * If the constructor of {@code FileOutputStream} fails, the object
     * {@code out} will be {@code null} and no resource will be closed.
     * <pre>
     * OutputStream out = null;
     * try {
     *     out = new FileOutputStream(myFile);
     *     out.write(bytes);
     * }
     * finally {
     *     closeResource(out);
     * }
     * </pre>
     *
     * @param resource the resource to close, which can be {@code null}
     */
    public static void closeResource(final Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            }
            catch (final IOException e) {
                // How to handle such errors?
            }
        }
    }

    /**
     * Converts a number of {@code bytes} to a user-friendly text representation.
     * <p>
     * For example, {@code 2938274582L} will be represented as {@code 2.7G}.
     * 
     * @param bytes the number of bytes
     * @return the user-friendly text describing the number of {@code bytes}
     * 
     * @throws IllegalArgumentException if {@code bytes} is negative
     */
    public static String toUserFriendlyByteString(final long bytes) {
        argNotNegative("bytes", bytes);

        if (bytes < 1024L) {
            return Long.toString(bytes);
        }

        long integer = bytes;
        for (final char suffix : "kMGTP".toCharArray()) {
            final long fraction = (((integer * 10L) / 1024L) % 10L);
            integer /= 1024L;
            if (integer < 1024L) {
                return String.format("%d.%d%c", integer, fraction, suffix);
            }
        }

        // Last suffix, which is 'E' for exabyte
        final long fraction = (((integer * 10L) / 1024L) % 10L);
        integer /= 1024L;
        return String.format("%d.%dE", integer, fraction);
    }
}
