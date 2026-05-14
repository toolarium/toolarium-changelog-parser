/*
 * ChangelogContentParser.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.parser.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Defines the change-log content parser.
 *
 * @author patrick
 */
public class ChangelogContentParser {
    /** The newline */
    public static final char NEWLINE = '\n';

    private final Set<Character> headerStopChars = new HashSet<>();
    private final Set<Character> newlineStopChars = new HashSet<>();
    private final Set<Character> descriptionStopChars = new HashSet<>();
    private final Set<Character> defaultStopChars = new HashSet<>();

    private char sectionCharacter;
    private String data;
    private int pos;


    /**
     * Constructor for ChangelogParser
     */
    public ChangelogContentParser() {
        this.sectionCharacter = '#';

        headerStopChars.add(' ');
        headerStopChars.add(NEWLINE);
        descriptionStopChars.add(NEWLINE);
        descriptionStopChars.add(sectionCharacter);
        newlineStopChars.add(NEWLINE);
    }


    /**
     * Initialize the parser with the given data.
     *
     * @param input the input data
     */
    public void init(String input) {
        this.data = input.replace("\r", "");
        this.pos = 0;
        defaultStopChars.add(sectionCharacter);
    }


    /**
     * Check if end of input is reached.
     *
     * @return true if at end
     */
    public boolean isEOL() {
        return pos >= data.length();
    }


    /**
     * Read the version
     *
     * @return the version
     */
    public String readVersion() {
        if (!isEOL()) {
            readBlanks();
            String version = readText(headerStopChars);

            if (version.startsWith("[") && !version.endsWith("]")) {
                // read everything until closing bracket and the next stop char
                int closeBracket = data.indexOf(']', pos);
                if (closeBracket >= 0) {
                    version += data.substring(pos, closeBracket + 1);
                    pos = closeBracket + 1;

                    // also read a trailing link like (url)
                    if (!isEOL() && currentChar() == '(') {
                        int closeParen = data.indexOf(')', pos);
                        if (closeParen >= 0) {
                            version += data.substring(pos, closeParen + 1);
                            pos = closeParen + 1;
                        }
                    }
                }
            }

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
            String version = readText(headerStopChars);
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
     *
     * @return the read separator
     */
    public Character readHeaderSeparator() {
        Character result = null;

        try {
            readBlanks();
            if (!isEOL() && (currentChar() == '-' || currentChar() == '/')) {
                result = currentChar();
                advance();
            }

            readBlanks();
        } catch (IndexOutOfBoundsException e) {
            // end of parseable content reached
        }

        return result;
    }


    /**
     * Read until end of line.
     *
     * @return the content
     */
    public String readEOL() {
        if (!isEOL()) {
            String result = readText(newlineStopChars);
            if (!isEOL()) {
                pos++;
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
        // skip leading newlines
        while (!isEOL() && currentChar() == NEWLINE) {
            pos++;
        }

        StringBuilder sep = new StringBuilder();
        while (!isEOL() && currentChar() == sectionCharacter) {
            sep.append(readSeparatorChar());
        }

        readBlanks();
        return sep.toString();
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
                    if (item.startsWith("-") || item.startsWith("*")) {
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

        StringBuilder text = new StringBuilder();
        boolean end = false;
        while (!isEOL() && !end) {
            String result = readUntil(descriptionStopChars);
            if (!result.isEmpty()) {
                text.append(result);
            }

            try {
                if (!isEOL() && currentChar() == NEWLINE) {
                    String s = readSeparatorWithStopChars(descriptionStopChars);
                    text.append(s);
                } else { // separator
                    if (!result.isEmpty()) {
                        text.append(readSeparatorWithStopChars(descriptionStopChars));
                    } else {
                        end = true;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                // end of line reached
            }
        }

        String result = text.toString();
        while (!result.isEmpty() && result.endsWith("" + NEWLINE)) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }


    //////////////////////////////////////////////////////////////////////////
    // Internal parser methods
    //////////////////////////////////////////////////////////////////////////

    /**
     * Get the current character.
     *
     * @return the current character
     */
    private char currentChar() {
        return data.charAt(pos);
    }


    /**
     * Advance position by one.
     */
    private void advance() {
        pos++;
    }


    /**
     * Skip blanks (spaces and tabs).
     */
    private void readBlanks() {
        while (!isEOL() && (currentChar() == ' ' || currentChar() == '\t')) {
            pos++;
        }
    }


    /**
     * Read text until a stop character or default stop character is encountered.
     *
     * @param stopChars the stop characters
     * @return the text read
     */
    private String readText(Set<Character> stopChars) {
        int start = pos;
        while (!isEOL() && !stopChars.contains(currentChar()) && !defaultStopChars.contains(currentChar())) {
            pos++;
        }
        return data.substring(start, pos);
    }


    /**
     * Read bytes until a stop character or default stop character is encountered.
     * Same as readText but kept for semantic clarity.
     *
     * @param stopChars the stop characters
     * @return the text read
     */
    private String readUntil(Set<Character> stopChars) {
        return readText(stopChars);
    }


    /**
     * Read a single separator character and advance.
     *
     * @return the separator character
     */
    private char readSeparatorChar() {
        char ch = currentChar();
        pos++;
        return ch;
    }


    /**
     * Read separator characters until a non-stop character is found.
     * Stops after consuming a newline to avoid crossing into the next section.
     *
     * @param stopChars the stop characters
     * @return the separator text read
     */
    private String readSeparatorWithStopChars(Set<Character> stopChars) {
        int start = pos;
        while (!isEOL() && (stopChars.contains(currentChar()) || defaultStopChars.contains(currentChar()))) {
            boolean isNewline = currentChar() == NEWLINE;
            pos++;
            if (isNewline) {
                break;
            }
        }
        return data.substring(start, pos);
    }
}
