/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.smb;

import org.alfresco.jlan.debug.DebugConfigSection;
import org.alfresco.jlan.server.auth.CifsAuthenticator;
import org.alfresco.jlan.server.auth.LocalAuthenticator;
import org.alfresco.jlan.server.auth.UserAccountList;
import org.alfresco.jlan.server.auth.acl.DefaultAccessControlManager;
import org.alfresco.jlan.server.config.CoreServerConfigSection;
import org.alfresco.jlan.server.config.GlobalConfigSection;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.SecurityConfigSection;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.DiskDeviceContext;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FilesystemsConfigSection;
import org.alfresco.jlan.server.filesys.SrvDiskInfo;
import org.alfresco.jlan.smb.server.CIFSConfigSection;
import org.springframework.extensions.config.element.GenericConfigElement;

public class SmbServerConfiguration extends ServerConfiguration {
    private static final int[] DEFAULT_MEMORY_POOL_PKT_SIZES = {256, 4096, 16384, 66000};
    private static final int[] DEFAULT_MEMORY_POOL_INIT_ALLOC = {20, 20, 5, 5};
    private static final int[] DEFAULT_MEMORY_POOL_MAX_ALLOC = {100, 50, 50, 50};
    /** For debugging it can be useful to use sufficiently high ports as it will not require root permissions. */
    private static final boolean HIGHPORT_SMB = false;

    public SmbServerConfiguration(final String hostname, final String domainName, final String localPath, final String shareName, final DiskInterface diskDriver) throws InvalidConfigurationException, DeviceContextException {
        super(hostname);

        setServerName(hostname);

        createDebugConfigSection(this);
        createCoreServerConfigSection(this);
        createGlobalConfigSection(this);
        createCifsConfigSection(this, hostname, domainName);

        final SecurityConfigSection securityConfigSection = createSecurityConfigSection(this);
        final FilesystemsConfigSection filesystemsConfigSection = createFilesystemsConfigSection(this);

        // Create and start disk share
        final DiskSharedDevice diskShare = createDiskShare(localPath, shareName, diskDriver, securityConfigSection);
        diskShare.setConfiguration(this);
        diskShare.setAccessControlList(securityConfigSection.getGlobalAccessControls());

        ((DiskDeviceContext) diskShare.getContext()).startFilesystem(diskShare);

        filesystemsConfigSection.addShare(diskShare);
    }

    private static void createDebugConfigSection(final ServerConfiguration serverConfiguration) throws InvalidConfigurationException {
        final DebugConfigSection debugConfigSection = new DebugConfigSection(serverConfiguration);

        final GenericConfigElement params = new GenericConfigElement("params");
        final GenericConfigElement logLevelConfigElement = new GenericConfigElement("logLevel");
        logLevelConfigElement.setValue("Info");
        params.addChild(logLevelConfigElement);

        debugConfigSection.setDebug("org.raqet.smb.Log4jLoggingDebug", params);

    }

    private static void createCoreServerConfigSection(final ServerConfiguration serverConfiguration) throws InvalidConfigurationException {
        final CoreServerConfigSection coreServerConfigSection = new CoreServerConfigSection(serverConfiguration);
        coreServerConfigSection.setMemoryPool(DEFAULT_MEMORY_POOL_PKT_SIZES, DEFAULT_MEMORY_POOL_INIT_ALLOC, DEFAULT_MEMORY_POOL_MAX_ALLOC);
        coreServerConfigSection.setThreadPool(25, 50);
    }

    private static void createGlobalConfigSection(final ServerConfiguration serverConfiguration) {
        @SuppressWarnings("unused")
        final GlobalConfigSection globalConfigSection = new GlobalConfigSection(serverConfiguration);
    }

    private static void createCifsConfigSection(final ServerConfiguration serverConfiguration, final String hostname, final String domainName) throws InvalidConfigurationException {
        final CIFSConfigSection cifsConfigSection = new CIFSConfigSection(serverConfiguration);
        cifsConfigSection.setServerName(hostname);
        cifsConfigSection.setDomainName(domainName);
        cifsConfigSection.setHostAnnounceInterval(5);
        cifsConfigSection.setHostAnnouncer(true);
        cifsConfigSection.setAuthenticator(createCifsAuthenticatior(serverConfiguration));
        cifsConfigSection.setTcpipSMB(true);
        if (HIGHPORT_SMB) {
            cifsConfigSection.setTcpipSMBPort(1025);
            cifsConfigSection.setSessionPort(1026);
        }
    }

    private static CifsAuthenticator createCifsAuthenticatior(final ServerConfiguration serverConfiguration) throws InvalidConfigurationException {
        final CifsAuthenticator authenticator = new LocalAuthenticator();
        authenticator.setAllowGuest(true);
        authenticator.setAccessMode(CifsAuthenticator.USER_MODE);
        authenticator.initialize(serverConfiguration, new GenericConfigElement("params"));
        return authenticator;
    }

    private static SecurityConfigSection createSecurityConfigSection(final ServerConfiguration serverConfiguration) throws InvalidConfigurationException {
        final SecurityConfigSection securityConfigSection = new SecurityConfigSection(serverConfiguration);
        securityConfigSection.setAccessControlManager(createAccessControlManager(serverConfiguration));
//      securityConfigSection.setJCEProvider("cryptix.jce.provider.CryptixCrypto");
        securityConfigSection.setUserAccounts(new UserAccountList());
        return securityConfigSection;
    }

    private static DefaultAccessControlManager createAccessControlManager(final ServerConfiguration serverConfiguration) throws InvalidConfigurationException {
        final DefaultAccessControlManager accessControlManager = new DefaultAccessControlManager();
        accessControlManager.initialize(serverConfiguration, new GenericConfigElement("params"));
        return accessControlManager;
    }

    private static FilesystemsConfigSection createFilesystemsConfigSection(final ServerConfiguration serverConfiguration) {
        return new FilesystemsConfigSection(serverConfiguration);
    }

    private static DiskSharedDevice createDiskShare(final String localPath, final String shareName, final DiskInterface diskDriver, final SecurityConfigSection securityConfigSection) throws DeviceContextException {
        final GenericConfigElement args = new GenericConfigElement("args");
        final GenericConfigElement localPathConfig = new GenericConfigElement("LocalPath");
        localPathConfig.setValue(localPath);
        args.addChild(localPathConfig);

        final DiskDeviceContext diskDeviceContext = (DiskDeviceContext) diskDriver.createContext(shareName, args);
        diskDeviceContext.setShareName(shareName);
        diskDeviceContext.setConfigurationParameters(args);
        diskDeviceContext.enableChangeHandler(false);
        diskDeviceContext.setDiskInformation(new SrvDiskInfo(2560000, 64, 512, 2304000)); // Default to a 80Gb sized disk with 90% free space

        return new DiskSharedDevice(shareName, diskDriver, diskDeviceContext);
    }
}
