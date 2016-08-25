/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.http;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.jlan.server.core.DeviceContextException;
import org.apache.log4j.Logger;
import org.raqet.ClientInfo;
import org.raqet.clientapi.DiskInfoDeviceLink;
import org.raqet.clientapi.DiskInfoMessage;
import org.raqet.database.ComputerDbEntry;
import org.raqet.database.InvestigationCaseDB;
import org.raqet.database.InvestigationCaseDbEntry;
import org.raqet.iscsi.IScsiException;
import org.raqet.iscsi.IScsiLogicalUnitCapacity;
import org.raqet.iscsi.TargetAddress;
import org.raqet.remote.RemoteDeviceInfo;
import org.raqet.remote.RemoteDeviceManager;
import org.raqet.smb.RaqetDiskDriver;
import org.raqet.util.FileUtilities;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;


public class RaqetControl {
    private static final Logger LOG = Logger.getLogger(RaqetControl.class);

    private final RemoteDeviceManager _remoteDeviceManager;
    private final InvestigationCaseDB _investigationCaseDB;
    private final RaqetDiskDriver _diskInterface;

    //TODO: Get a more serious implementation, now only a stub for testing
    private final List<ClientInfo> _clientList = new ArrayList<>();

    private final String _serverIPAddress;
    private final String _VPNserverIPAddress;
    private final String _VPNserversubnet;
    private final String _VPNclientUser;
    private final String _VPNclientSecret;
    private final String _VPNserverUser;
    private final String _VPNserverSecret;

    /** the password embedded in the live OS. The default value "secret" will be overwritten.
     */
    private String _osPassword = "secret";

    /** TODO: Move a lot of state from Main to this class, for now just reference Main.
     * @param evidenceDirectory */

    public RaqetControl(final String initiatorName,
                        final String serverIPAdress,
                        final String vPNserverIPAddress,
                        final String vPNserversubnet,
                        final String vPNclientUser,
                        final String vPNclientSecret,
                        final String vPNserverUser,
                        final String vPNserverSecret,
                        final File evidenceDirectory) throws IScsiException {
        _remoteDeviceManager = new RemoteDeviceManager(initiatorName, evidenceDirectory);
        _diskInterface = new RaqetDiskDriver();
        _serverIPAddress = serverIPAdress;
        _VPNserverIPAddress = vPNserverIPAddress;
        _VPNserversubnet = vPNserversubnet;
        _VPNclientUser = vPNclientUser;
        _VPNclientSecret = vPNclientSecret;
        _VPNserverUser = vPNserverUser;
        _VPNserverSecret = vPNserverSecret;

        _investigationCaseDB = new InvestigationCaseDB(evidenceDirectory);
        // Create a default case to create auto generated Computer entry based on clients
        // that could be started multiple times on different hardware
        try {
            createInvestigationCase(new InvestigationCase("defaultcase"));
        }
        catch (final Exception e) {
            // This is an expected exception as normally the default case allready exists
        }
    }

    public RaqetDiskDriver getRaqetDiskDriver() {
        return _diskInterface;
    }

    public RemoteDeviceManager getRemoteDeviceManager() {
        return _remoteDeviceManager;
    }

    private void writeLogging(final ComputerDbEntry computer, final List<String> logging) {
        final File baseFolder = computer.getBaseStorageFolder();
        try (PrintWriter logfile = new PrintWriter(new BufferedWriter(new FileWriter(new File(baseFolder, "clientlogfile.txt"), true)))) {
            for (final String logline : logging) {
                logfile.print(logline);
            }
        }
        catch (final IOException e) {
            LOG.error("Failed to write client logfile in " + baseFolder.toString(), e);
        }

    }

