/*
 * ChangelogConfig.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.config;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Defines the change-log validation and format configuration.  
 * 
 * @author patrick
 */
public class ChangelogConfig implements Serializable {
    
    /** Regular expression: identifier in a content pattern */
    public static final String IDENTIFIER_IN_CONTENT = "[a-zA-Z0_9]{0,3}+[\\-\\_\\:]{1}+[0-9]{3,}+";
    
    /** Regular expression: link in a content pattern */
    public static final String LINK_IN_CONTENT = "((http?|https|ftp|file)://)+((W|w){3}.)?[a-zA-Z0-9\\-\\_]+\\.([a-zA-Z0-9\\-\\.\\_\\~\\/\\%])+";
    
    private static final long serialVersionUID = 2583751968740322695L;
    private char sectionCharacter;
    private char headerSeparator;
    private char itemSeparator;
    private boolean supportUnreleased;
    private boolean supportEmptySection;
    private boolean supportSpaceAroundVersion;
    private boolean supportBracketsAroundVersion;
    private boolean supportReleaseLink;
    private boolean supportReleaseInfo;
    private boolean supportLinkInDescription;
    private boolean supportIdListOnEndOfTheComment;
    private Pattern linkCommentCheckPattern;
    private Pattern idCommentCheckPattern;

    
    /**
     * Constructor for ChangelogConfig
     */
    public ChangelogConfig() {
        sectionCharacter = '#';
        headerSeparator = '-';
        itemSeparator = '-';
        supportUnreleased = true;
        supportEmptySection = false;
        supportSpaceAroundVersion = true;
        supportBracketsAroundVersion = true;
        supportReleaseLink = true;
        supportReleaseInfo = true;
        supportLinkInDescription = true;
        supportIdListOnEndOfTheComment = true;
        setLinkCommentCheckExpression(LINK_IN_CONTENT);
        setIdCommentCheckExpression(IDENTIFIER_IN_CONTENT);
    }
    

