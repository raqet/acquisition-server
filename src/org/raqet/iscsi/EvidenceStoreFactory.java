/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EvidenceStoreFactory {
    private final Map<String, EvidenceStore> _evidenceStores;

    public EvidenceStoreFactory() {
        _evidenceStores = new HashMap<String, EvidenceStore>();
    }

    public synchronized EvidenceStore getEvidenceStore(final File evidenceFile, final long length, final int blockSize) throws IOException {
        if (_evidenceStores.containsKey(evidenceFile.getPath())) {
            final EvidenceStore evidenceStore = _evidenceStores.get(evidenceFile.getPath());
            evidenceStore.incRefCount();
            return evidenceStore;
        }
        else {
            final EvidenceStore evidenceStore = new EvidenceStore(this, evidenceFile, length, blockSize);
            evidenceStore.incRefCount();
            _evidenceStores.put(evidenceFile.getPath(), evidenceStore);
            return evidenceStore;
        }

    }

    public synchronized void closeEvidenceStore(final EvidenceStore evidenceStore) throws IOException {
        //TODO: This sync to disk is not convenient, it could block relatively long.
        //Must find a better way to tackle the race-condition on close/reopen and multiple closes.
        evidenceStore.synctodisk();
        if (0 == evidenceStore.decRefCount()) {
            evidenceStore.doclose();
            _evidenceStores.remove(evidenceStore.getPath());
        }
    }

}