    public void addClient(final ClientInfo target) throws IScsiException, DeviceContextException {
        final int index = _clientList.indexOf(target);
        if (index >= 0) {
            _clientList.get(index).seen();
            LOG.info("Ignoring reregistration of existing client \"" + target.getClientid() + "\" (" + target.getHardwareid() + ")");
            return;

        }
        _clientList.add(target);
        /* Try to find the most specific incantation of the computer */
        ComputerDbEntry computer = _investigationCaseDB.findClient(target.getClientid(), target.getHardwareid());

        /* When a computer has not been matched, try to match without hardwareId */
        if (computer == null) {
            computer = _investigationCaseDB.findClient(target.getClientid(), "");
            if (computer != null) {
                /* set the hardwareId */
                computer.setHardwareId(target.getHardwareid());
                try {
                    _investigationCaseDB.update(computer);
                }
                catch (final Exception e) {
                    LOG.error("Failed to create update computer for client" + target.getClientid(), e);
                    return;
                }
            }
        }
        if (computer == null) {
            computer = new ComputerDbEntry();
            computer.setHardwareId(target.getHardwareid());
            computer.setClientId(target.getClientid());
            computer.setComputerName(target.getClientid() + "_" + target.getHardwareid());
            computer.setUrlcomputerName(target.getClientid() + "_" + target.getHardwareid());
            try {
                _investigationCaseDB.createComputer("defaultcase", computer);
            }
            catch (final Exception e) {
                LOG.error("Failed to create default computer");
                return;
            }
        }
        target.setComputerDbEntry(computer);

        writeLogging(computer, target.getLogging());

        addRemoteDevice(new TargetAddress(target.getIpAddress() + ":3260"), target);
    }

    /**
     * splits a formatted ISCSI target strings in seperate elements and calls addRemoteDevice
     *
     * @param target [[<username>:<password>@]<hostname>:<port>]
     * @throws IScsiException
     * @throws DeviceContextException
     */
    public void addRemoteDevice(final String target) throws IScsiException, DeviceContextException {
        addRemoteDevice(new TargetAddress(target), null);
    }

    private static String generateEvidenceFilename(final String targetName, final int lun) {
        // Escape illegal characters (':' on Windows platform) in the iSCSI target name
        return String.format("%s_%04d.dd", targetName.replace(':', '_'), lun);
    }

    /**
     * discoveres LUNs on a remote ISCSI target. Each lun immediately published as
     *
     * @param targetAddress ISCSI portal address
     * @param clientInfo Meta information from registration
     * @throws IScsiException
     * @throws DeviceContextException
     */

    public void addRemoteDevice(final TargetAddress targetAddress, final ClientInfo clientInfo) throws IScsiException, DeviceContextException {
        // Determine remote devices

        final List<RemoteDeviceInfo> deviceList = _remoteDeviceManager.listTargets(targetAddress);
        if (deviceList.isEmpty()) {
            LOG.warn("Failed to determine remote devices, ignoring remote device " + targetAddress);
            return;
        }

        // Log remote devices
        LOG.info("Remote devices at '" + targetAddress + "':");

        for (final RemoteDeviceInfo device : deviceList) {
            String smbMountPath = "\\";
            device.setEvidenceDirectory(new File("/tmp/"));
            device.setEvidenceBaseFileName(generateEvidenceFilename(device.getTargetName(), device.getLun()));

            LOG.info(" o  " + device.getTargetName());
            LOG.info("     - LUN:  " + device.getLun());
            LOG.info("     - Type: " + device.getDeviceType());
            LOG.info("     - URL:  " + device.getPortal());

            final IScsiLogicalUnitCapacity capacity = device.getCapacity();
            final long size = (capacity.getLogicalBlockAddress() + 1) * capacity.getBlockSize();
            LOG.info("     - Size: " + FileUtilities.toUserFriendlyByteString(size));
            LOG.info("     - Blk:  " + capacity.getBlockSize());
            if (clientInfo != null) {
                final String deviceName = clientInfo.getDeviceMapping(device.getLun()).getDevice();
                LOG.info("     - Dev   " + deviceName);
                smbMountPath = "\\" + clientInfo.getComputerDbEntry().getCaseAndComputer() + "\\";
                final List<String> disks = clientInfo.getComputerDbEntry().getDisks();

                if (!disks.contains(deviceName)) {
                    disks.add(deviceName);
                }
                try {
                    _investigationCaseDB.update(clientInfo.getComputerDbEntry());
                }
                catch (final Exception e) {
                    LOG.error("Failed to create update computer for client " + clientInfo.getClientid(), e);
                    return;
                }

                device.setEvidenceDirectory(clientInfo.getComputerDbEntry().getBaseStorageFolder());
                device.setEvidenceBaseFileName(String.format("%04d-", device.getLun()) +
                    clientInfo.getClientid() + "_" + deviceName + ".dd");
            }
            LOG.info("Published on path " + smbMountPath);
            _diskInterface.addRemoteDevice(_remoteDeviceManager, smbMountPath, device);
        }
    }

