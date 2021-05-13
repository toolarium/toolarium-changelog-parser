/*
 * ChangelogParser.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.parser;

import com.github.toolarium.changelog.config.ChangelogConfig;
import com.github.toolarium.changelog.dto.Changelog;
import com.github.toolarium.changelog.dto.ChangelogChangeType;
import com.github.toolarium.changelog.dto.ChangelogEntry;
import com.github.toolarium.changelog.dto.ChangelogSection;
import com.github.toolarium.changelog.parser.impl.ChangelogContentParser;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import jptools.parser.ParseException;
import jptools.resource.FileCacheManager;
import jptools.util.ByteArray;
import jptools.util.EnumUtil;
import jptools.util.StringHelper;


/**
 * Implements a change-log parser following https://keepachangelog.com.
 * 
 * @author patrick
 */
public class ChangelogParser {
    private ChangelogConfig changelogConfig;
    
    
    /**
     * Constructor for ChangelogParser
     */
    public ChangelogParser() {
        this(new ChangelogConfig());
    }
    
    
    /**
     * Constructor for ChangelogParser
     * 
     * @param changelogConfig the change-log configuration for formatting
     */
    public ChangelogParser(ChangelogConfig changelogConfig) {
        this.changelogConfig = changelogConfig;
    }
    
    
    /**
     * Parse a change-log 
     * 
     * @param filename the filename
     * @return the change-log parse result
     * @throws IOException In case of an i/o error
     */
    public ChangelogParseResult parse(Path filename) throws IOException {
        if (filename == null) {
            throw new IOException("Invalid filename input");
        }
        
        ByteArray content = (ByteArray) new FileCacheManager().getFile(filename.toString());
        return parseContent(content.trim((byte) ' '));
    }

    
    /**
     * Parse a change-log content
     * 
     * @param inputContent the change-log content to parse
     * @return the change-log parse result
     */
    public ChangelogParseResult parse(String inputContent) {
        if (inputContent == null || inputContent.isBlank()) {
            return new ChangelogParseResult();
        }

        return parseContent(new ByteArray(inputContent));
    }
    
    
    /**
     * Parse a change-log content
     * 
     * @param inputContent the change-log content to parse
     * @return the change-log parse result
     */
    public ChangelogParseResult parseContent(ByteArray inputContent) {
        if (inputContent == null || inputContent.length() == 0) {
            return new ChangelogParseResult();
        }

        ChangelogContentParser parser = new ChangelogContentParser(changelogConfig);
        parser.init(inputContent);

        List<String> errorMessageList = new ArrayList<String>();
        ChangelogParseResult result = new ChangelogParseResult();

        try {
            // project name and description (optional)
            addList(errorMessageList, readChangelogHeader(parser, result));

            try {
                // all change-log entries
                addList(errorMessageList, readChangelogEntryList(parser, result.getChangelog()));
            } catch (Exception e) {
                addError(errorMessageList, "Changelog Entry", e.getMessage());
            }
        } catch (Exception e) {
            addError(errorMessageList, "Changelog header", e.getMessage());
        }

        result.setErrorMessageList(errorMessageList);
        return result;
    }
    
    
    /**
     * Read the change-log entry list
     * 
     * @param parser the parser
     * @param changelogParseResult the change-log parse result
     * @return the error list
     */
    protected List<String> readChangelogHeader(ChangelogContentParser parser, ChangelogParseResult changelogParseResult) {
        List<String> errorMessageList = new ArrayList<String>();
        if (parser == null || changelogParseResult == null) {
            return errorMessageList;
        }

        parser.readChangelogSeparator();
        String name = parser.readEOL();
        if (name == null || name.isBlank()) {
            errorMessageList.add("Invalid empty changelog name!");
        }

        String description = parser.readDescription();

        changelogParseResult.setChangelog(new Changelog(name, description));
        return errorMessageList;
    }

    
    /**
     * Read the change-log entry list
     * 
     * @param parser the parser
     * @param changelog the change-log
     * @return the error list
     */
    protected List<String> readChangelogEntryList(ChangelogContentParser parser, Changelog changelog) {
        List<String> errorMessageList = new ArrayList<String>();
        if (parser == null || changelog == null) {
            return errorMessageList;
        }

        parser.readChangelogSeparator();
        while (!parser.isEOL()) {
            ChangelogEntry changelogEntry = new ChangelogEntry();

            // read version number
            String releaseVersion = readBracketExpression(errorMessageList, "relase version", parser.readVersion());
            try {
                changelogEntry.setReleaseVersion(releaseVersion);
            } catch (ParseException ev) {
                addError(errorMessageList, releaseVersion, "Invalid relase version [" + releaseVersion + "]");
            }

            // read header separator
            parser.readHeaderSeparator();

            // read date
            String releaseDate = parser.readDate();
            try {
                changelogEntry.setReleaseDate(releaseDate);
            } catch (DateTimeParseException ev) {
                addError(errorMessageList, releaseVersion, "Invalid relase date [" + releaseDate + "]");
            }

            // read header separator
            parser.readHeaderSeparator();

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
            addList(errorMessageList, readChangelogSectionList(parser, changelogEntry));
        }

        return errorMessageList;
    }

    
    /**
     * Read the change-log section list
     * 
     * @param parser the parser
     * @param changelogEntry the change-log entry
     * @return the error list
     */
    protected List<String> readChangelogSectionList(ChangelogContentParser parser, ChangelogEntry changelogEntry) {
        List<String> errorMessageList = new ArrayList<String>();
        if (parser == null || changelogEntry == null) {
            return errorMessageList;
        }
        
        String sep = parser.readChangelogSeparator();
        while (!parser.isEOL() && sep.length() == 3) {
            String changelogType = parser.readEOL();

            ChangelogChangeType changelogChangeType = EnumUtil.valueOf(ChangelogChangeType.class, changelogType);
            if (changelogChangeType == null) {
                addError(errorMessageList, "" + changelogEntry.getReleaseVersion(), "Invalid changelog change type: " + changelogType);
            } else if (!changelogChangeType.getTypeName().equals(changelogType)) {
                addError(errorMessageList, changelogEntry.getReleaseVersion() + " / " + changelogChangeType.getTypeName(), "Don't match exactly: " + changelogType);
            }

            ChangelogSection section = new ChangelogSection(changelogChangeType);
            changelogEntry.getSectionList().add(section);

            try {
                addList(errorMessageList, readChangelogSectionItemList(parser, changelogEntry, section));
            } catch (Exception e) {
                addError(errorMessageList, "Changelog section", e.getMessage());
            }

            sep = parser.readChangelogSeparator();
        }

        return errorMessageList;
    }

    
    /**
     * Read the change-log section list
     * 
     * @param parser the parser
     * @param changelogEntry the change-log entry
     * @param section the section
     * @return the error list
     */
    protected List<String> readChangelogSectionItemList(ChangelogContentParser parser, ChangelogEntry changelogEntry, ChangelogSection section) {
        List<String> errorMessageList = new ArrayList<String>();
        if (parser == null || section == null) {
            return errorMessageList;
        }
        
        String itemContent = parser.readChangelogText();

        if (itemContent != null && !itemContent.isEmpty()) {
            String[] itemSplit = itemContent.split("" + ChangelogContentParser.NEWLINE);
            if (itemSplit != null) {
                String currentItem = "";
                for (int i = 0; i < itemSplit.length; i++) {
                    String item = itemSplit[i];
                    if (item.startsWith("" + changelogConfig.getItemSeparator())) {
                        String comment = item.substring(1).stripLeading();
                        if (comment.trim().isEmpty()) {
                            addError(errorMessageList, "" + changelogEntry.getReleaseVersion() + " / " + section.getChangeType().getTypeName(), "Invalid empty comment!");
                        } else {
                            if (!currentItem.trim().isEmpty()) {
                                section.add(currentItem);
                            }
                            
                            currentItem = comment;
                        }
                    } else {
                        if (item.isBlank()) {
                            addError(errorMessageList, "" + changelogEntry.getReleaseVersion() + " / " + section.getChangeType().getTypeName(), "Invalid empty line!");
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

        return errorMessageList;
    }

    
    /**
     * Read bracket expression
     * 
     * @param errorMessageList the error list
     * @param sectionName the section name
     * @param input the input
     * @return the prepared input
     */
    protected String readBracketExpression(List<String> errorMessageList, String sectionName, String input) {
        if (input == null) {
            return input;
        }
        
        String result = input.trim();
        if (changelogConfig.isSupportBracketsAroundVersion()) {
            if (!result.isEmpty() && result.startsWith("[")) {
                result = result.substring(1);
            } else {
                addError(errorMessageList, result, "Invalid " + sectionName + " format " + result + ", expected to start with [");
            }
            
            if (!result.isEmpty() && result.endsWith("]")) {
                result = result.substring(0, result.length() - 1);
            } else {
                addError(errorMessageList, result, "Invalid " + sectionName + " format " + result + ", expected to end with ]");
            }
        } else if (result.startsWith("[") || result.endsWith("[")) {
            addError(errorMessageList, result, "Invalid " + sectionName + " format " + result + ", expected no brackets.");
        }
        
        return result;
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

        return list;
    }
}
