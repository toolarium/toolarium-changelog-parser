/*
 * IChangelogParser.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.parser;

import com.github.toolarium.changelog.dto.ChangelogReleaseVersion;
import java.io.IOException;
import java.nio.file.Path;


/**
 * Defines the change-log parser interface which implements a change-log parser following https://keepachangelog.com.
 * 
 * @author patrick
 */
public interface IChangelogParser {
    /**
     * Parse a change-log 
     * 
     * @param filename the filename
     * @return the change-log parse result
     * @throws IOException In case of an i/o error
     */
    ChangelogParseResult parse(Path filename) throws IOException;

    
    /**
     * Parse a change-log content
     * 
     * @param inputContent the change-log content to parse
     * @return the change-log parse result
     */
    ChangelogParseResult parse(String inputContent);
    
    
    /**
     * Parse a change-log content
     * 
     * @param inputContent the change-log content to parse
     * @return the change-log parse result
     */
    ChangelogParseResult parseContent(String inputContent);
    

    /**
     * Parse a version
     *
     * @param inputVersion the version to parse following the Semantic Versioning (https://semver.org/spec/v2.0.0.html).
     * @return the parsed version
     */
    ChangelogReleaseVersion parseVersion(String inputVersion);
}