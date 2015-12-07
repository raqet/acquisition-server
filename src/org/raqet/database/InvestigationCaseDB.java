/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The InvestigationCase DB is a simple filesystem mapping.
 * The keys in the database are foldernames
 * The content of the database is stored in json formatted files
 *
 *  The Database should (for now?) act directly on the filesystem without too much caching.
 *
 *  The filesystem database data is mixed with data blobs such as the disk images
 *  and log files.
 *
 *  This allows for easy archival and restore of cases.
 * @author schramp
 *
 */
public class InvestigationCaseDB {
    private static final Logger LOG = Logger.getLogger(InvestigationCaseDB.class);

    private static final String INVESTIGATIONENTRYNAME = "InvestigationCaseDbEntry.json";
    private static final String COMPUTERENTRYNAME = "ComputerDbEntry.json";
    private final File _baseDbPath;



    public InvestigationCaseDB(final File basePath) {
        _baseDbPath = basePath;
    }

    public void createCase(final InvestigationCaseDbEntry investigationCaseDbEntry) throws Exception {
        final File caseDirectory = new File(_baseDbPath, investigationCaseDbEntry.getUrlCaseName());
        if (caseDirectory.exists()) {
            throw new Exception("Duplicate key, insert failed to create Case");
        }
        caseDirectory.mkdir();
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(caseDirectory, INVESTIGATIONENTRYNAME), investigationCaseDbEntry);
    }

    public InvestigationCaseDbEntry getCase(final String urlcaseName) throws JsonParseException, JsonMappingException, IOException {
        final File caseDirectory = new File(_baseDbPath, urlcaseName);
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(caseDirectory, INVESTIGATIONENTRYNAME), InvestigationCaseDbEntry.class);
    }

    public List<String> getCases() {
        final List<String> caseList = new ArrayList<String>();
        for (final File file : _baseDbPath.listFiles()) {
            if (file.isDirectory()) {
                if (new File(file, INVESTIGATIONENTRYNAME).exists()) {
                    caseList.add(file.getName());
                }
            }
        }
        return caseList;
    }

    public void createComputer(final String urlcaseName, final ComputerDbEntry computerDbEntry) throws Exception {
        final InvestigationCaseDbEntry investigationCaseDbEntry = getCase(urlcaseName);
        final File caseDirectory = new File(_baseDbPath, investigationCaseDbEntry.getUrlCaseName());
        final File computerDirectory = new File(caseDirectory, computerDbEntry.getUrlComputerName());
        if (computerDirectory.exists()) {
            throw new Exception("Duplicate key, insert failed to create Computer");
        }
        computerDirectory.mkdir();
        computerDbEntry.setBaseStorageFolder(computerDirectory);
        computerDbEntry.setCaseAndComputer(urlcaseName + "\\" + computerDbEntry.getUrlComputerName());
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(computerDirectory, COMPUTERENTRYNAME), computerDbEntry);
    }

    public List<String> getComputers(final String urlcaseName) {
        final List<String> computerList = new ArrayList<String>();
        for (final File file : new File(_baseDbPath, urlcaseName).listFiles()) {
            if (file.isDirectory()) {
                if (new File(file, COMPUTERENTRYNAME).exists()) {
                    computerList.add(file.getName());
                }
            }
        }
        return computerList;
    }

    public ComputerDbEntry getComputer(final String urlcaseName, final String urlcomputerName) throws JsonParseException, JsonMappingException, IOException {
        final File caseDirectory = new File(_baseDbPath, urlcaseName);
        final File computerDirectory = new File(caseDirectory, urlcomputerName);
        final ObjectMapper mapper = new ObjectMapper();
        final ComputerDbEntry computerDbEntry = mapper.readValue(new File(computerDirectory, COMPUTERENTRYNAME), ComputerDbEntry.class);
        computerDbEntry.setBaseStorageFolder(computerDirectory);
        computerDbEntry.setCaseAndComputer(urlcaseName + "\\" + computerDbEntry.getUrlComputerName());
        return computerDbEntry;
    }

    public static String escapeName(final String name) {
        final String pattern = "[^a-zA-Z0-9_\\.]";
        final String replacement = "";
        return name.replaceAll(pattern, replacement).toLowerCase();
    }

    public ComputerDbEntry findClient(final String clientid, final String hardwareId) {
        for (final String caseName : getCases()) {
            for (final String computerName : getComputers(caseName)) {
                try {
                    final ComputerDbEntry computer = getComputer(caseName, computerName);
                    if (computer.getClientId().equals(clientid) && computer.getHardwareId().equals(hardwareId)) {
                        return computer;
                    }
                }
                catch (final IOException e) {
                    LOG.info("Failed to read data for " + caseName + " " + computerName);
                }
                finally {

                }
            }
        }
        return null;
    }

    public void update(final ComputerDbEntry computerDbEntry) throws Exception {
        assert (computerDbEntry.getBaseStorageFolder() != null);
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(computerDbEntry.getBaseStorageFolder(), COMPUTERENTRYNAME), computerDbEntry);
    }
}
