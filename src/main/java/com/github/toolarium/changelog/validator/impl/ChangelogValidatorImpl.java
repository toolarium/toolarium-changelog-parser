/*
 * ChangelogValidator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.validator.impl;

import com.github.toolarium.changelog.ChangelogFactory;
import com.github.toolarium.changelog.config.ChangelogConfig;
import com.github.toolarium.changelog.dto.Changelog;
import com.github.toolarium.changelog.dto.ChangelogChangeType;
import com.github.toolarium.changelog.dto.ChangelogEntry;
import com.github.toolarium.changelog.dto.ChangelogErrorList;
import com.github.toolarium.changelog.dto.ChangelogErrorList.ErrorType;
import com.github.toolarium.changelog.dto.ChangelogReleaseVersion;
import com.github.toolarium.changelog.dto.ChangelogSection;
import com.github.toolarium.changelog.parser.ChangelogParseResult;
import com.github.toolarium.changelog.validator.IChangelogValidator;
import com.github.toolarium.changelog.validator.ValidationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Change-log validator 
 * 
 * @author patrick
 */
public class ChangelogValidatorImpl implements IChangelogValidator {
    private static final String EXCLAMATION_MARK = "!";
    private static final String END_MESSAGE = "]" + EXCLAMATION_MARK;
    private ChangelogConfig changelogConfig;

    
    /**
     * Constructor for ChangelogValidator
     */
    public ChangelogValidatorImpl() {
        this(new ChangelogConfig());
    }

    
    /**
     * Constructor for ChangelogValidator
     * 
     * @param changelogConfig the change-log configuration for formatting
     */
    public ChangelogValidatorImpl(ChangelogConfig changelogConfig) {
        this.changelogConfig = changelogConfig;
    }


    /**
     * @see com.github.toolarium.changelog.validator.IChangelogValidator#validate(java.nio.file.Path)
     */
    @Override
    public Changelog validate(Path filename) throws ValidationException, IOException {
        return validate(filename, null, null, null);
    }


    /**
     * @see com.github.toolarium.changelog.validator.IChangelogValidator#validate(java.nio.file.Path, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Changelog validate(Path filename, String projectName, String description, String version) throws ValidationException, IOException {
        Changelog changelog = null;
        ChangelogErrorList parseChangelogErrorList = null;
        try {
            ChangelogParseResult result = ChangelogFactory.getInstance().parse(filename);
            parseChangelogErrorList = result.getChangelogErrorList();
            
            validate(result.getChangelog(), projectName, description, version);
            changelog = result.getChangelog();
        } catch (ValidationException e) {
            
            if (e.getValidationErrorList() != null && !e.getValidationErrorList().isEmpty()) {
                parseChangelogErrorList.add(e.getValidationErrorList());
            }
            
            throw new ValidationException(e.getMessage(), parseChangelogErrorList);
        }

        if (parseChangelogErrorList != null && !parseChangelogErrorList.isEmpty()) {
            throw new ValidationException("Changelog parse errors.", parseChangelogErrorList);
        }
        
        return changelog;
    }


    /**
     * @see com.github.toolarium.changelog.validator.IChangelogValidator#validate(com.github.toolarium.changelog.dto.Changelog)
     */
    @Override
    public Changelog validate(Changelog changelog) throws ValidationException {
        return validate(changelog, null, null, null);
    }
    

    /**
     * @see com.github.toolarium.changelog.validator.IChangelogValidator#validate(com.github.toolarium.changelog.dto.Changelog, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Changelog validate(Changelog changelog, String projectName, String description, String inputVersion) throws ValidationException {
        
        ChangelogErrorList changelogErrorList = new ChangelogErrorList();
        ChangelogReleaseVersion version = convertVersion(changelogErrorList, inputVersion);

        if (changelog == null) {
            changelogErrorList.addGeneralError(ErrorType.CHANGELOG, "Invalid changelog!");
        } else {
            validateHeader(changelogErrorList, changelog, projectName, description);
            validateEntries(changelogErrorList, changelog.getEntries(), version);
            validateUnreleasdEntry(changelogErrorList, changelog);
        }

        if (changelogErrorList.isEmpty()) {
            return changelog;
        }
        
        throw new ValidationException("Changelog validation errors.", changelogErrorList);
    }


    /**
     * Convert input version
     * 
     * @param changelogErrorList the change-log error list
     * @param inputVersion the input version
     * @return the converted version
     */
    protected ChangelogReleaseVersion convertVersion(ChangelogErrorList changelogErrorList, String inputVersion) {
        ChangelogReleaseVersion version = null;
        if (inputVersion != null) {
            if (!"Unreleased".equals(inputVersion.trim())) {
                version = ChangelogFactory.getInstance().createChangelogParser().parseVersion(inputVersion); 
                
                if (version == null) {
                    changelogErrorList.addGeneralError(ErrorType.REFERENCE, "Invalid reference version [" + version + END_MESSAGE);
                }
            }
        }
        return version;
    }


