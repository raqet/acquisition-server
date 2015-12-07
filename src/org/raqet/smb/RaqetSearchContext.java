/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.smb;

import static com.javaforge.reifier.check.ArgumentChecker.argNotNull;

import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.server.filesys.pseudo.PseudoSearchContext;

public class RaqetSearchContext extends PseudoSearchContext {
    private final SearchContext _searchContext;

    public RaqetSearchContext(final SearchContext searchContext, final String path) {
        super(path);

        _searchContext = argNotNull("searchContext", searchContext);
    }

    @Override
    public int getResumeId() {
        return super.getResumeId() + _searchContext.getResumeId();
    }

    @Override
    public boolean hasMoreFiles() {
        if (hasMorePseudoFiles()) {
            return true;
        }

        return _searchContext.hasMoreFiles();
    }

    @Override
    public boolean nextFileInfo(final FileInfo info) {
        if (hasMorePseudoFiles()) {
            return nextPseudoFileInfo(info);
        }

        return _searchContext.nextFileInfo(info);
    }

    @Override
    public String nextFileName() {
        if (hasMorePseudoFiles()) {
            return nextPseudoFileName();
        }

        return _searchContext.nextFileName();
    }

    @Override
    public boolean restartAt(final int resumeId) {
        if (restartAtPseudoFile(resumeId)) {
            return true;
        }

        return _searchContext.restartAt(resumeId - super.getResumeId());
    }

    @Override
    public boolean restartAt(final FileInfo info) {
        if (restartAtPseudoFile(info)) {
            return true;
        }

        return _searchContext.restartAt(info);
    }
}