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
import com.github.toolarium.common.util.EnumUtil;
import com.github.toolarium.common.util.StringUtil;
import com.github.toolarium.common.version.Version;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;


/**
 * Implements a change-log parser following https://keepachangelog.com.
 * 
 * @author patrick
 */
public class ChangelogParserImpl implements IChangelogParser {
    private static final String STAR_SIGN = "*";
    private static final String DASH_SIGN = "-";
    private volatile boolean dateWarning;


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
        
        String content = Files.readString(filename);
        return parseContent(content.strip());
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
            } catch (RuntimeException e) {
                result.getChangelogErrorList().addGeneralError(ErrorType.ENTRIES, e.getMessage());
            }
        } catch (RuntimeException e) {
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
            return new ChangelogReleaseVersion(v.getMajorNumber(), v.getMinorNumber(), v.getPatchNumber(), v.getPatchSuffix());
        } catch (IllegalArgumentException e) {
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

        String sep = parser.readChangelogSeparator();
        String name = "";
        if (!sep.isEmpty()) {
            name = parser.readEOL();
        }

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
                String preapredReleaseDate = StringUtil.getInstance().trimRight(StringUtil.getInstance().trimLeft(releaseDate, '('), ')');
                
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
                    releaseInfo = releaseInfo.replace("[YANKED]", "").trim();
                    releaseInfo = releaseInfo.replace("YANKED", "").trim();

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

            changelog.addEntry(changelogEntry);
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
                
                String releaseLink = StringUtil.getInstance().trimRight(StringUtil.getInstance().trimLeft(releaseVersion.substring(idx + 1), '('), ')');
                if (releaseLink != null && !releaseLink.isBlank()) {
                    try {
                        changelogEntry.setReleaseLink(URI.create(releaseLink).toURL());
                    } catch (IllegalArgumentException | java.net.MalformedURLException e) {
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

                if ((v.getMajorSuffix() != null && !v.getMajorSuffix().isEmpty())
                        || (v.getMinorSuffix() != null && !v.getMinorSuffix().isEmpty())) {
                    throw new IllegalArgumentException("Invalid version format: " + releaseVersion + "!");
                }

                String buildInfo = StringUtil.getInstance().trimLeft(StringUtil.getInstance().trimLeft(v.getPatchSuffix(),'-'),'.');
                changelogReleaseVersion = new ChangelogReleaseVersion(v.getMajorNumber(), v.getMinorNumber(), v.getPatchNumber(), buildInfo);                    
            }

            changelogEntry.setReleaseVersion(changelogReleaseVersion);
            changelogEntry.setHasBracketsAroundVersion(hasBracketsAroundVersion);
        } catch (IllegalArgumentException ev) {
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

            ChangelogChangeType changelogChangeType = EnumUtil.getInstance().valueOf(ChangelogChangeType.class, changelogType);
            if (changelogChangeType == null) {
                changelogErrorList.addReleaseError(changelogEntry.getReleaseVersion(), "Invalid changelog change type: [" + changelogType + "]!");
            } else if (!changelogChangeType.getTypeName().equals(changelogType)) {
                changelogErrorList.addReleaseError(changelogEntry.getReleaseVersion(), "Don't match exactly: [" + changelogType + "]!");
            }

            ChangelogSection section = new ChangelogSection(changelogChangeType);
            changelogEntry.addSection(section);

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
                StringBuilder currentItem = new StringBuilder();
                for (int i = 0; i < itemSplit.length; i++) {
                    String item = itemSplit[i];

                    String strippedLeadingWhitespaces = item.stripLeading();
                    if (!item.equals(strippedLeadingWhitespaces) && (strippedLeadingWhitespaces.startsWith(DASH_SIGN) || strippedLeadingWhitespaces.startsWith(STAR_SIGN))) {
                        item = strippedLeadingWhitespaces;
                        changelogErrorList.addReleaseError(changelogEntry.getReleaseVersion(), "Space before comment list in section type " + section.getChangeType().getTypeName() + "!");
                    }

                    if (item.startsWith(DASH_SIGN) || item.startsWith(STAR_SIGN)) {
                        String comment = item.substring(1).stripLeading();
                        if (comment.trim().isEmpty()) {
                            changelogErrorList.addReleaseError(changelogEntry.getReleaseVersion(), "Empty comment list in section type " + section.getChangeType().getTypeName() + "!");
                        } else {
                            if (currentItem.toString().trim().length() > 0) {
                                section.add(currentItem.toString());
                            }

                            currentItem = new StringBuilder(comment);
                        }
                    } else {
                        if (item.isBlank()) {
                            changelogErrorList.addReleaseError(changelogEntry.getReleaseVersion(), "Empty comment list in section type " + section.getChangeType().getTypeName() + "!");
                        } else {
                            currentItem.append(ChangelogContentParser.NEWLINE).append(item);
                        }
                    }
                }

                if (!currentItem.toString().isBlank()) {
                    section.add(currentItem.toString());
                }
            }
        }
    }
}
