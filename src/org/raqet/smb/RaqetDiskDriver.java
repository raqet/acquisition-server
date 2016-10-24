/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.smb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.FileStatus;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.server.filesys.pseudo.MemoryPseudoFile;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFile;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFileList;
import org.alfresco.jlan.server.filesys.pseudo.PseudoNetworkFile;
import org.alfresco.jlan.smb.server.disk.JavaFileDiskDriver;
import org.apache.log4j.Logger;
import org.raqet.remote.RemoteDeviceInfo;
import org.raqet.remote.RemoteDeviceManager;


public class RaqetDiskDriver extends JavaFileDiskDriver implements DiskInterface {
	private static final Logger LOG = Logger.getLogger(RaqetDiskDriver.class);
    private final Map<String, PseudoFileList> _pseudoFileLists;

    public RaqetDiskDriver() {
        _pseudoFileLists = new HashMap<>();

        // TODO: (see RAQET-36) Remove hack for bug in PseudoFileList when reimplementing the disk driver.
        existingPseudoFileList("\\");
    }

    public void addRemoteDevice(final RemoteDeviceManager remoteDeviceManager, final String folder, final RemoteDeviceInfo remoteDevice) {
        final PseudoFileList pseudoFileList = existingPseudoFileList(folder);
        pseudoFileList.addFile(new RemotePseudoFile(remoteDeviceManager, remoteDevice));
    }

    private PseudoFileList existingPseudoFileList(final String path) {
        final PseudoFileList pseudoFileList = _pseudoFileLists.get(path);
        if (pseudoFileList != null) {
            return pseudoFileList;
        }

        final PseudoFileList newPseudoFileList = new PseudoFileList();
        _pseudoFileLists.put(path, newPseudoFileList);

        // TODO: (see RAQET-36) Remove hack for bug in PseudoFileList when reimplementing the disk driver.
        newPseudoFileList.addFile(new MemoryPseudoFile("123.txt", new byte[]{1, 2, 3}));

        return newPseudoFileList;
    }

    @Override
    public void closeFile(final SrvSession sess, final TreeConnection tree, final NetworkFile file) throws IOException {
        if (file instanceof PseudoNetworkFile) {
            file.close();
        }
        else {
            super.closeFile(sess, tree, file);
        }
    }

    @Override
    public int fileExists(final SrvSession sess, final TreeConnection tree, final String name) {
        if (findPseudoFile(name) != null) {
            return FileStatus.FileExists;
        }

        return super.fileExists(sess, tree, name);
    }

    @Override
    public FileInfo getFileInformation(final SrvSession sess, final TreeConnection tree, final String name) throws IOException {
        final PseudoFile pseudoFile = findPseudoFile(name);
        if (pseudoFile != null) {
            return pseudoFile.getFileInfo();
        }

        return super.getFileInformation(sess, tree, name);
    }

    @Override
    public NetworkFile openFile(final SrvSession sess, final TreeConnection tree, final FileOpenParams params) throws IOException {
        if (params != null) {
            final String path = params.getPath();
            final PseudoFile pseudoFile = findPseudoFile(path);
            if (pseudoFile != null) {
                return pseudoFile.getFile(path);
            }
        }

        return super.openFile(sess, tree, params);
    }

    private PseudoFile findPseudoFile(final String path) {
        if (path == null) {
            return null; // No path specified
        }
        final String folder;
        final int folderpart = path.lastIndexOf("\\");
        if (folderpart >= 0) {
            folder = path.substring(0, folderpart + 1);
        }
        else {
            folder = "\\";
        }

        final PseudoFileList pseudoFileList = _pseudoFileLists.get(folder);
        if (pseudoFileList == null) {
            return null; // No pseudo files for this folder
        }

        final String filename = path.substring(folder.length());
        return pseudoFileList.findFile(filename, false);
    }

    @Override
    public SearchContext startSearch(final SrvSession sess, final TreeConnection tree, final String searchPath, final int attrib) throws FileNotFoundException {
    	SearchContext searchContext;
    	try {
    		searchContext = super.startSearch(sess, tree, searchPath, attrib);
    	} catch (FileNotFoundException exception) {
    		searchContext = null;
    	}
        
        int lastSlash = searchPath.lastIndexOf("\\");
        if (lastSlash < 1) {
        	return searchContext;
        }
        final String path = searchPath.substring(0, lastSlash+1);
        final String pattern = searchPath.substring(lastSlash+1);
        final RaqetSearchContext raqetSearchContext = new RaqetSearchContext(searchContext, path);
        
        final PseudoFileList pseudoFileList = _pseudoFileLists.get(path);
        if (pseudoFileList == null) {
        	return searchContext;
        }
        
        if (pattern.equals("*")) {
            raqetSearchContext.setPseudoFileList(pseudoFileList);
        } else {
            final PseudoFileList newPseudoFileList = new PseudoFileList();
            newPseudoFileList.addFile(new MemoryPseudoFile("123.txt", new byte[]{1, 2, 3}));
        	for (int i=0; i < pseudoFileList.numberOfFiles(); i++) {
        		PseudoFile pseudoFile = pseudoFileList.getFileAt(i);
        		if (pseudoFile.getFileName().equals(pattern)) {
        			newPseudoFileList.addFile(pseudoFile);
        		} else {
        		}
        	}
        	if ((searchContext == null) && 
        		(newPseudoFileList.numberOfFiles() == 1)) {
        		throw new FileNotFoundException();
        	}
            raqetSearchContext.setPseudoFileList(newPseudoFileList);
        }
        

        return raqetSearchContext;
    }
}