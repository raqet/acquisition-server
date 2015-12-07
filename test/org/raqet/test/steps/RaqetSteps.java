/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.test.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Composite;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.raqet.test.RaqetConfig;


public final class RaqetSteps {
    private final RaqetConfig _config;
    private byte[] _bytes;

    private enum AlternateMountPath {
        mount1, mount2, evidence
    };

//    AlternateMountPath _alternateMountPath;
    private Object _availabilityStatus;
    private BitSet _bitmap;
    private String _caseName = "";
    private String _computerName = "";

    static final Map<AlternateMountPath, File> SEARCHPATHS = new EnumMap<AlternateMountPath, File>(AlternateMountPath.class);
    //Static initializer to set all search paths
    static {
        SEARCHPATHS.put(AlternateMountPath.mount1, new File(RaqetConfig.MOUNTPATH));
        SEARCHPATHS.put(AlternateMountPath.mount2, new File(RaqetConfig.MOUNTPATH2));
        SEARCHPATHS.put(AlternateMountPath.evidence, new File(RaqetConfig.STORAGEPATH));
    }

    public RaqetSteps(final RaqetConfig config) {
        _config = config;
    }


    @Given("the acquisition server is running")
    public void acquisitionServerIsRunning() {
        assertTrue(_config.getServer().isRunning());
    }

    @Given("the client is configured for case $caseName")
    public void setCaseName(final String caseName) {
        _caseName = caseName;
    }

    @Given("the client is configured for computer $computerName")
    public void setComputerName(final String computerName) {
        _computerName = computerName;
    }

    //TODO: Either remove or repair
    @Given("a client $name with $number disks exported through an SMB share")
    @Alias("a client <name> with <number> disks exported through an SMB share")
    @Composite(steps = {
        "Given the acquisition server is running",
        "When a new client with name <name> is started",
        "Then the <number> disks of <name> are exported on a SMB share within 30 seconds"})
    public void dummy(@Named("name") final String name, @Named("number") final int number) {
    }

    @When("waiting for $secondsToWait seconds")
    public void sleep(final int secondsToWait) throws InterruptedException {
        Thread.sleep(secondsToWait * 1000);
    }

    @When("a new client with name $name is started")
    public void startNewClient(final String name) {
        _config.getClients().startNewClient(name);
    }

    @Then("a client $name is registered within $seconds seconds")
    public void clientIsRegistered(final String name, final int secondsToWait) {
        assertTrue(_config.getServer().clientHasRegistered(name));
    }

    private File[] getMatchingFiles(final AlternateMountPath mount, final String regexp) {
        final File base = SEARCHPATHS.get(mount);
        final File caseDir = new File(base, _caseName);
        final File dir = new File(caseDir, _computerName);

        final FilenameFilter filter = new RegexFileFilter(regexp);

        return dir.listFiles(filter);
    }

    @Then("the $number disks of $name are exported on a SMB share within $seconds seconds")
    public void clientDiskIsExported(final int number, final String name, final int secondsToWait) throws InterruptedException {
        sleep(secondsToWait);
        final File[] filelist = getMatchingFiles(AlternateMountPath.mount1, String.format(".*%s.*", name));
        final int found = filelist.length;
        for (final File file : filelist) {
            System.out.println(file.getName());
        }
        assertEquals("Number of SMB virtual files doesn't match disks", number, found);
    }

    public File getFileForClient(final AlternateMountPath mp, final int lun, final String name) throws IOException {
        File fileToUse = null;
        final String fileNameRegExp;
        if (mp != AlternateMountPath.evidence) {
            fileNameRegExp = String.format("virtualevidence-%04d-%s_.*.dd", lun, name);
        } else {
            fileNameRegExp = String.format("%04d-%s_.*.dd", lun, name);
        }
        final File[] files = getMatchingFiles(mp, fileNameRegExp);
        if (files.length > 0) {
            fileToUse = files[0];
        }

        assertTrue("Unable to find file for lun", fileToUse != null);
        return fileToUse;
    };

    public File getBitmapFileForClient(final int lun, final String name) throws IOException {
        File fileToUse = null;
        final File[] files = getMatchingFiles(AlternateMountPath.evidence, String.format("%04d-%s_.*.dd.bitmap", lun, name));
        if (files.length > 0) {
            fileToUse = files[0];
        }

        assertTrue("Unable to find bitmap file for lun", fileToUse != null);
        return fileToUse;
    };

    public byte[] readingLunOnClientWorker(final int number, final long offset, final File fileToUse) throws IOException {
        byte[] readbytes;
        final int nrOfBytesRead;
        try (final RandomAccessFile randomAccessFile = new RandomAccessFile(fileToUse, "r")) {
            randomAccessFile.seek(offset);
            readbytes = new byte[number];
            assertTrue("Unable to allocate buffer", readbytes != null);
            nrOfBytesRead = randomAccessFile.read(readbytes);
        }
        assertEquals("Read file ", number, nrOfBytesRead);
        return readbytes;
    }

    @When("reading $number bytes at offset $offset of disk $lun on client $name")
    public void readingLunOnClient(final int number, final long offset, final int lun, final String name) throws IOException {
        final File fileToUse = getFileForClient(AlternateMountPath.mount1, lun, name);
        _bytes = readingLunOnClientWorker(number, offset, fileToUse);
    }