    /**
     * Validate the header
     * 
     * @param changelogErrorList the change-log error list
     * @param changelog the change-log
     * @param projectName the expected project name
     * @param description the expected description
     */
    protected void validateHeader(ChangelogErrorList changelogErrorList, Changelog changelog, String projectName, String description) {
        if (changelog == null) {
            return;
        }

        if (projectName != null && !projectName.isBlank()) {
            if (!changelog.getProjectName().equals(projectName)) {
                changelogErrorList.addGeneralError(ErrorType.HEADER, "The name don't correspond to [" + projectName + "], current [" + changelog.getProjectName() + END_MESSAGE);
            }
        }

        if (description != null && !description.isBlank()) {
            if (!changelog.getDescription().equals(description)) {
                changelogErrorList.addGeneralError(ErrorType.HEADER, "The don't correspond to [" + description + END_MESSAGE);
            }
        } else {
            validateHeaderDescription(changelogErrorList, changelog.getDescription());
        }
    }

    
    /**
     * Validate the header description
     * 
     * @param changelogErrorList the change-log error list
     * @param description the header description
     */
    protected void validateHeaderDescription(ChangelogErrorList changelogErrorList, String description) {
        if (description == null || description.isBlank()) {
            return;
        }

        if (!changelogConfig.isSupportLinkInDescription()) {
            String link = changelogConfig.hasLinkInComment(description);
            if (link != null) {
                changelogErrorList.addGeneralError(ErrorType.HEADER, "Description has a link comment which is not allowed: [" + link + END_MESSAGE);
            }
        }
        
        if (changelogConfig.isIdInCommentEnabled()) {
            String id = changelogConfig.hasIdInComment(description);
            if (id != null) {
                changelogErrorList.addGeneralError(ErrorType.HEADER, "Description has an id in comment which is not allowed: [" + id + END_MESSAGE);
            }
        }
    
        if (!(description.trim().endsWith(".") || description.trim().endsWith(EXCLAMATION_MARK))) {
            changelogErrorList.addGeneralError(ErrorType.HEADER, "Description text don't end with a punction mark!");
        }
    }

    
    /**
     * Validate the header
     * 
     * @param changelogErrorList the change-log error list
     * @param entries the change-log entries
     * @param version the reference version or null
     */
    protected void validateEntries(ChangelogErrorList changelogErrorList, List<ChangelogEntry> entries, ChangelogReleaseVersion version) {
        
        if (entries == null || entries.isEmpty()) {
            changelogErrorList.addGeneralError(ErrorType.ENTRIES, "Missing changelog entries!");
            return;
        }

        // validate sort order
        validateEntryOrder(changelogErrorList, entries, version);

        // validate entries
        for (ChangelogEntry entry : entries) {
            validateEntry(changelogErrorList, entry);
        }
        
        // validate if version exists
        validateVersionExist(changelogErrorList, entries, version);
    }


