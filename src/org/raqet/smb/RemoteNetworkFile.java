/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.smb;

import java.io.IOException;

import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.server.filesys.cache.LocalFileState;
import org.alfresco.jlan.server.filesys.cache.NetworkFileStateInterface;
import org.alfresco.jlan.smb.SeekType;
import org.raqet.remote.RemoteDeviceInfo;
import org.raqet.remote.RemoteDeviceManager;

import nl.minvenj.nfi.streams.inputdata.InputData;

final class RemoteNetworkFile extends NetworkFile implements NetworkFileStateInterface {
    private final RemoteDeviceManager _remoteDeviceManager;
    private final RemoteDeviceInfo _remoteDeviceInfo;

    private InputData _inputData;
    private FileState _state;

    RemoteNetworkFile(final String name, final FileInfo fileInfo, final RemoteDeviceManager remoteDeviceManager, final RemoteDeviceInfo remoteDeviceInfo) {
        super(name);

        _remoteDeviceManager = remoteDeviceManager;
        _remoteDeviceInfo = remoteDeviceInfo;

        setFileSize(fileInfo.getSize());
        setModifyDate(fileInfo.getModifyDateTime());
        setCreationDate(fileInfo.getCreationDateTime());

        final String path = fileInfo.getPath();
        if (path != null) {
            setFileId(path.hashCode());
            setFullName(path);
        }
    }

    @Override
    public void closeFile() throws java.io.IOException {
        if (_inputData != null) {
            _inputData.close();
            _inputData = null;

            _state = null;

            setClosed(true);
        }
    }

    public long currentPosition() {
        if (_inputData == null) {
            return 0L;
        }

        return _inputData.getPosition();
    }

    @Override
    public void flushFile() {
        // Ignored.
    }

    public boolean isEndOfFile() {
        if (_inputData == null) {
            return false;
        }

        return (_inputData.getPosition() == _inputData.getSize());
    }

    @Override
    public void openFile(final boolean createFlag) throws IOException {
        if (_inputData == null) {
            _inputData = _remoteDeviceManager.openDevice(_remoteDeviceInfo);

            setClosed(false);
        }
    }

    @Override
    public synchronized int readFile(final byte[] buf, final int len, final int pos, final long fileOff) throws IOException {
        if (_inputData == null) {
            openFile(false);
        }

        if (fileOff > _inputData.getSize()) {
            //Trying to read beyond file
            return 0;
        }
        _inputData.seek(fileOff);
        return _inputData.read(buf, pos, len);
    }

    @Override
    public long seekFile(final long pos, final int typ) throws IOException {
        if (_inputData == null) {
            openFile(false);
        }

        switch (typ) {
            case SeekType.StartOfFile:
                _inputData.seek(pos);
                break;

            case SeekType.CurrentPos:
                _inputData.seek(_inputData.getPosition() + pos);
                break;

            case SeekType.EndOfFile:
                _inputData.seek(_inputData.getSize() + pos);
                break;
        }

        return currentPosition();
    }

    @Override
    public void truncateFile(final long siz) {
        // Ignored.
    }

    public void writeFile(final byte[] buf, final int len, final int pos) {
        // Ignored.
    }

    @Override
    public void writeFile(final byte[] buf, final int len, final int pos, final long offset) {
        // Ignored.
    }

    @Override
    public FileState getFileState() {
        if (_state == null) {
            // TODO: (see RAQET-36) Create a RemoteFileState class that properly tracks the state of remote files.
            _state = new LocalFileState(getFullName(), false);
        }
        return _state;
    }
}