    public List<InvestigationCase> getInvestigationCases() {
        final List<InvestigationCase> listInvestigationCases = new ArrayList<InvestigationCase>();
        for (final String urlcaseName : _investigationCaseDB.getCases()) {
            InvestigationCaseDbEntry investigationCaseDbEntry;
            try {
                investigationCaseDbEntry = _investigationCaseDB.getCase(urlcaseName);
                final InvestigationCase investigationCase = new InvestigationCase(investigationCaseDbEntry.getCaseName());
                investigationCase.setUrlCaseName(investigationCaseDbEntry.getUrlCaseName());
                listInvestigationCases.add(investigationCase);
            }
            catch (final Exception e) {
                LOG.error("Failed to read case information", e);
            }
        }
        return listInvestigationCases;
    }

    public void createInvestigationCase(final InvestigationCase investigationCase) throws Exception {
        final InvestigationCaseDbEntry investigationCaseDbEntry = new InvestigationCaseDbEntry();
        investigationCaseDbEntry.setUrlCaseName(investigationCase.getUrlCaseName());
        investigationCaseDbEntry.setCaseName(investigationCase.getCaseName());
        _investigationCaseDB.createCase(investigationCaseDbEntry);
    }

    public InvestigationCase getInvestigationCase(final String caseName) throws JsonParseException, JsonMappingException, IOException {
        final InvestigationCase investigationCase = new InvestigationCase();
        final InvestigationCaseDbEntry investigationCaseDbEntry = _investigationCaseDB.getCase(caseName);
        investigationCase.setUrlCaseName(investigationCaseDbEntry.getUrlCaseName());
        investigationCase.setCaseName(investigationCaseDbEntry.getCaseName());
        return investigationCase;
    }

    public List<Computer> getComputers(final String investigationCase) {
        final List<Computer> listComputers = new ArrayList<Computer>();
        for (final String computerName : _investigationCaseDB.getComputers(investigationCase)) {
            LOG.info("Looking up computer " + computerName + " for case " + investigationCase);
            try {
                final ComputerDbEntry computerDbEntry = _investigationCaseDB.getComputer(investigationCase, computerName);
                final Computer computer = new Computer(computerDbEntry.getComputerName(), computerDbEntry.getUrlComputerName());
                listComputers.add(computer);
            }
            catch (final Exception e) {
                LOG.error("Fail to read computer information", e);
            }
        }
        return listComputers;
    }

    public void createComputer(final String investigationCase, final Computer computer) throws Exception {
        final ComputerDbEntry computerDbEntry = new ComputerDbEntry();
        final String urlComputerName = InvestigationCaseDB.escapeName(computer.getUrlComputerName());
        computerDbEntry.setUrlcomputerName(urlComputerName);
        computerDbEntry.setComputerName(computer.getComputerName());
        computerDbEntry.setClientId(computer.getClientId());
        _investigationCaseDB.createComputer(investigationCase, computerDbEntry);
    }

