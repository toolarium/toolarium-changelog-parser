/*
 * ChangelogFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog;

import com.github.toolarium.changelog.config.ChangelogConfig;
import com.github.toolarium.changelog.dto.Changelog;
import com.github.toolarium.changelog.exception.ValidationException;
import com.github.toolarium.changelog.formatter.ChangelogFormatter;
import com.github.toolarium.changelog.parser.ChangelogParseResult;
import com.github.toolarium.changelog.parser.ChangelogParser;
import com.github.toolarium.changelog.validator.ChangelogValidator;
import java.io.IOException;
import java.nio.file.Path;
import jptools.util.ByteArray;


/**
 * The change-log factory
 * 
 * @author patrick
 */
public final class ChangelogFactory {
    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static class HOLDER {
        static final ChangelogFactory INSTANCE = new ChangelogFactory();
    }

    
    /**
     * Constructor
     */
    private ChangelogFactory() {
        // NOP
    }

    
    /**
     * Get the instance
     *
     * @return the instance
     */
    public static ChangelogFactory getInstance() {
        return HOLDER.INSTANCE;
    }

    
    /**
     * Parse a change-log
     *
     * @param filename the filename
     * @return the parser result
     * @throws IOException In case of an I/O error to read the file
     */
    public ChangelogParseResult parse(Path filename) throws IOException {
        return new ChangelogParser().parse(filename);
    }

    
    /**
     * Parse a change-log
     *
     * @param content the content to parse
     * @return the parser result
     * @throws IOException In case of an I/O error to read the file
     */
    public ChangelogParseResult parse(String content) throws IOException {
        if (content == null) {
            throw new IOException("Invalid content!");
        }
        
        return new ChangelogParser().parseContent(new ByteArray(content));
    }


    /**
     * Validate a change-log
     *
     * @param changelogConfiguration the change-log validation configuration
     * @param filename the filename
     * @throws IOException In case of an I/O error to read the file
     * @throws ValidationException In case of a validation error. It includes also parsing errors.
     */
    public void validate(ChangelogConfig changelogConfiguration, Path filename) throws IOException, ValidationException {
        new ChangelogValidator(changelogConfiguration).validate(filename);
    }

    
    /**
     * Validate a change-log
     *
     * @param changelogConfiguration the change-log validation configuration
     * @param filename the filename
     * @param projectName the reference project name or null
     * @param description the reference description or null
     * @param version the reference version which should be the newest one or null
     * @throws IOException In case of an I/O error to read the file
     * @throws ValidationException In case of a validation error. It includes also parsing errors.
     */
    public void validate(ChangelogConfig changelogConfiguration, Path filename, String projectName, String description, String version) throws IOException, ValidationException {
        new ChangelogValidator(changelogConfiguration).validate(filename, projectName, description, version);
    }


    /**
     * Validate a change-log
     *
     * @param changelogConfiguration the change-log validation configuration
     * @param changelog the change-log
     * @throws IOException In case of an I/O error to read the file
     * @throws ValidationException In case of a validation error
     */
    public void validate(ChangelogConfig changelogConfiguration, Changelog changelog) throws IOException, ValidationException {
        new ChangelogValidator(changelogConfiguration).validate(changelog);
    }

    
    /**
     * Validate a change-log
     *
     * @param changelogConfiguration the change-log validation configuration
     * @param changelog the change-log
     * @param projectName the reference project name or null
     * @param description the reference description or null
     * @param version the reference version which should be the newest one or null
     * @throws IOException In case of an I/O error to read the file
     * @throws ValidationException In case of a validation error
     */
    public void validate(ChangelogConfig changelogConfiguration, Changelog changelog, String projectName, String description, String version) throws IOException, ValidationException {
        new ChangelogValidator(changelogConfiguration).validate(changelog, projectName, description, version);
    }


    /**
     * Format a change-log
     *
     * @param changelogConfiguration the change-log configuration
     * @param filename the filename
     * @return the formatted change-log
     * @throws IOException In case of an I/O error to read the file
     */
    public String format(ChangelogConfig changelogConfiguration, Path filename) throws IOException {
        return format(changelogConfiguration, parse(filename).getChangelog());
    }


    /**
     * Format a change-log
     *
     * @param changelogConfiguration the change-log configuration
     * @param changelog the change-log
     * @return the formatted change-log
     * @throws IOException In case of an I/O error to read the file
     */
    public String format(ChangelogConfig changelogConfiguration, Changelog changelog) throws IOException {
        return new ChangelogFormatter(changelogConfiguration).format(changelog);
    }
}
