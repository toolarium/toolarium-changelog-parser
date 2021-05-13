/*
 * ChangelogValidator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.validator;

import com.github.toolarium.changelog.ChangelogFactory;
import com.github.toolarium.changelog.config.ChangelogConfig;
import com.github.toolarium.changelog.dto.Changelog;
import com.github.toolarium.changelog.dto.ChangelogChangeType;
import com.github.toolarium.changelog.dto.ChangelogEntry;
import com.github.toolarium.changelog.dto.ChangelogSection;
import com.github.toolarium.changelog.exception.ValidationException;
import com.github.toolarium.changelog.parser.ChangelogParseResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jptools.parser.ParseException;
import jptools.util.version.Version;


/**
 * Change-log validator 
 * 
 * @author patrick
 */
public class ChangelogValidator {
    private ChangelogConfig changelogConfig;

    
    /**
     * Constructor for ChangelogValidator
     */
    public ChangelogValidator() {
        this(new ChangelogConfig());
    }

    
    /**
     * Constructor for ChangelogValidator
     * 
     * @param changelogConfig the change-log configuration for formatting
     */
    public ChangelogValidator(ChangelogConfig changelogConfig) {
        this.changelogConfig = changelogConfig;
    }

    
    /**
     * Validate 
     * 
     * @param filename the filename
     * @throws ValidationException the validation exception
     * @throws IOException In case of an I/O error
     */
    public void validate(Path filename) throws ValidationException, IOException {
        validate(filename, null, null, null);
    }

    
    /**
     * Validate 
     * 
     * @param filename the filename
     * @param projectName the reference project name or null
     * @param description the reference description or null
     * @param version the reference version which should be the newest one or null
     * @throws ValidationException the validation exception
     * @throws IOException In case of an I/O error
     */
    public void validate(Path filename, String projectName, String description, String version) throws ValidationException, IOException {
        List<String> errorMessageList = new ArrayList<String>();

        try {
            ChangelogParseResult result = ChangelogFactory.getInstance().parse(filename);
            addList(errorMessageList, result.getErrorMessageList());

            validate(result.getChangelog(), projectName, description, version);
        } catch (ValidationException e) {
            addList(errorMessageList, e.getValidationErrorList());
            throw new ValidationException(e.getMessage(), errorMessageList);
        }

        if (errorMessageList != null && !errorMessageList.isEmpty()) {
            throw new ValidationException("Changelog validation errors.", errorMessageList);
        }
    }

    
    /**
     * Validate 
     * 
     * @param changelog the change-log to validate
     * @throws ValidationException the validation exception
     */
    public void validate(Changelog changelog) throws ValidationException {
        validate(changelog, null, null, null);
    }
    
    
    /**
     * Validate 
     * 
     * @param changelog the change-log to validate
     * @param projectName the reference project name or null
     * @param description the reference description or null
     * @param inputVersion the reference version which should be the newest one or null
     * @throws ValidationException the validation exception
     */
    public void validate(Changelog changelog, String projectName, String description, String inputVersion) throws ValidationException {
        List<String> errorMessageList = new ArrayList<String>();

        Version version = convertVersion(errorMessageList, inputVersion);

        if (changelog == null) {
            addError(errorMessageList, "Changelog", "Invalid changelog!");
        } else {
            addList(errorMessageList, validateHeader(changelog, projectName, description));
            addList(errorMessageList, validateEntries(changelog.getEntries(), version));
            addList(errorMessageList, validateUnreleasdEntry(changelog));
        }

        if (errorMessageList.isEmpty()) {
            return;
        }
        
        throw new ValidationException("Changelog validation errors.", errorMessageList);
    }


    /**
     * Convert input version
     * 
     * @param errorMessageList the error list
     * @param inputVersion the input version
     * @return the converted version
     */
    protected Version convertVersion(List<String> errorMessageList, String inputVersion) {
        Version version = null;
        if (inputVersion != null) {
            if (!"Unreleased".equals(inputVersion.trim())) {
                try {
                    version = new Version(inputVersion.trim());
                } catch (ParseException e) {
                    addError(errorMessageList, "Reference", "Invalid reference version " + version);
                }
            }
        }
        return version;
    }


