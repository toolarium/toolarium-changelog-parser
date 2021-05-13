/*
 * ChangelogContentParser.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.parser.impl;

import com.github.toolarium.changelog.config.ChangelogConfig;
import java.util.ArrayList;
import java.util.List;
import jptools.parser.EOLException;
import jptools.parser.ParseException;
import jptools.parser.StopBytes;
import jptools.parser.StringParser;
import jptools.util.ByteArray;


/**
 * 
 * 
 * @author patrick
 */
public class ChangelogContentParser extends StringParser {
    /** The newline */
    public static final char NEWLINE = '\n';

    private static final long serialVersionUID = 12321321321321L;
    
    private StopBytes headerStopBytes = new StopBytes();
    private StopBytes newlineStopBytes = new StopBytes();
    private StopBytes descriptionStopBytes = new StopBytes();

    private char sectionCharacter;
    private String itemSeparator;
    private byte headerSeparator;

    
    /**
     * Constructor for ChangelogParser
     */
    public ChangelogContentParser() {
        this(new ChangelogConfig());
    }

    
    /**
     * Constructor for ChangelogParser
     * 
     * @param changelogConfig the change-log configuration for formatting
     */
    public ChangelogContentParser(ChangelogConfig changelogConfig) {
        super();

        this.sectionCharacter = changelogConfig.getSectionCharacter();
        this.itemSeparator = "" + changelogConfig.getItemSeparator();
        this.headerSeparator = (byte) changelogConfig.getHeaderSeparator();

        headerStopBytes.addStopBytes(" ");
        headerStopBytes.addStopBytes("" + NEWLINE);
        descriptionStopBytes.addStopBytes("" + NEWLINE);
        descriptionStopBytes.addStopBytes("" + sectionCharacter);
        newlineStopBytes.addStopBytes("" + NEWLINE);
    }

    
    /**
     * @see jptools.parser.StringParser#init(java.lang.String)
     */
    @Override
    public void init(String data) {
        super.init(data.replace("\r", ""));
        super.addStopBytes("" + sectionCharacter);
    }


    /**
     * @see jptools.parser.Parser#init(jptools.util.ByteArray)
     */
    @Override
    public void init(ByteArray data) {
        super.init(data.replace(ByteArray.CR, new ByteArray("")));
        super.addStopBytes("" + sectionCharacter);
    }

    
    /**
     * Read the version
     *
     * @return the version
     */
    public String readVersion() {
        if (!isEOL()) {
            readBlanks();
            String version = readText(headerStopBytes);
            return version.trim();
        }

        return "";
    }


    /**
     * Read the date
     *
     * @return the date
     */
    public String readDate() {
        if (!isEOL()) {
            readBlanks();
            String version = readText(headerStopBytes);
            return version.trim();
        }

        return "";
    }

    
    /**
     * Read the header end
     *
     * @return the header end
     */
    public String readHeaderEnd() {
        if (!isEOL()) {
            return readEOL();
        }

        return "";
    }

    
    /**
     * Read the description
     *
     * @return the description
     */
    public String readDescription() {
        return readChangelogText();
    }

    
    /**
     * Read the header separator.
     */
    public void readHeaderSeparator() {
        try {
            readBlanks();
            if (!isEOL() && getCurrentByte() == headerSeparator) {
                readNext();
            }

            readBlanks();

        } catch (ParseException e) {
            // NOP
        }
    }

    
    /**
     * Read until end of line.
     *
     * @return the content
     */
    public String readEOL() {
        if (!isEOL()) {
            String result = readText(newlineStopBytes);
            try {
                readNext();
            } catch (EOLException e) {
                // NOP
            }

            return result;
        }

        return "";
    }

    
    /**
     * Read the changelog separator
     *
     * @return the changelog separator
     */
    public String readChangelogSeparator() {
        String sep = "";
        byte stopByte = (byte) sectionCharacter;
        try {
            do {
                sep += "" + readSeparator();
            } while (!isEOL() && stopByte == getCurrentByte());
        } catch (EOLException e) {
            // NOP
        }

        return sep;
    }
    
    
    /**
     * Read section items
     *
     * @return the section item
     */
    public List<String> readItems() {
        String itemContent = readChangelogText();

        List<String> result = new ArrayList<>();
        if (itemContent != null && !itemContent.isEmpty()) {
            String[] itemSplit = itemContent.split("" + NEWLINE);
            if (itemSplit != null) {
                String currentItem = "";
                for (int i = 0; i < itemSplit.length; i++) {
                    String item = itemSplit[i];
                    if (item.startsWith(itemSeparator)) {
                        if (!currentItem.isEmpty()) {
                            result.add(currentItem);
                        }

                        currentItem = item.substring(1).stripLeading();
                    } else {
                        currentItem += NEWLINE + item;
                    }
                }

                if (!currentItem.isEmpty()) {
                    result.add(currentItem);
                }
            }
        }
        
        return result;
    }
    
    
    /**
     * Read the description
     *
     * @return the description
     */
    public String readChangelogText() {
        if (isEOL()) {
            return "";
        }

        String text = "";
        boolean end = false;
        while (!isEOL() && !end) {
            String result = readBytes(descriptionStopBytes).toString();
            if (!result.isEmpty()) {
                text += result;
            }
            
            try {
                if (getCurrentByte() == (byte) NEWLINE) {
                    String s = readSeparator(descriptionStopBytes).toString();
                    text += s;
                } else { // separator
                    if (!result.isEmpty()) {
                        text += readSeparator(descriptionStopBytes);
                    } else {
                        end = true;
                    }
                }
            } catch (EOLException e) {
                // NOP
            }
        }

        while (!text.isEmpty() && text.endsWith("" + NEWLINE)) {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }
}
