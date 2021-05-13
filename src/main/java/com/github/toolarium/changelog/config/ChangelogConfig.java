/*
 * ChangelogConfig.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.config;

import java.io.Serializable;
import java.util.Objects;
import jptools.util.RegularExpressionHolder;


/**
 * Defines the change-log parse and format configuration  
 * 
 * @author patrick
 */
public class ChangelogConfig implements Serializable {
    
    /** Regular expression: identifier in a content pattern */
    public static final String IDENTIFIER_IN_CONTENT = ".*[a-zA-Z]{0,3}+[-_]{1}+[0-9]{3,}+.*";
    
    /** Regular expression: link in a content pattern */
    public static final String LINK_IN_CONTENT = ".*((http?|https|ftp|file)://)+((W|w){3}.)?[a-zA-Z0-9]+(\\.[a-zA-Z])?.*";
    
    private static final long serialVersionUID = 2583751968740322695L;
    private char sectionCharacter;
    private char headerSeparator;
    private char itemSeparator;
    private boolean supportUnreleased;
    private boolean supportBracketsAroundVersion;
    private boolean supportReleaseInfo;
    private RegularExpressionHolder linkCommentCheckExpression;
    private RegularExpressionHolder idCommentCheckExpression;

    
    /**
     * Constructor for ChangelogConfig
     */
    public ChangelogConfig() {
        sectionCharacter = '#';
        headerSeparator = '-';
        itemSeparator = '-';
        supportUnreleased = true;
        supportBracketsAroundVersion = false;
        supportReleaseInfo = false;
        setLinkCommentCheckExpression(LINK_IN_CONTENT);
        setIdCommentCheckExpression(IDENTIFIER_IN_CONTENT);
    }
    

    /**
     * Constructor for ChangelogConfig
     * 
     * @param headerSeparator the header separator
     * @param itemSeparator the item separator
     * @param supportUnreleased true to support unreleased
     * @param supportBracketsAroundVersion true to support brackets around version
     * @param supportReleaseInfo the release information
     */
    public ChangelogConfig(char headerSeparator, char itemSeparator, boolean supportUnreleased, boolean supportBracketsAroundVersion, boolean supportReleaseInfo) {
        this();
        
        this.headerSeparator = headerSeparator;
        this.itemSeparator = itemSeparator;
        this.supportUnreleased = supportUnreleased;
        this.supportBracketsAroundVersion = supportBracketsAroundVersion;
        this.supportReleaseInfo = supportReleaseInfo;
    }


    /**
     * Get the section character
     * 
     * @return the section character
     */
    public char getSectionCharacter() {
        return sectionCharacter;
    }


    /**
     * Set the section separator
     * 
     * @param sectionCharacter the section separator
     */
    public void setSectionCharacter(char sectionCharacter) {
        this.sectionCharacter = sectionCharacter;
    }


    /**
     * Get the header character
     * 
     * @return the header character
     */
    public char getHeaderSeparator() {
        return headerSeparator;
    }


    /**
     * Set the header separator
     * 
     * @param headerSeparator the header separator
     */
    public void setHeaderSeparator(char headerSeparator) {
        this.headerSeparator = headerSeparator;
    }


    /**
     * Get the item character
     * 
     * @return the item character
     */
    public char getItemSeparator() {
        return itemSeparator;
    }


    /**
     * Set the item separator
     * 
     * @param itemSeparator the item separator
     */
    public void setItemSeparator(char itemSeparator) {
        this.itemSeparator = itemSeparator;
    }

    
    /**
     * Check if unreleased is supported
     * 
     * @return true if unreleased is supported
     */
    public boolean isSupportUnreleased() {
        return supportUnreleased;
    }


    /**
     * Set if unreleased is supported
     * 
     * @param supportUnreleased true if unreleased is supported
     */
    public void setSupportUnreleased(boolean supportUnreleased) {
        this.supportUnreleased = supportUnreleased;
    }

    
    /**
     * Check if brackets around version is supported
     * 
     * @return true if brackets around version
     */
    public boolean isSupportBracketsAroundVersion() {
        return supportBracketsAroundVersion;
    }