    public Computer getComputer(final String caseName, final String computerName) throws JsonParseException, JsonMappingException, IOException {
        final Computer computer = new Computer();
        final ComputerDbEntry computerDbEntry = _investigationCaseDB.getComputer(caseName, computerName);
        /* Computernames and Casenames can be used in filesystem paths, better make them clean */
        final String urlComputerName = InvestigationCaseDB.escapeName(computerDbEntry.getUrlComputerName());
        computer.setUrlComputerName(urlComputerName);
        computer.setComputerName(computerDbEntry.getComputerName());
        computer.setClientId(computerDbEntry.getClientId());
        return computer;
    }

    public List<String> getDisks(final String caseName, final String computerName) throws JsonParseException, JsonMappingException, IOException {
        final ComputerDbEntry computerDbEntry = _investigationCaseDB.getComputer(caseName, computerName);
        final List<String> disks = computerDbEntry.getDisks();

        return disks;
    }

    public void diskInformation(final ClientInfo clientInfo, final DiskInfoMessage target) {
        final int index = _clientList.indexOf(clientInfo);
        if (index >= 0) {
            final ClientInfo activeClientInfo = _clientList.get(index);
            activeClientInfo.seen();
            final ComputerDbEntry computerDbEntry = activeClientInfo.getComputerDbEntry();
            final File basePath = computerDbEntry.getBaseStorageFolder();
            for (final DiskInfoDeviceLink link : target.getDiskInformation()) {
                final File diskInfoFilePath = new File(basePath, link.getDevice() + ".txt");
                if (!diskInfoFilePath.exists()) {
                    try (final PrintWriter diskInfoFile = new PrintWriter(diskInfoFilePath, "UTF-8")) {
                        diskInfoFile.print(link.getDiskinfo().toString());
                    }
                    catch (final Exception e) {
                        LOG.error("Failed to write disk information for " + clientInfo.getClientid(), e);
                    }
                }
            }
            return;
        }
        LOG.info("Ignoring diskInformation of unregistered client \"" + clientInfo.getClientid() + "\" (" + clientInfo.getHardwareid() + ")");
    }

    public File getGetAcquisitionSystem(final String caseName, final String computerName,final String imageType) throws JsonParseException, JsonMappingException, IOException, InterruptedException {
        final ComputerDbEntry computerDbEntry = _investigationCaseDB.getComputer(caseName, computerName);
        final String clientID = computerDbEntry.getClientId();
//        ./authoring/raqet -p secret --clientid testingclient --server http://192.168.13.1:5555 \
//        --output build/testbuild.iso --target iso \
//        --kernel ./openwrt/bin/x86/openwrt-x86-generic-ramfs.bzImage \
//        --initrdextra build/initrd.img --plainiscsiinitiator 192.168.13.1/32
        final File temp = File.createTempFile("raqetos_", ".iso.tmp");
        final String path = temp.getAbsolutePath();

        final ProcessBuilder pb;
        if (_VPNserverIPAddress == "") {
            pb = new ProcessBuilder("raqet", "-p", _osPassword,
                                                     "--clientid", clientID,
                                                     "--server", "http://" + _serverIPAddress + ":5555",
                                                     "--output", path,
                                                     "--target", imageType,
                                                     "--plainiscsiinitiator", _serverIPAddress + "/32"
                                                               );
        } else {
            pb = new ProcessBuilder("raqet", "-p", _osPassword,
                                    "--clientid", clientID,
                                    "--server", "http://" + _serverIPAddress + ":5555",
                                    "--output", path,
                                    "--target", imageType,
                                    "--vpnserveraddress", _VPNserverIPAddress,
                                    "--vpnserversubnet", _VPNserversubnet,
                                    "--vpnclientuser", _VPNclientUser,
                                    "--vpnclientsecret", _VPNclientSecret,
                                    "--vpnserveruser", _VPNserverUser,
                                    "--vpnserversecret", _VPNserverSecret
                );
        }


        pb.directory(new File("/tmp/"));
        pb.inheritIO();
        final Process process = pb.start();
        process.waitFor();
        return temp;
    }

    public void setOSPassword(final String osPassword) {
        _osPassword = osPassword;
    }

}
