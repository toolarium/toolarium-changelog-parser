/*
 * ChangelogParser.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.parser.impl;

import com.github.toolarium.changelog.dto.Changelog;
import com.github.toolarium.changelog.dto.ChangelogChangeType;
import com.github.toolarium.changelog.dto.ChangelogEntry;
import com.github.toolarium.changelog.dto.ChangelogErrorList;
import com.github.toolarium.changelog.dto.ChangelogErrorList.ErrorType;
import com.github.toolarium.changelog.dto.ChangelogReleaseVersion;
import com.github.toolarium.changelog.dto.ChangelogSection;
import com.github.toolarium.changelog.parser.ChangelogParseResult;
import com.github.toolarium.changelog.parser.IChangelogParser;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import jptools.parser.ParseException;
import jptools.resource.FileCacheManager;
import jptools.util.ByteArray;
import jptools.util.EnumUtil;
import jptools.util.StringHelper;
import jptools.util.version.Version;


/**
 * Implements a change-log parser following https://keepachangelog.com.
 * 
 * @author patrick
 */
public class ChangelogParserImpl implements IChangelogParser {
    private boolean dateWarning;
    
    /**
     * Constructor for ChangelogParser
     */
    public ChangelogParserImpl() {
        dateWarning = false;
    }
    

    /**
     * @see com.github.toolarium.changelog.parser.IChangelogParser#parse(java.nio.file.Path)
     */
    @Override
    public ChangelogParseResult parse(Path filename) throws IOException {
        if (filename == null) {
            throw new IOException("Invalid filename input!");
        }
        
        ByteArray content = (ByteArray) new FileCacheManager().getFile(filename.toString());
        return parseContent(content.trim((byte) ' ').toString());
    }


    /**
     * @see com.github.toolarium.changelog.parser.IChangelogParser#parse(java.lang.String)
     */
    @Override
    public ChangelogParseResult parse(String inputContent) {
        if (inputContent == null || inputContent.isBlank()) {
            return new ChangelogParseResult();
        }

        return parseContent(inputContent);
    }
    