    /**
     * Set if brackets around version is supported
     * 
     * @param supportBracketsAroundVersion true if brackets around version is supported
     */
    public void setSupportBracketsAroundVersion(boolean supportBracketsAroundVersion) {
        this.supportBracketsAroundVersion = supportBracketsAroundVersion;
    }

    
    /**
     * Check if release information in header is supported
     * 
     * @return true if release information in header is supported
     */
    public boolean isSupportReleaseInfo() {
        return supportReleaseInfo;
    }

    /**
     * Set if release information in header is supported
     * 
     * @param supportReleaseInfo true if release information in header is supported
     */
    public void setSupportReleaseInfo(boolean supportReleaseInfo) {
        this.supportReleaseInfo = supportReleaseInfo;
    }
    
    /**
     * Check if links in comment are supported
     * 
     * @return true if links in comment are supported
     */
    public boolean isLinkInCommentEnabled() {
        return (linkCommentCheckExpression != null);
    }

    /**
     * Get the link comment check expression
     * 
     * @return the link comment check expression
     */
    public String getLinkCommentCheckExpression() {
        if (linkCommentCheckExpression == null) {
            return null;
        }
        
        return linkCommentCheckExpression.toString();
    }
    
    /**
     * Set the link comment check expression
     * 
     * @param linkCommentCheckExpression the link comment check expression
     */
    public void setLinkCommentCheckExpression(String linkCommentCheckExpression) {
        if (linkCommentCheckExpression == null) {
            this.linkCommentCheckExpression = null;
        } else {
            this.linkCommentCheckExpression = new RegularExpressionHolder(linkCommentCheckExpression);
        }
    }
    
    /**
     * Check if the text has a link
     * 
     * @param text the text to verify
     * @return true if it has a link
     */
    public boolean hasLinkInComment(String text) {
        if (text == null) {
            return false;
        }
        
        if (linkCommentCheckExpression != null) {
            return linkCommentCheckExpression.match(text);
        }

        return false;
    }    
    
    
    /**
     * Check if ID's in comment are supported
     * 
     * @return true if ID's in comment are supported
     */
    public boolean isIdInCommentEnabled() {
        return (idCommentCheckExpression != null);
    }

    /**
     * Get the id comment check expression
     * 
     * @return the id comment check expression
     */
    public String getIdCommentCheckExpression() {
        if (idCommentCheckExpression == null) {
            return null;
        }

        return idCommentCheckExpression.toString();
    }
    
    /**
     * Set the id comment check expression
     * 
     * @param idCommentCheckExpression the id comment check expression
     */
    public void setIdCommentCheckExpression(String idCommentCheckExpression) {
        if (idCommentCheckExpression == null) {
            this.idCommentCheckExpression = null;
        } else {
            this.idCommentCheckExpression = new RegularExpressionHolder(idCommentCheckExpression);
        }
    }
    
    /**
     * Check if the text has an id
     * 
     * @param text the text to verify
     * @return true if it has an identifier
     */
    public boolean hasIdInComment(String text) {
        if (text == null) {
            return false;
        }
        
        if (idCommentCheckExpression != null) {
            return idCommentCheckExpression.match(text);
        }

        return false;
    }

    
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(headerSeparator, itemSeparator, sectionCharacter, supportBracketsAroundVersion, supportReleaseInfo, supportUnreleased);
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChangelogConfig other = (ChangelogConfig) obj;
        return headerSeparator == other.headerSeparator && itemSeparator == other.itemSeparator && sectionCharacter == other.sectionCharacter && supportBracketsAroundVersion == other.supportBracketsAroundVersion
                && supportReleaseInfo == other.supportReleaseInfo && supportUnreleased == other.supportUnreleased;
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ChangelogConfig [sectionCharacter=" + sectionCharacter
               + ", headerSeparator=" + headerSeparator 
               + ", itemSeparator=" + itemSeparator 
               + ", supportUnreleased=" + supportUnreleased
               + ", supportBracketsAroundVersion=" + supportBracketsAroundVersion
               + ", supportReleaseInfo=" + supportReleaseInfo
               + ", linkCommentCheckExpression=" + linkCommentCheckExpression
               + ", idCommentCheckExpression=" + idCommentCheckExpression
               + "]";
    }
}
