/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

import java.io.Closeable;
import java.io.IOException;

public interface BlockReader extends Closeable {

    Block retrieveBlock(int blockNumber) throws IOException;
}