    /**
     * Validate the header
     * 
     * @param changelog the change-log
     * @param projectName the expected project name
     * @param description the expected description
     * @return the error list
     */
    protected List<String> validateHeader(Changelog changelog, String projectName, String description) {
        List<String> errorMessageList = new ArrayList<String>();
        if (changelog == null) {
            return errorMessageList;
        }

        if (projectName != null && !projectName.isBlank()) {
            if (!changelog.getProjectName().equals(projectName)) {
                addError(errorMessageList, "Header", "The name don't correspond to [" + projectName + "], current [" + changelog.getProjectName() + "]!");
            }
        }

        if (description != null && !description.isBlank()) {
            if (!changelog.getDescription().equals(description)) {
                addError(errorMessageList, "Header", "The don't correspond to [" + description + "]!");
            }
        }

        return errorMessageList;
    }

    
    /**
     * Validate the header
     * 
     * @param entries the change-log entries
     * @param version the reference version or null
     * @return the error list
     */
    protected List<String> validateEntries(List<ChangelogEntry> entries, Version version) {
        List<String> errorMessageList = new ArrayList<String>();
        if (entries == null || entries.isEmpty()) {
            addError(errorMessageList, "Entries", "Missing changelog entries!");
            return errorMessageList;
        }

        // validate if version exists
        errorMessageList.addAll(validateVersionExist(entries, version));

        // validate sort order
        errorMessageList.addAll(validateEntryOrder(entries, version));

        // validate entries
        for (ChangelogEntry entry : entries) {
            addList(errorMessageList, validateEntry(entry));
        }
        
        return errorMessageList;
    }


    /**
     * Validate the unreleased entry
     * 
     * @param changelog the change-log
     * @return the error list
     */
    protected List<String> validateUnreleasdEntry(Changelog changelog) {
        List<String> errorMessageList = new ArrayList<String>();
        if (!changelogConfig.isSupportUnreleased()) {
            try {
                if (changelog.getEntry(null) != null) {
                    addError(errorMessageList, "Unreleased", "The unreleased section is not supported!");
                }
            } catch (ParseException e) {
                // NOP
            }
        }

        return errorMessageList;
    }
    
    
    /**
     * Validate entry order
     * 
     * @param entries the entries
     * @param version the version
     * @return the error list
     */
    protected List<String> validateEntryOrder(List<ChangelogEntry> entries, Version version) {
        List<String> errorMessageList = new ArrayList<String>();
        if (entries == null || entries.isEmpty()) {
            return errorMessageList;
        }

        // create sorted reference list
        List<ChangelogEntry> sortedEntries = createSortedChangelogEntryReferenceList(entries);
        if (version != null && sortedEntries != null && !sortedEntries.isEmpty()) {
            for (ChangelogEntry entry : sortedEntries) {
                if (entry.getReleaseVersion() != null && entry.getReleaseVersion().compareTo(version) > 0) {
                    addError(errorMessageList, "" + entry.getReleaseVersion(), "Newer version in than " + version + " in changelog found!");
                }
            }
        }

        // check sort order
        boolean printedSortError = false;
        int firstIndex = 0;
        for (int i = 0; i < entries.size(); i++) {
            ChangelogEntry entry = entries.get(i);

            if (i == 0 && !entry.isReleased() && entry.getReleaseVersion() == null) {
                firstIndex++;
            }

            if (version != null && i == firstIndex) {
                if (entry.getReleaseVersion().compareTo(version) != 0) {
                    addError(errorMessageList, "" + entry.getReleaseVersion(), "The version " + version + " should be referenced as first entry.");
                }
            }

            if (!printedSortError && !entry.equals(sortedEntries.get(i))) {
                addError(errorMessageList, "" + entry.getReleaseVersion(), "Not valid sorted!");
                printedSortError = true;
            }
        }

        return errorMessageList;
    }

        
    /**
     * Validate entry order
     * 
     * @param entries the entries
     * @param searchVersion the version to check
     * @return the error list
     */
    protected List<String> validateVersionExist(List<ChangelogEntry> entries, Version searchVersion) {
        List<String> errorMessageList = new ArrayList<String>();
        if (searchVersion == null) {
            return errorMessageList;
        }
        
        int found = 0;
        for (ChangelogEntry entry : entries) {
            if (!entry.isReleased() && searchVersion == null) {
                found++;
            } else if (entry.getReleaseVersion() != null && entry.getReleaseVersion().equals(searchVersion)) {
                found++;
            }
        }

        if (found == 0) {
            addError(errorMessageList, "" + searchVersion, "Could not find version " + searchVersion + "!");
        } else if (found > 1) {
            addError(errorMessageList, "" + searchVersion, "Found " + found + " times the same version " + searchVersion + "!");
        }

        return errorMessageList;
    }
        
