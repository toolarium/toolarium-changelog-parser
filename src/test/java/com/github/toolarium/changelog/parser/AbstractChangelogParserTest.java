/*
 * AbstractChangelogParserTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.changelog.ChangelogFactory;
import com.github.toolarium.changelog.config.ChangelogConfig;
import com.github.toolarium.changelog.dto.Changelog;
import com.github.toolarium.changelog.dto.ChangelogErrorList;
import java.io.IOException;
import java.nio.file.Path;
import jptools.resource.FileCacheManager;
import jptools.util.ByteArray;


/**
 * Abstract change-log parser test class 
 * 
 * @author patrick
 */
public abstract class AbstractChangelogParserTest {
    /**
     * Assert change-log
     * 
     * @param filename the filename
     * @param errorMessageList the error message list
     * @return the change-log
     * @throws IOException In case of I/O error
     */
    protected Changelog assertChangelogFile(Path filename, ChangelogErrorList errorMessageList) throws IOException {
        ChangelogConfig changelogConfig = new ChangelogConfig();
        return assertChangelog(changelogConfig, readContent(filename), parseFile(filename), errorMessageList);
    }

    
    /**
     * Assert change-log
     * 
     * @param filename the filename
     * @param changelogConfig the change-log configuration
     * @param errorMessageList the error message list
     * @return the change-log
     * @throws IOException In case of I/O error
     */
    protected Changelog assertChangelogFile(ChangelogConfig changelogConfig, Path filename, ChangelogErrorList errorMessageList) throws IOException {
        return assertChangelog(changelogConfig, readContent(filename), parseFile(filename), errorMessageList);
    }

    
    /**
     * Assert change-log
     * 
     * @param content the content
     * @param changelogConfig the change-log configuration
     * @param errorMessageList the error message list
     * @return the change-log
     * @throws IOException In case of I/O error
     */
    protected Changelog assertChangelog(ChangelogConfig changelogConfig, String content, ChangelogErrorList errorMessageList) throws IOException {
        return assertChangelog(changelogConfig, content, ChangelogFactory.getInstance().parse(content), errorMessageList);
    }
    
    
    /**
     * Assert change-log
     * 
     * @param changelogConfig the change-log configuration
     * @param content the content
     * @param changelogParseResult the parse result
     * @param errorMessageList the error message list
     * @return the change-log
     * @throws IOException In case of I/O error
     */
    protected Changelog assertChangelog(ChangelogConfig changelogConfig, String content, ChangelogParseResult changelogParseResult, ChangelogErrorList errorMessageList) throws IOException {
        assertEquals(content, format(changelogConfig, changelogParseResult.getChangelog()));
        if (errorMessageList == null || errorMessageList.isEmpty()) {
            assertTrue(changelogParseResult.getChangelogErrorList().isEmpty(), "Expected no errors: " + changelogParseResult.getChangelogErrorList());
        } else {
            assertEquals(errorMessageList, changelogParseResult.getChangelogErrorList());
        }

        assertEquals(content, format(changelogConfig, changelogParseResult.getChangelog()));
        return changelogParseResult.getChangelog();
    }


    /**
     * Format change-log
     * 
     * @param changelogConfig the change-log configuration
     * @param changelog the change-loh to format
     * @return the formated change-log
     * @throws IOException In case of an I/O error
     */
    protected String format(ChangelogConfig changelogConfig, Changelog changelog) throws IOException {
        return ChangelogFactory.getInstance().format(changelogConfig, changelog);
    }

    
    /**
     * Parse a change-log
     * 
     * @param filename the filename
     * @return the parsed change-log
     * @throws IOException In case of an I/O error
     */
    protected ChangelogParseResult parseFile(Path filename) throws IOException {
        return ChangelogFactory.getInstance().parse(filename);
    }

        
    /**
     * Read the change-log raw content
     * 
     * @param filename the filename
     * @return the raw content
     * @throws IOException In case of an I/O error
     */
    protected String readContent(Path filename) throws IOException {
        ByteArray content = (ByteArray) new FileCacheManager().getFile(filename.toString());
        return content.toString().replace("\r", "");
    }
}
