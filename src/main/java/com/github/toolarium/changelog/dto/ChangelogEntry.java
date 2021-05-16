/*
 * ChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.dto;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * The change-log entry contains beside the version and release date an optional description and a list with {@link ChangelogSection}s.
 * 
 * @author patrick
 */
public class ChangelogEntry implements Comparable<ChangelogEntry>, Serializable {
    private static final long serialVersionUID = 23424823094L;
    private ChangelogReleaseVersion releaseVersion;
    private URL releaseLink;
    private boolean hasBracketsAroundVersion;
    private LocalDate releaseDate;
    private String releaseDescription;
    private String releaseInfo;
    private boolean isReleased;
    private boolean wasYanked;
    private List<ChangelogSection> sectionList;

    
    /**
     * Constructor for ChangelogEntry
     */
    public ChangelogEntry() {
        sectionList = new ArrayList<ChangelogSection>();
    }

    
    /**
     * Constructor for ChangelogEntry
     * 
     * @param releaseVersion the release version 
     * @param releaseDate the release date
     */
    public ChangelogEntry(ChangelogReleaseVersion releaseVersion, LocalDate releaseDate) {
        this(releaseVersion, releaseDate, null, null);
    }    
    
    
    /**
     * Constructor for ChangelogEntry
     * 
     * @param releaseVersion the release version 
     * @param releaseDate the release date
     * @param releaseDescription the release description
     * @param releaseInfo the release information 
     */
    public ChangelogEntry(ChangelogReleaseVersion releaseVersion, LocalDate releaseDate, String releaseDescription, String releaseInfo) {
        this(releaseVersion, releaseDate, releaseDescription, releaseInfo, true, false, new ArrayList<ChangelogSection>());
    }
    
    
    /**
     * Constructor for ChangelogEntry
     * 
     * @param releaseVersion the release version 
     * @param releaseDate the release date
     * @param releaseDescription the release description
     * @param releaseInfo the additional release information 
     * @param isReleased true if it is released otherwise false
     * @param wasYanked true if it is yanked otherwise false
     * @param sectionList the section list
     */
    public ChangelogEntry(ChangelogReleaseVersion releaseVersion, LocalDate releaseDate, String releaseDescription, String releaseInfo, boolean isReleased, boolean wasYanked, List<ChangelogSection> sectionList) {
        this.releaseVersion = releaseVersion;
        this.releaseDate = releaseDate;
        this.releaseDescription = releaseDescription;
        this.releaseInfo = releaseInfo;
        this.isReleased = isReleased;
        this.wasYanked = wasYanked;
        this.sectionList = sectionList;
    }

    
    /**
     * Get the release version
     *
     * @return the release version
     */
    public ChangelogReleaseVersion getReleaseVersion() {
        return releaseVersion;
    }

    
    /**
     * Set the release version
     *
     * @param releaseVersion the release version
     */
    public void setReleaseVersion(ChangelogReleaseVersion releaseVersion) {
        if (releaseVersion == null) {
            isReleased = false;
            return;
        }

        isReleased = true;
        this.releaseVersion = releaseVersion;
    }

    
    /**
     * Get the release link
     *
     * @return the release link
     */
    public URL getReleaseLink() {
        return releaseLink;
    }

    
    /**
     * Set the release link
     *
     * @param releaseLink the release link
     */
    public void setReleaseLink(URL releaseLink) {
        this.releaseLink = releaseLink;
    }

    
    /**
     * Get has brackets around version
     *
     * @return true if it has brackets around version
     */
    public boolean hasBracketsAroundVersion() {
        return hasBracketsAroundVersion;
    }


    /**
     * Set has brackets around version
     *
     * @param hasBracketsAroundVersion has brackets around version
     */
    public void setHasBracketsAroundVersion(boolean hasBracketsAroundVersion) {
        this.hasBracketsAroundVersion = hasBracketsAroundVersion;
    }


    /**
     * Get the release date 
     *
     * @return the release date or null if no date is set
     */
    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    
    /**
     * Set the release date
     * 
     * @param date the release date
     */
    public void setReleaseDate(String date) {
        if (date != null && !date.isEmpty()) {
            this.releaseDate = LocalDate.parse(date);
        }
    }
    
    /**
     * Get the release description.
     *
     * @return a description or null
     */
    public String getDescription() { 
        return releaseDescription; 
    }

    
    /**
     * Set the release description.
     *
     * @param releaseDescription a description or null
     */
    public void setDescription(String releaseDescription) {
        this.releaseDescription = releaseDescription;
    }

    
    /**
     * Get the release information.
     *
     * @return a release information or null
     */
    public String getInfo()  { 
        return releaseInfo; 
    }

    
    /**
     * Set the release information.
     *
     * @param releaseInfo a release information or null
     */
    public void setInfo(String releaseInfo) {
        this.releaseInfo = releaseInfo;
    }

    
    /**
     * Get the section list.
     *
     * @return the section list
     */
    public List<ChangelogSection> getSectionList() {
        return sectionList;
    }

    
    /**
     * Describes if it is relased or is unreleased.
     * 
     * @return true if the entry version/date heading is "Unreleased"
     */
    public boolean isReleased() { 
        return isReleased; 
    }

    
    /**
     * Set it it was considered as yanked.
     */
    public void setWasYanked() {
        wasYanked = true;
    }
    
    
    /**
     * Describes if it is considered yanked. Marked entries in the change-log with [YANKED] in the version/date heading have been yanked. 
     * A yanked release should have a description indicating why it was yanked but this description is not required.
     *
     * @return true if the entry was marked with the [YANKED] tag
     */
    public boolean wasYanked() {
        return wasYanked;
    }


    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ChangelogEntry o) {
        if (!isReleased || !o.isReleased) {
            if (!isReleased && o.isReleased) {
                return -1;
            }
            
            if (!isReleased) {
                return 0;
            }
            
            return 1;
        }

        return -releaseVersion.compareTo(o.releaseVersion);
    }
    



    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(hasBracketsAroundVersion, isReleased, releaseDate, releaseDescription, releaseInfo, releaseLink, releaseVersion, sectionList, wasYanked);
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
        ChangelogEntry other = (ChangelogEntry) obj;
        return hasBracketsAroundVersion == other.hasBracketsAroundVersion && isReleased == other.isReleased && Objects.equals(releaseDate, other.releaseDate) && Objects.equals(releaseDescription, other.releaseDescription)
                && Objects.equals(releaseInfo, other.releaseInfo) && Objects.equals(releaseLink, other.releaseLink) && Objects.equals(releaseVersion, other.releaseVersion) && Objects.equals(sectionList, other.sectionList)
                && wasYanked == other.wasYanked;
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ChangelogEntry [releaseVersion=" + releaseVersion
               + ", releaseDate=" + releaseDate + ", releaseLink=" + releaseLink + ", releaseDescription=" + releaseDescription + ", releaseInfo=" + releaseInfo + ", isReleased=" + isReleased
               + ", wasYanked=" + wasYanked + ", sectionList=" + sectionList
               + "]";
    }
}