    /**
     * Validate the unreleased entry
     * 
     * @param changelogErrorList the change-log error list
     * @param changelog the change-log
     */
    protected void validateUnreleasdEntry(ChangelogErrorList changelogErrorList, Changelog changelog) {
        
        if (!changelogConfig.isSupportUnreleased()) {
            if (changelog.getEntry(null) != null) {
                changelogErrorList.addGeneralError(ErrorType.UNRELEASED, "The unreleased section is not supported!");
            }
        }
    }
    
    
    /**
     * Validate entry order
     * 
     * @param changelogErrorList the change-log error list
     * @param entries the entries
     * @param version the version
     */
    protected void validateEntryOrder(ChangelogErrorList changelogErrorList, List<ChangelogEntry> entries, ChangelogReleaseVersion version) {
        
        if (entries == null || entries.isEmpty()) {
            return;
        }

        // create sorted reference list
        List<ChangelogEntry> sortedEntries = createSortedChangelogEntryReferenceList(entries);
        if (version != null && sortedEntries != null && !sortedEntries.isEmpty()) {
            for (ChangelogEntry entry : sortedEntries) {
                if (entry.getReleaseVersion() != null && entry.getReleaseVersion().compareTo(version) > 0) {
                    changelogErrorList.addReleaseError(entry.getReleaseVersion(), "Newer version in than [" + version + "] in changelog found!");
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
                    changelogErrorList.addReleaseError(entry.getReleaseVersion(), "The version [" + version + "] should be referenced as first entry.");
                }
            }

            if (!printedSortError && !entry.equals(sortedEntries.get(i))) {
                changelogErrorList.addReleaseError(entry.getReleaseVersion(), "Invalid sort order!");
                printedSortError = true;
            }
        }
    }

        
    /**
     * Validate entry order
     * 
     * @param changelogErrorList the change-log error list
     * @param entries the entries
     * @param searchVersion the version to check
     */
    protected void validateVersionExist(ChangelogErrorList changelogErrorList, List<ChangelogEntry> entries, ChangelogReleaseVersion searchVersion) {
        
        if (searchVersion == null) {
            return;
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
            changelogErrorList.addReleaseError(searchVersion, "Could not find version " + searchVersion + EXCLAMATION_MARK);
        } else if (found > 1) {
            changelogErrorList.addReleaseError(searchVersion, "Found " + found + " times the same version " + searchVersion + EXCLAMATION_MARK);
        }
    }

    
    /**
     * Validate entry order
     * 
     * @param changelogErrorList the change-log error list
     * @param entry the entry
     */
    protected void validateEntry(ChangelogErrorList changelogErrorList, ChangelogEntry entry) {
        
        if (entry == null) {
            changelogErrorList.addGeneralError(ErrorType.ENTRIES, "Invalid empty entry!");
            return;
        }

        if (!changelogConfig.isSupportReleaseLink() && entry.getReleaseLink() != null) {
            changelogErrorList.addReleaseError(entry.getReleaseVersion(), "Release link is not supported [" + entry.getReleaseLink() + END_MESSAGE);
        }

        if (!changelogConfig.isSupportReleaseInfo() && entry.getInfo() != null && !entry.getInfo().isBlank()) {
            changelogErrorList.addReleaseError(entry.getReleaseVersion(), "Additional release information is not supported [" + entry.getInfo() + END_MESSAGE);
        }

        validateEntryDescription(changelogErrorList, entry);
        validateChangelogSections(changelogErrorList, entry.getReleaseVersion(), entry.getSectionList());
    }

    
    