    /**
     * @see com.github.toolarium.changelog.parser.IChangelogParser#parseContent(java.lang.String)
     */
    @Override
    public ChangelogParseResult parseContent(String inputContent) {
        if (inputContent == null || inputContent.length() == 0) {
            return new ChangelogParseResult();
        }

        ChangelogContentParser parser = new ChangelogContentParser();
        parser.init(inputContent);

        ChangelogErrorList changelogErrorList = new ChangelogErrorList();
        ChangelogParseResult result = new ChangelogParseResult();
        
        try {
            // project name and description (optional)
            readChangelogHeader(parser, result);

            try {
                // all change-log entries
                readChangelogEntryList(parser, changelogErrorList, result.getChangelog());
            } catch (Exception e) {
                result.getChangelogErrorList().addGeneralError(ErrorType.ENTRIES, e.getMessage());
            }
        } catch (Exception e) {
            result.getChangelogErrorList().addGeneralError(ErrorType.HEADER, e.getMessage());
        }

        if (!changelogErrorList.isEmpty()) {
            result.setChangelogErrorList(changelogErrorList);
        }

        return result;
    }
    
    
    /**
     * @see com.github.toolarium.changelog.parser.IChangelogParser#parseVersion(java.lang.String)
     */
    @Override
    public ChangelogReleaseVersion parseVersion(String inputVersion) {
        try {
            Version v = new Version(inputVersion.trim());
            return new ChangelogReleaseVersion(v.getMajorNumber(), v.getMinorNumber(), v.getBuildNumber(), v.getBuildInfo());
        } catch (ParseException e) {
            return null;
        }
    }

    
    /**
     * Read the change-log entry list.
     * 
     * @param parser the parser
     * @param changelogParseResult the change-log parse result
     */
    protected void readChangelogHeader(ChangelogContentParser parser, ChangelogParseResult changelogParseResult) {
        if (parser == null || changelogParseResult == null) {
            return;
        }

        parser.readChangelogSeparator();
        String name = parser.readEOL();
        if (name == null || name.isBlank()) {
            changelogParseResult.getChangelogErrorList().addGeneralError(ChangelogErrorList.ErrorType.CHANGELOG, "Invalid empty changelog name!");
        }

        String description = parser.readDescription();
        changelogParseResult.setChangelog(new Changelog(name, description));
    }

    
    /**
     * Read the change-log entry list.
     * 
     * @param parser the parser
     * @param changelogErrorList the change-log error list
     * @param changelog the change-log
     */
    protected void readChangelogEntryList(ChangelogContentParser parser, ChangelogErrorList changelogErrorList, Changelog changelog) {
        if (parser == null || changelog == null) {
            return;
        }

        parser.readChangelogSeparator();
        while (!parser.isEOL()) {
            ChangelogEntry changelogEntry = new ChangelogEntry();
            ChangelogReleaseVersion releaseVersion = readVersion(parser, changelogErrorList, changelogEntry);

            // read header separator
            Character separator = parser.readHeaderSeparator();

            // read date
            String releaseDate = parser.readDate();
            try {
                String preapredReleaseDate = StringHelper.trimRight(StringHelper.trimLeft(releaseDate, '('), ')');
                
                if (preapredReleaseDate != null && !preapredReleaseDate.isEmpty()) {
                    changelogEntry.setReleaseDate(LocalDate.parse(preapredReleaseDate));
                }
                
                if (!preapredReleaseDate.equals(releaseDate) && !dateWarning) {
                    dateWarning = true;
                    changelogErrorList.addGeneralError(ErrorType.ENTRIES, "Invalid relase date format, e.g. [" + releaseDate + "]!");
                }
                
            } catch (DateTimeParseException ev) {
                changelogErrorList.addReleaseError(releaseVersion, "Invalid relase date [" + releaseDate + "]!");
            }

            // read header separator
            Character dateSeparator = parser.readHeaderSeparator();
            if (separator != null && dateSeparator != null && !separator.equals(dateSeparator)) {
                changelogErrorList.addReleaseError(releaseVersion, "Found mixed separator character in version section " + separator + " and " + dateSeparator + ".");
            }

            // read header trailer
            String releaseInfo = parser.readHeaderEnd();
            if (releaseInfo != null && !releaseInfo.isBlank()) {
                if (releaseInfo.indexOf("YANKED") >= 0 || releaseInfo.indexOf("[YANKED]") >= 0) {
                    releaseInfo = StringHelper.replace(releaseInfo, "[YANKED]", "").trim();
                    releaseInfo = StringHelper.replace(releaseInfo, "YANKED", "").trim();

                    if (releaseInfo.isBlank()) {
                        releaseInfo = null;
                    }

                    changelogEntry.setWasYanked();
                }
            } else {
                releaseInfo = null;
            }
            changelogEntry.setInfo(releaseInfo);

            // read description
            String releaseDescription = parser.readDescription();
            changelogEntry.setDescription(releaseDescription);

            changelog.getEntries().add(changelogEntry);
            readChangelogSectionList(parser, changelogErrorList, changelogEntry);
        }
    }

    
    /**
     * Read the change-log entry list.
     * 
     * @param parser the parser
     * @param changelogErrorList the change-log error list
     * @param changelogEntry the change-log entry
     * @return the version number
     */
    protected ChangelogReleaseVersion readVersion(ChangelogContentParser parser, ChangelogErrorList changelogErrorList, ChangelogEntry changelogEntry) {
        if (parser == null || changelogEntry == null) {
            return null;
        }
        
        String releaseLinkError = null;

        // read version number
        String releaseVersion = parser.readVersion().stripLeading();
        boolean hasBracketsAroundVersion = false;
        if (!releaseVersion.isEmpty() && releaseVersion.startsWith("[")) {
            int idx = releaseVersion.indexOf("]", 1);
            if (idx > 0) {
                hasBracketsAroundVersion = true;
                
                String releaseLink = StringHelper.trimRight(StringHelper.trimLeft(releaseVersion.substring(idx + 1), '('), ')');
                if (releaseLink != null && !releaseLink.isBlank()) {
                    try {
                        changelogEntry.setReleaseLink(new URL(releaseLink));
                    } catch (Exception e) {
                        releaseLinkError = "Invalid relase link [" + releaseLink + "]: " + e.getMessage() + "!";
                    }                
                }
                
                releaseVersion = releaseVersion.substring(1, idx);
            }
        }

        ChangelogReleaseVersion changelogReleaseVersion = null;

        try {
            if (!Changelog.UNRELEASED_ENTRY_NAME.equalsIgnoreCase(releaseVersion.trim())) {
                Version v = new Version(releaseVersion.trim());

                if ((v.getMajorInfo() != null && !v.getMajorInfo().isEmpty()) 
                        || (v.getMinorInfo() != null && !v.getMinorInfo().isEmpty())) {
                    throw new ParseException("Invalid version format: " + releaseVersion + "!");
                }

                String buildInfo = StringHelper.trimLeft(StringHelper.trimLeft(v.getBuildInfo(),'-'),'.');
                changelogReleaseVersion = new ChangelogReleaseVersion(v.getMajorNumber(), v.getMinorNumber(), v.getBuildNumber(), buildInfo);                    
            }

            changelogEntry.setReleaseVersion(changelogReleaseVersion);
            changelogEntry.setHasBracketsAroundVersion(hasBracketsAroundVersion);
        } catch (ParseException ev) {
            changelogErrorList.addReleaseError(changelogReleaseVersion, "Invalid relase version [" + releaseVersion + "]!");
        }

        if (releaseLinkError != null) {
            changelogErrorList.addReleaseError(changelogReleaseVersion, releaseLinkError);
        }
        
        return changelogReleaseVersion;
    }

    
    /**
     * Read the change-log section list.
     * 
     * @param parser the parser
     * @param changelogErrorList the change-log error list
     * @param changelogEntry the change-log entry
     */
    protected void readChangelogSectionList(ChangelogContentParser parser, ChangelogErrorList changelogErrorList, ChangelogEntry changelogEntry) {
        if (parser == null || changelogEntry == null) {
            return;
        }
        
        String sep = parser.readChangelogSeparator();
        while (!parser.isEOL() && sep.length() == 3) {
            String changelogType = parser.readEOL();

            ChangelogChangeType changelogChangeType = EnumUtil.valueOf(ChangelogChangeType.class, changelogType);
            if (changelogChangeType == null) {
                changelogErrorList.addReleaseError(changelogEntry.getReleaseVersion(), "Invalid changelog change type: [" + changelogType + "]!");
            } else if (!changelogChangeType.getTypeName().equals(changelogType)) {
                changelogErrorList.addReleaseError(changelogEntry.getReleaseVersion(), "Don't match exactly: [" + changelogType + "]!");
            }

            ChangelogSection section = new ChangelogSection(changelogChangeType);
            changelogEntry.getSectionList().add(section);

            try {
                readChangelogSectionItemList(parser, changelogErrorList, changelogEntry, section);
            } catch (Exception e) {
                changelogErrorList.addGeneralError(ErrorType.ENTRIES, e.getMessage());
            }

            sep = parser.readChangelogSeparator();
        }
    }

    
    /**
     * Read the change-log section list.
     * 
     * @param parser the parser
     * @param changelogErrorList the change-log error list
     * @param changelogEntry the change-log entry
     * @param section the section
     */
    protected void readChangelogSectionItemList(ChangelogContentParser parser, ChangelogErrorList changelogErrorList, ChangelogEntry changelogEntry, ChangelogSection section) {
        if (parser == null || section == null) {
            return;
        }
        
        String itemContent = parser.readChangelogText();
        if (itemContent != null && !itemContent.isEmpty()) {
            String[] itemSplit = itemContent.split("" + ChangelogContentParser.NEWLINE);
            if (itemSplit != null) {
                String currentItem = "";
                for (int i = 0; i < itemSplit.length; i++) {
                    String item = itemSplit[i];
                    if (item.startsWith("-") || item.startsWith("*")) {
                        String comment = item.substring(1).stripLeading();
                        if (comment.trim().isEmpty()) {
                            changelogErrorList.addReleaseError(changelogEntry.getReleaseVersion(), "Empty comment list in section type " + section.getChangeType().getTypeName() + "!");
                        } else {
                            if (!currentItem.trim().isEmpty()) {
                                section.add(currentItem);
                            }
                            
                            currentItem = comment;
                        }
                    } else {
                        if (item.isBlank()) {
                            changelogErrorList.addReleaseError(changelogEntry.getReleaseVersion(), "Empty comment list in section type " + section.getChangeType().getTypeName() + "!");
                        } else {
                            currentItem += ChangelogContentParser.NEWLINE + item;
                        }
                    }
                }

                if (!currentItem.isBlank()) {
                    section.add(currentItem);
                }
            }
        }
    }
}
