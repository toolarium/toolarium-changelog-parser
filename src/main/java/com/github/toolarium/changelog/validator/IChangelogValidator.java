/*
 * IChangelogValidator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.validator;

import com.github.toolarium.changelog.dto.Changelog;
import java.io.IOException;
import java.nio.file.Path;


/**
 * Defines the change-log validator.
 * 
 * @author patrick
 */
public interface IChangelogValidator {
    
    /**
     * Validate 
     * 
     * @param filename the filename
     * @return the validated change-log
     * @throws ValidationException the validation exception
     * @throws IOException In case of an I/O error
     */
    Changelog validate(Path filename) throws ValidationException, IOException;

    
    /**
     * Validate 
     * 
     * @param filename the filename
     * @param projectName the reference project name or null
     * @param description the reference description or null
     * @param version the reference version which should be the newest one or null
     * @return the validated change-log
     * @throws ValidationException the validation exception
     * @throws IOException In case of an I/O error
     */
    Changelog validate(Path filename, String projectName, String description, String version) throws ValidationException, IOException;
    
    
    /**
     * Validate 
     * 
     * @param changelog the change-log to validate
     * @return the validated change-log
     * @throws ValidationException the validation exception
     */
    Changelog validate(Changelog changelog) throws ValidationException;
    
    
    /**
     * Validate 
     * 
     * @param changelog the change-log to validate
     * @param projectName the reference project name or null
     * @param description the reference description or null
     * @param inputVersion the reference version which should be the newest one or null
     * @return the validated change-log
     * @throws ValidationException the validation exception
     */
    Changelog validate(Changelog changelog, String projectName, String description, String inputVersion) throws ValidationException;
}
