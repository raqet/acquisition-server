/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.test.steps;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.ScenarioType;
import org.raqet.test.RaqetConfig;


public final class BeforeAfterSteps {
    private final RaqetConfig _config;

    public BeforeAfterSteps(final RaqetConfig config) {
        _config = config;
    }

    private void deleteOldAcquiredData() throws IOException {
        FileUtils.deleteDirectory(new File(RaqetConfig.STORAGEPATH));
    }

    private void makeMountpoint(final String name) {
        final File f = new File(name);
        f.mkdir();
    }

    private void mountSMB(final String name) throws IOException, InterruptedException {
        makeMountpoint(name);
        final Process p = Runtime.getRuntime().exec("mount.cifs -o sec=ntlmv2,nocase,password=\"\"  //127.0.0.1/ddshare/ " + name);
        p.waitFor();
        assertTrue(p.exitValue() == 0);
    }

    private void umountSMB(final String mountName) throws IOException, InterruptedException {
        final Process p = Runtime.getRuntime().exec("umount " + mountName);
        p.waitFor();
        assertTrue(p.exitValue() == 0);
    }

    @BeforeScenario(uponType = ScenarioType.ANY)
    public void beforeScenario() throws Throwable {
        deleteOldAcquiredData();
        _config.getServer().start();
        Thread.sleep(1000);
        mountSMB(RaqetConfig.MOUNTPATH);
        mountSMB(RaqetConfig.MOUNTPATH2);
    }

    @AfterScenario(uponType = ScenarioType.ANY)
    public void afterScenario() throws Exception {
        umountSMB(RaqetConfig.MOUNTPATH2);
        umountSMB(RaqetConfig.MOUNTPATH);
        _config.getServer().stop();
        Thread.sleep(1000);
    }
}