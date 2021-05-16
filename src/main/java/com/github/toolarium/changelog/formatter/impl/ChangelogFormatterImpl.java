/*
 * ChangelogFormatter.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.formatter.impl;

import com.github.toolarium.changelog.config.ChangelogConfig;
import com.github.toolarium.changelog.dto.Changelog;
import com.github.toolarium.changelog.dto.ChangelogEntry;
import com.github.toolarium.changelog.dto.ChangelogSection;
import com.github.toolarium.changelog.formatter.IChangelogFormatter;
import java.util.Collections;
import java.util.List;


/**
 * Implements a change-log formatter
 * 
 * @author patrick
 */
public class ChangelogFormatterImpl implements IChangelogFormatter {
    private static final String SPACE = " ";
    private ChangelogConfig changelogConfig;

    
    /**
     * Constructor for ChangelogParser
     */
    public ChangelogFormatterImpl() {
        this(new ChangelogConfig());
    }
    
    
    /**
     * Constructor for ChangelogParser
     * 
     * @param changelogConfig the change-log configuration for formatting
     */
    public ChangelogFormatterImpl(ChangelogConfig changelogConfig) {
        this.changelogConfig = changelogConfig;
    }


    /**
     * @see com.github.toolarium.changelog.formatter.IChangelogFormatter#format(com.github.toolarium.changelog.dto.Changelog)
     */
    @Override
    public String format(Changelog changelog) {
        String firstSection = "" + changelogConfig.getSectionCharacter();
        String secondSection = "" + firstSection + changelogConfig.getSectionCharacter();
        String thirdSection = "" + secondSection + changelogConfig.getSectionCharacter();
        
        StringBuilder result = new StringBuilder();
        append(result, firstSection + SPACE + changelog.getProjectName());
        newline(result);

        if (changelog.getDescription() != null && !changelog.getDescription().isEmpty()) {
            append(result, changelog.getDescription());
            newline(result);
        }

        List<ChangelogEntry> entries = changelog.getEntries();
        if (entries != null) {
            Collections.sort(entries);
            for (ChangelogEntry entry : entries) {
                newline(result);

                String version = null;
                if (entry.getReleaseVersion() != null && entry.isReleased()) {
                    version = entry.getReleaseVersion().toString();
                }

                if (changelogConfig.isSupportUnreleased() && (version == null || version.isEmpty())) {
                    version = "Unreleased";
                }
                
                if (version != null) {
                    if (changelogConfig.isSupportReleaseLink() && entry.getReleaseLink() != null) {
                        append(result, secondSection + SPACE + "[" + version + "]");
                        append(result, "(" + entry.getReleaseLink().toExternalForm() + ")");
                    } else {
                        append(result, secondSection + SPACE + prepareBracketExpression(changelogConfig, version));
                    }
                    
                    if (entry.getReleaseDate() != null) {
                        append(result, SPACE + changelogConfig.getHeaderSeparator());
                        append(result, SPACE + entry.getReleaseDate());

                        if (entry.wasYanked()) {
                            append(result, SPACE + changelogConfig.getHeaderSeparator() + SPACE + prepareBracketExpression(changelogConfig, "YANKED"));
                        }

                        if (entry.getInfo() != null && !entry.getInfo().isBlank()) {
                            append(result, SPACE + changelogConfig.getHeaderSeparator());
                            append(result, SPACE + entry.getInfo());
                        }
                    }

                    newline(result);

                    List<ChangelogSection> sectionList = entry.getSectionList();
                    boolean hasSections = sectionList != null && !sectionList.isEmpty();
                    if (entry.getDescription() != null && !entry.getDescription().isEmpty()) {
                        append(result, entry.getDescription());
                        newline(result);

                        if (hasSections) {
                            newline(result);
                        }
                    }

                    if (hasSections) {
                        int count = 0;
                        for (ChangelogSection section : sectionList) {
                            if (count > 0) {
                                newline(result);
                            }

                            append(result, thirdSection + SPACE + section.getChangeType().getTypeName());
                            newline(result);

                            List<String> commentList = section.getChangeCommentList();
                            if (commentList != null) {
                                for (String comment : commentList) {
                                    append(result, "" + changelogConfig.getItemSeparator() + SPACE + comment);
                                    newline(result);
                                }
                            }

                            count++;
                        }
                    }
                }
            }
        }

        return result.toString();
    }

    
    /**
     * Prepare bracket expression
     * 
     * @param config the configuration
     * @param input the input
     * @return the result
     */
    protected String prepareBracketExpression(ChangelogConfig config, String input) {
        if (config.isSupportBracketsAroundVersion()) {
            return "[" + input + "]";
        }
        
        return input;
    }
    
    
    /**
     * Append a string
     * 
     * @param result the result
     * @param value the value to add
     */
    protected void append(StringBuilder result, String value) {
        if (value != null && !value.isEmpty()) {
            result.append(value);
        }
    }
    
    
    /**
     * Adds a newline
     * 
     * @param result the result
     */
    protected void newline(StringBuilder result) {
        result.append("\n");
    }
}