    @When("reading $number bytes at offset $offset of disk $lun on the evidence storage of $name")
    public void readingLunOnEvidenceStore(final int number, final long offset, final int lun, final String name) throws IOException {
        final File fileToUse = getFileForClient(AlternateMountPath.evidence, lun, name);
        _bytes = readingLunOnClientWorker(number, offset, fileToUse);
    }


    @Then("the read bytes should contain \"$text\"")
    public void checkReadContent(final String text) throws UnsupportedEncodingException {
        assertEquals("Read content mismatch", text, new String(_bytes));
    }

    @Then("the read bytes should be zeros")
    public void checkReadZeros() throws UnsupportedEncodingException {
        boolean allzeros = true;
        for (int index = 0; index < _bytes.length; index++) {
            if (_bytes[index] != 0) {
                allzeros = false;
            }
        }
        assertTrue("Read content is not zeros", allzeros);
    }

    class ReadThread extends Thread {
        private final int _number;
        private final long _offset;
        private final File _fileToUse;
        protected byte[] _threadBytes;
        protected boolean _succes;

        ReadThread(final int number, final long offset, final File fileToUse) {
            _number = number;
            _offset = offset;
            _fileToUse = fileToUse;
            _succes = false;
        }

        @Override
        public void run() {
            try {
                _threadBytes = readingLunOnClientWorker(_number, _offset, _fileToUse);
                _succes = true;
            }
            catch (final IOException e) {
                // Fail without further error handling, success flag should be false
                e.printStackTrace();
            }
        };
    };

    @When("parallel reading with skips of $skip in $numberThread threads $number bytes at offset $offset  of disk $lun on client $name")
    public void readParallelWithSkip(final int skip, final int numberThread, final int number, final long offset, final int lun, final String name) throws IOException, InterruptedException {
        assertTrue("Minimum of 2 Threads", numberThread >= 2);
        int skipoffset = 0;
        final File[] filesToUse = new File[2];
        //Alternate between one virtual files on two mount points, it is not too clear
        //whether a single mount point will serialize the reads
        filesToUse[0] = getFileForClient(AlternateMountPath.mount1, lun, name);
        filesToUse[1] = getFileForClient(AlternateMountPath.mount2, lun, name);
        final ReadThread[] thread = new ReadThread[numberThread];
        for (int i = 0; i < numberThread; i++) {
            thread[i] = new ReadThread(number, offset + skipoffset, filesToUse[i % 2]);
            skipoffset = skipoffset + skip;
        }
        for (int i = 0; i < numberThread; i++) {
            thread[i].start();
        }
        for (int i = 0; i < numberThread; i++) {
            thread[i].join();
        }
        for (int i = 0; i < numberThread; i++) {
            assertTrue("Read thread should succeed", thread[i]._succes);
        }
        //Perhaps better to put the test in the @Then, but then we need to keep the state of all threads.
        if (skip == 0) {
            for (int i = 1; i < numberThread; i++) {
                assertTrue("Read thread should contain same data", Arrays.equals(thread[0]._threadBytes, thread[i]._threadBytes));
            }
        }
        _bytes = thread[0]._threadBytes;
    }

    @When("parallel reading in $numberThread threads $number bytes at offset $offset of disk $lun on client $name")
    public void readParallel(final int numberThread, final int number, final long offset, final int lun, final String name) throws IOException, InterruptedException {
        readParallelWithSkip(0, numberThread, number, offset, lun, name);
    }

    @Given("a readable evidence storagebitmap of disk $lun of $name")
    public void checkDataLunOnEvidenceStore(final int lun, final String name) throws IOException {
        final File file = getBitmapFileForClient(lun, name);
        final RandomAccessFile bitmapFile = new RandomAccessFile(file, "r");
        assertTrue("Unable to open bitmapfile", bitmapFile != null);
        final byte[] bitmapBuffer = new byte[(int) bitmapFile.length()];
        bitmapFile.readFully(bitmapBuffer);
        _bitmap = BitSet.valueOf(bitmapBuffer);
    }

    @When("checking availability of bytes at offset $offset in the evidence storagebitmap")
    public void checkDataLunOnEvidenceStore(final long offset) throws IOException {
        _availabilityStatus = _bitmap.get((int) (offset / 65536));
    }

    @When("checking availability of bytes from offset $offset1 until $offset2 in the evidence storagebitmap")
    public void checkDataLunOnEvidenceStoreRange(final long offset1, final long offset2) throws IOException {
        _availabilityStatus = _bitmap.get((int) (offset1 / 65536));
        for (long offset = offset1; offset <= offset2; offset = offset + 65536) {
            assertEquals(String.format("Availability status in range at offset %d must all be the same", offset), _availabilityStatus, _bitmap.get((int) (offset / 65536)));
        }
    }

    @Then("the availability status should be $availabilitystatus")
    public void checkDataLunOnEvidenceStoreStatus(final boolean availabilitystatus) {
        assertEquals("Status bitmap incorrect", _availabilityStatus, availabilitystatus);
    }

}