    /**
     * Constructor for ChangelogConfig
     * 
     * @param headerSeparator the header separator to validate strict; otherwise null
     * @param itemSeparator the item separator to validate strict; otherwise null
     * @param supportUnreleased true to support unreleased
     * @param supportEmptySection true to support empty sections
     * @param supportSpaceAroundVersion true if space around version is supported
     * @param supportBracketsAroundVersion true to support brackets around version
     * @param supportReleaseLink true to support release link
     * @param supportReleaseInfo the release information
     * @param supportLinkInDescription true to support link in description
     * @param supportIdListOnEndOfTheComment true to support id as a comma separated list on end of the comment
     */
    public ChangelogConfig(char headerSeparator, 
                           char itemSeparator, 
                           boolean supportUnreleased, 
                           boolean supportEmptySection,
                           boolean supportSpaceAroundVersion,
                           boolean supportBracketsAroundVersion,
                           boolean supportReleaseLink,
                           boolean supportReleaseInfo, 
                           boolean supportLinkInDescription,
                           boolean supportIdListOnEndOfTheComment) {
        this();
        
        this.headerSeparator = headerSeparator;
        this.itemSeparator = itemSeparator;
        this.supportUnreleased = supportUnreleased;
        this.supportEmptySection = supportEmptySection;
        this.supportSpaceAroundVersion = supportSpaceAroundVersion;
        this.supportBracketsAroundVersion = supportBracketsAroundVersion;
        this.supportReleaseLink = supportReleaseLink;
        this.supportReleaseInfo = supportReleaseInfo;
        this.supportLinkInDescription = supportLinkInDescription;
        this.supportIdListOnEndOfTheComment = supportIdListOnEndOfTheComment;
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
     * Check if empty section is supported
     * 
     * @return true if empty section is supported
     */
    public boolean isSupportEmptySection() {
        return supportEmptySection;
    }


    /**
     * Set if empty section is supported
     * 
     * @param supportEmptySection true to support empty section
     */
    public void setSupportEmptySection(boolean supportEmptySection) {
        this.supportEmptySection = supportEmptySection;
    }

    
    /**
     * Check if space around version is supported
     * 
     * @return true if space around version is supported
     */
    public boolean isSupportSpaceAroundVersion() {
        return supportSpaceAroundVersion;
    }


    /**
     * Set if space around version is supported
     * 
     * @param supportSpaceAroundVersion true if space around version is supported
     */
    public void setSupportSpaceAroundVersion(boolean supportSpaceAroundVersion) {
        this.supportSpaceAroundVersion = supportSpaceAroundVersion;
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
     * Check if release link is supported
     * 
     * @return true true if release link is supported
     */
    public boolean isSupportReleaseLink() {
        return supportReleaseLink;
    }


    /**
     * Set if release link is supported
     * 
     * @param supportReleaseLink true if release link is supported
     */
    public void setSupportReleaseLink(boolean supportReleaseLink) {
        this.supportReleaseLink = supportReleaseLink;
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
     * Check if link in description is supported.
     * 
     * @return true if link in description is supported
     */
    public boolean isSupportLinkInDescription() {
        return supportLinkInDescription;
    }

    
    /**
     * Set if link in description is supported.
     * 
     * @param supportLinkInDescription true to enable link in description
     */
    public void setSupportLinkInDescription(boolean supportLinkInDescription) {
        this.supportLinkInDescription = supportLinkInDescription;
    }
    
    
    /**
     * Check if links in comment are supported
     * 
     * @return true if links in comment are supported
     */
    public boolean isLinkInCommentEnabled() {
        return (linkCommentCheckPattern != null);
    }

    
    /**
     * Get the link comment check expression
     * 
     * @return the link comment check expression
     */
    public String getLinkCommentCheckExpression() {
        if (linkCommentCheckPattern == null) {
            return null;
        }
        
        return linkCommentCheckPattern.toString();
    }
    
    
    /**
     * Set the link comment check expression
     * 
     * @param linkCommentCheckExpression the link comment check expression
     */
    public void setLinkCommentCheckExpression(String linkCommentCheckExpression) {
        if (linkCommentCheckExpression == null) {
            this.linkCommentCheckPattern = null;
        } else {
            this.linkCommentCheckPattern = Pattern.compile(linkCommentCheckExpression);
        }
    }
    
    /**
     * Check if the text has a link
     * 
     * @param text the text to verify
     * @return the link or null
     */
    public String hasLinkInComment(String text) {
        if (text == null) {
            return null;
        }
        
        String link = null;
        if (linkCommentCheckPattern != null) {
            Matcher matcher = linkCommentCheckPattern.matcher(text);
            if (matcher.find()) {
                link = matcher.group();
            }
        }

        return link;
    }    
    
    
    /**
     * Check if id as comma separated list on end of the comment is allowed.
     * 
     * @return true if id as comma separated list on end of the comment is allowed.
     */
    public boolean isSupportIdListOnEndOfTheComment() {
        return supportIdListOnEndOfTheComment;
    }

    
    /**
     * Set if id as comma separated list on end of the comment is allowed.
     * 
     * @param supportIdListOnEndOfTheComment true to enable id as comma separated list on end of the comment is allowed
     */
    public void setSupportIdListOnEndOfTheComment(boolean supportIdListOnEndOfTheComment) {
        this.supportIdListOnEndOfTheComment = supportIdListOnEndOfTheComment;
    }
    
    
    /**
     * Check if ID's in comment are supported
     * 
     * @return true if ID's in comment are supported
     */
    public boolean isIdInCommentEnabled() {
        return (idCommentCheckPattern != null);
    }

    
    /**
     * Get the id comment check expression
     * 
     * @return the id comment check expression
     */
    public String getIdCommentCheckExpression() {
        if (idCommentCheckPattern == null) {
            return null;
        }

        return idCommentCheckPattern.toString();
    }
    
    
    /**
     * Set the id comment check expression
     * 
     * @param idCommentCheckExpression the id comment check expression
     */
    public void setIdCommentCheckExpression(String idCommentCheckExpression) {
        if (idCommentCheckExpression == null) {
            this.idCommentCheckPattern = null;
        } else {
            this.idCommentCheckPattern = Pattern.compile(idCommentCheckExpression);
        }
    }
    
    
    /**
     * Check if the text has an id
     * 
     * @param text the text to verify
     * @return true if it has an identifier
     */
    public String hasIdInComment(String text) {
        if (text == null) {
            return null;
        }
        
        String id = null;
        if (idCommentCheckPattern != null) {
            Matcher matcher = idCommentCheckPattern.matcher(text);
            if (matcher.find()) {
                id = matcher.group();
            }
        }

        return id;
    }




    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(headerSeparator, idCommentCheckPattern, itemSeparator, linkCommentCheckPattern, sectionCharacter, supportSpaceAroundVersion, supportBracketsAroundVersion, supportLinkInDescription, supportIdListOnEndOfTheComment, 
                            supportReleaseInfo, supportReleaseLink, supportUnreleased, supportEmptySection);
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
        return headerSeparator == other.headerSeparator && Objects.equals(idCommentCheckPattern, other.idCommentCheckPattern) && itemSeparator == other.itemSeparator && Objects.equals(linkCommentCheckPattern, other.linkCommentCheckPattern)
                && sectionCharacter == other.sectionCharacter && supportSpaceAroundVersion == other.supportSpaceAroundVersion
                && supportBracketsAroundVersion == other.supportBracketsAroundVersion && supportLinkInDescription == other.supportLinkInDescription 
                && supportIdListOnEndOfTheComment == other.supportIdListOnEndOfTheComment && supportReleaseInfo == other.supportReleaseInfo && supportReleaseLink == other.supportReleaseLink 
                && supportUnreleased == other.supportUnreleased && supportEmptySection == other.supportEmptySection;
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
               + ", supportEmptySection=" + supportEmptySection
               + ", supportSpaceAroundVersion=" + supportSpaceAroundVersion
               + ", supportBracketsAroundVersion=" + supportBracketsAroundVersion
               + ", supportReleaseLink=" + supportReleaseLink
               + ", supportReleaseInfo=" + supportReleaseInfo
               + ", supportLinkInDescription=" + supportLinkInDescription
               + ", supportIdListOnEndOfTheComment=" + supportIdListOnEndOfTheComment               
               + ", linkCommentCheckExpression=" + linkCommentCheckPattern
               + ", idCommentCheckExpression=" + idCommentCheckPattern
               + "]";
    }
}