    /**
     * Validate entry order
     * 
     * @param entry the entry
     * @return the error list
     */
    protected List<String> validateEntry(ChangelogEntry entry) {
        List<String> errorMessageList = new ArrayList<String>();
        if (entry == null) {
            addError(errorMessageList, "Entries", "Invalid empty entry!");
            return errorMessageList;
        }

        if (!changelogConfig.isSupportReleaseInfo() && entry.getInfo() != null && !entry.getInfo().isBlank()) {
            addError(errorMessageList, "" + entry.getReleaseVersion(), "Additional release information is not supported: " + entry.getInfo() + "!");
        }

        errorMessageList.addAll(validateText("" + entry.getReleaseVersion(), entry.getDescription()));
        errorMessageList.addAll(validateChangelogSections(entry.getReleaseVersion(), entry.getSectionList()));
        return errorMessageList;
    }

    
    /**
     * Validate entry order
     * 
     * @param releaseVersion the release version
     * @param sectionList the section list
     * @return the error list
     */
    protected List<String> validateChangelogSections(Version releaseVersion, List<ChangelogSection> sectionList) {
        List<String> errorMessageList = new ArrayList<String>();
        if (sectionList == null) {
            addError(errorMessageList, "Entries", "Invalid empty section!");
            return errorMessageList;
        }

        Set<ChangelogChangeType> changeLogChangeTypeSet = new HashSet<>(Arrays.asList(ChangelogChangeType.values()));
        for (ChangelogSection section : sectionList) {
            if (section.getChangeType() == null) {
                
                // addError( errorMessageList, "" + releaseVersion, "Duplicate type " + changeType + "!" );
            } else if (!changeLogChangeTypeSet.remove(section.getChangeType())) {
                addError(errorMessageList, "" + releaseVersion, "Duplicate type " + section.getChangeType().getTypeName() + "!");
            } else if (section.getChangeCommentList() == null || section.getChangeCommentList().isEmpty()) {
                addError(errorMessageList, "" + releaseVersion + " / " + section.getChangeType(), "Empty comment list!");
            }
                
            for (String comment : section.getChangeCommentList()) {
                addList(errorMessageList, validateText("" + releaseVersion + " / " + section.getChangeType(), comment));
            }
        }

        return errorMessageList;
    }

    
    /**
     * Validate the text
     * 
     * @param header the header
     * @param text the text
     * @return the error list
     */
    protected List<String> validateText(String header, String text) {
        List<String> errorMessageList = new ArrayList<String>();
        if (text == null || text.isBlank()) {
            return errorMessageList;
        }

        if (changelogConfig.isLinkInCommentEnabled() && changelogConfig.hasLinkInComment(text)) {
            addError(errorMessageList, header, "Link is not allowed in comment: [" + text + "]!");
        }

        if (changelogConfig.isIdInCommentEnabled() && changelogConfig.hasIdInComment(text)) {
            addError(errorMessageList, header, "Id is not allowed in comment: [" + text + "]!");
        }

        return errorMessageList;
    }

    
    /**
     * Create a change-log entry reference list
     * 
     * @param entries the input entries
     * @return the reference list
     */
    protected List<ChangelogEntry> createSortedChangelogEntryReferenceList(List<ChangelogEntry> entries) {
        List<ChangelogEntry> sortedEntries = new ArrayList<ChangelogEntry>();
        sortedEntries.addAll(entries);
        Collections.sort(sortedEntries);
        return sortedEntries;
    }
 
    
    /**
     * Get the change-log configuration 
     *
     * @return the change-log configuration
     */
    protected ChangelogConfig getChangelogConfig() {
        return changelogConfig;
    }

    
    /**
     * Adds an error
     * 
     * @param errorMessageList the error message list
     * @param header the header
     * @param message the message
     */
    protected void addError(List<String> errorMessageList, String header, String message) {
        errorMessageList.add("- " + header + ": " + message);
    }
    

    /**
     * Add list
     * 
     * @param list the reference list
     * @param toAdd the list to add
     * @return the result
     */
    protected List<String> addList(List<String> list, List<String> toAdd) {
        if (toAdd != null && !toAdd.isEmpty()) {
            list.addAll(toAdd);
        }
        
        Collections.sort(list);
        Collections.reverse(list);
        return list;
    }
}