    /**
     * Validate the entry description
     * 
     * @param changelogErrorList the change-log error list
     * @param entry the change-log entry
     */
    protected void validateEntryDescription(ChangelogErrorList changelogErrorList, ChangelogEntry entry) {
        
        if (entry == null || entry.getDescription() == null || entry.getDescription().isBlank()) {
            return;
        }

        boolean unreleased = entry.getReleaseVersion() == null || !entry.isReleased();
        if (!changelogConfig.isSupportLinkInDescription()) {
            String link = changelogConfig.hasLinkInComment(entry.getDescription());
            if (link != null) {
                if (unreleased) {
                    changelogErrorList.addGeneralError(ErrorType.UNRELEASED, "Description has a link comment which is not allowed: [" + link + END_MESSAGE);
                } else {
                    changelogErrorList.addReleaseError(entry.getReleaseVersion(), "Description has a link comment which is not allowed: [" + link + END_MESSAGE);
                }
            }
        }

        if (changelogConfig.isIdInCommentEnabled()) {
            String id = changelogConfig.hasIdInComment(entry.getDescription());
            if (id != null) {
                if (unreleased) {
                    changelogErrorList.addGeneralError(ErrorType.UNRELEASED, "Description has an id in comment which is not allowed: [" + id + END_MESSAGE);
                } else {
                    changelogErrorList.addReleaseError(entry.getReleaseVersion(), "Description has an id in comment which is not allowed: [" + id + END_MESSAGE);
                }
            }
        }
        
        if (!(entry.getDescription().trim().endsWith(".") || entry.getDescription().trim().endsWith(EXCLAMATION_MARK))) {
            if (unreleased) {
                changelogErrorList.addGeneralError(ErrorType.UNRELEASED, "Description text don't end with a punction mark!");
            } else {
                changelogErrorList.addReleaseError(entry.getReleaseVersion(), "Description text don't end with a punction mark!");
            }
        }
    }

    
    /**
     * Validate entry order
     * 
     * @param changelogErrorList the change-log error list
     * @param releaseVersion the release version
     * @param sectionList the section list
     */
    protected void validateChangelogSections(ChangelogErrorList changelogErrorList, ChangelogReleaseVersion releaseVersion, List<ChangelogSection> sectionList) {
        
        if (sectionList == null) {
            changelogErrorList.addGeneralError(ErrorType.ENTRIES, "Invalid empty section!");
            return;
        }

        Set<ChangelogChangeType> changeLogChangeTypeSet = new HashSet<>(Arrays.asList(ChangelogChangeType.values()));
        for (ChangelogSection section : sectionList) {
            if (section.getChangeType() == null) {
                
                // addError( errorMessageList, releaseVersion, "Duplicate type " + changeType + "!" );
            } else if (!changeLogChangeTypeSet.remove(section.getChangeType())) {
                changelogErrorList.addReleaseError(releaseVersion, "Duplicate section type " + section.getChangeType().getTypeName() + EXCLAMATION_MARK);
            } else if (section.getChangeCommentList() == null || section.getChangeCommentList().isEmpty()) {
                changelogErrorList.addReleaseError(releaseVersion, "Empty comment list in section type " + section.getChangeType() + EXCLAMATION_MARK);
            }
                
            for (String comment : section.getChangeCommentList()) {
                String changelogChangeTypeStr = "Section";
                if (section.getChangeType() != null) {
                    changelogChangeTypeStr = section.getChangeType().getTypeName() + " section";
                }

                validateChangeComment(changelogErrorList, releaseVersion, changelogChangeTypeStr, comment);
            }
        }
    }

    
    /**
     * Validate the change comment
     * 
     * @param changelogErrorList the change-log error list
     * @param releaseVersion the release version or null
     * @param changelogChangeType the change log type
     * @param changeComment text the change comment
     */
    protected void validateChangeComment(ChangelogErrorList changelogErrorList, ChangelogReleaseVersion releaseVersion, String changelogChangeType, String changeComment) {
        if (changeComment == null || changeComment.isBlank()) {
            return;
        }

        if (changelogConfig.isLinkInCommentEnabled()) {
            String link = changelogConfig.hasLinkInComment(changeComment);
            if (link != null) {
                changelogErrorList.addReleaseError(releaseVersion, changelogChangeType + " has a link comment which is not allowed: [" + link + END_MESSAGE);
            }
        }

        String trimmedComment = changeComment.trim();
        if (!(trimmedComment.trim().endsWith(".") || trimmedComment.trim().endsWith(EXCLAMATION_MARK))) {
            changelogErrorList.addReleaseError(releaseVersion, changelogChangeType + " text don't end with a punction mark!");
        }

        ChangeComment comment = validateChangeIdInComment(changelogErrorList, releaseVersion, changelogChangeType, trimmedComment);
        if (comment != null && comment.getComment() != null && !comment.getComment().isBlank()) {
            validateSentence(changelogErrorList, releaseVersion, changelogChangeType, comment.getComment());
        } else {
            changelogErrorList.addReleaseError(releaseVersion, "Empty comment in section type " + changelogChangeType + EXCLAMATION_MARK);
        }
    }

    
    /**
     * Validate the change comment
     * 
     * @param changelogErrorList the change-log error list
     * @param releaseVersion the release version or null
     * @param changelogChangeType the change log type
     * @param changeComment text the change comment
     * @return list of ids.
     */
    protected ChangeComment validateChangeIdInComment(ChangelogErrorList changelogErrorList, ChangelogReleaseVersion releaseVersion, String changelogChangeType, String changeComment) {
        if (changeComment == null || changeComment.isBlank()) {
            return null;
        }
        
        String comment = changeComment.trim();
        if (comment.endsWith(".") || comment.endsWith(EXCLAMATION_MARK)) {
            comment = comment.substring(0, comment.length() - 1);
        }

        List<String> idList = null;
        if (changelogConfig.isSupportIdListOnEndOfTheComment()) {
            idList = new ArrayList<>();
            int idx = comment.lastIndexOf('(');
            if ((idx > 0) && (idx < comment.length())) {
                // cut comment
                String list = comment.substring(idx + 1).trim();
                if (!list.isEmpty() && list.endsWith(")")) {
                    list = list.substring(0, list.length() - 1);
                    
                    if (!list.isBlank()) {
                        comment = comment.substring(0, idx).trim();

                        String[] ids = list.split(",");
                        for (int i = 0; i < ids.length; i++) {
                            String id = ids[i].trim();
                            if (!id.isEmpty()) {
                                idList.add(id);
                            }
                        }
                    }
                }
            } 
        } 
        
        if (changelogConfig.isIdInCommentEnabled()) {
            String id = changelogConfig.hasIdInComment(comment);
            if (id != null) {
                changelogErrorList.addReleaseError(releaseVersion, changelogChangeType + " has an id in comment which is not allowed: [" + id + END_MESSAGE);
            }
        }
        
        return new ChangeComment(comment, idList);
    }

    
    /**
     * Validate the sentence.
     * 
     * @param changelogErrorList the change-log error list
     * @param releaseVersion the release version or null
     * @param changelogChangeType the change log type
     * @param sentence the sentence to test
     */
    protected void validateSentence(ChangelogErrorList changelogErrorList, ChangelogReleaseVersion releaseVersion, String changelogChangeType, String sentence) {
        if (sentence == null || sentence.isBlank()) {
            return;
        }
     
        String[] splitComment = sentence.split(" ");
        if (splitComment.length < 2) {
            changelogErrorList.addReleaseError(releaseVersion, "Invalid sentence in section type " + changelogChangeType + ": [" + sentence + "]" + EXCLAMATION_MARK);
        }
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
}
