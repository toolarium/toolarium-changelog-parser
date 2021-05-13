/*
 * ChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jptools.parser.ParseException;
import jptools.util.version.Version;


/**
 * The change-log entry 
 * 
 * @author patrick
 */
public class ChangelogEntry implements Comparable<ChangelogEntry>, Serializable {
    private static final long serialVersionUID = 23424823094L;
    private Version releaseVersion;
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
    public ChangelogEntry(Version releaseVersion, LocalDate releaseDate) {
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
    public ChangelogEntry(Version releaseVersion, LocalDate releaseDate, String releaseDescription, String releaseInfo) {
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
    public ChangelogEntry(Version releaseVersion, LocalDate releaseDate, String releaseDescription, String releaseInfo, boolean isReleased, boolean wasYanked, List<ChangelogSection> sectionList) {
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
    public Version getReleaseVersion() {
        return releaseVersion;
    }

    
    /**
     * Get the release version
     *
     * @param inputVersion the release version
     * @throws ParseException In case of an invalid version
     */
    public void setReleaseVersion(String inputVersion) throws ParseException {
        if (inputVersion == null || inputVersion.isBlank()) {
            isReleased = false;
            return;
        }

        String version = inputVersion.trim();
        if ("Unreleased".equalsIgnoreCase(version)) {
            isReleased = false;
            return;
        }

        this.isReleased = true;
        this.releaseVersion = new Version(version);
        if ((releaseVersion.getMajorInfo() != null && !releaseVersion.getMajorInfo().isEmpty()) 
                || (releaseVersion.getMinorInfo() != null && !releaseVersion.getMinorInfo().isEmpty())
                || (releaseVersion.getBuildInfo() != null && !releaseVersion.getBuildInfo().isEmpty())) {
            throw new ParseException("Invalid version format: " + inputVersion);
        }
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
        return Objects.hash(isReleased, releaseDate, releaseDescription, releaseInfo, releaseVersion, sectionList, wasYanked);
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
        return isReleased == other.isReleased && Objects.equals(releaseDate, other.releaseDate) && Objects.equals(releaseDescription, other.releaseDescription) && Objects.equals(releaseInfo, other.releaseInfo)
                && Objects.equals(releaseVersion, other.releaseVersion) && Objects.equals(sectionList, other.sectionList) && wasYanked == other.wasYanked;
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ChangelogEntry [releaseVersion=" + releaseVersion
               + ", releaseDate=" + releaseDate + ", releaseDescription=" + releaseDescription + ", releaseInfo=" + releaseInfo + ", isReleased=" + isReleased
               + ", wasYanked=" + wasYanked + ", sectionList=" + sectionList
               + "]";
    }

    
    /**
     * The builder
     */
    public static class Builder {
        private Version releaseVersion;
        private LocalDate releaseDate;
        private String releaseDescription;
        private String releaseInfo;
        private boolean isReleased = true;
        private boolean wasYanked;
        private List<ChangelogSection> sectionList = new ArrayList<>();


        /**
         * Constructor for Builder
         */
        public Builder() {
        }        
        
        /**
         * Set the release version
         * 
         * @param inputVersion the release version
         * @return the object
         * @throws ParseException In case of an invalid version string
         */
        public Builder version(String inputVersion) throws ParseException {
            if (inputVersion == null || inputVersion.isBlank()) {
                return setUnreleased();
            }
            
            String version = inputVersion.trim();
            if ("Unreleased".equalsIgnoreCase(version)) {
                return setUnreleased();
            }
            
            this.releaseVersion = new Version(version);
            if ((releaseVersion.getMajorInfo() != null && !releaseVersion.getMajorInfo().isEmpty()) || (releaseVersion.getMinorInfo() != null && !releaseVersion.getMinorInfo().isEmpty()) 
                    || (releaseVersion.getBuildInfo() != null && !releaseVersion.getBuildInfo().isEmpty())) {
                throw new ParseException("Invalid version format: " + inputVersion);
            }

            return this;
        }

        /**
         * Set the release date
         * 
         * @param date the release date
         * @return the object
         * @throws DateTimeParseException In case of an invalid date string
         */
        public Builder date(String date) throws DateTimeParseException {
            if (date != null && !date.isEmpty()) {
                this.releaseDate = LocalDate.parse(date);
            }
            
            return this;
        }

        
        /**
         * Set the release description
         * 
         * @param description the release description
         * @return the object
         */
        public Builder description(String description) {
            this.releaseDescription = description;
            return this;
        }        
        
        
        /**
         * Set the release info
         * 
         * @param info the release info
         * @return the object
         */
        public Builder info(String info) {
            this.releaseInfo = info;
            return this;
        }

        
        /**
         * Add a section
         * 
         * @param section the section
         * @return the object
         */
        public Builder addSection(ChangelogSection section) {
            sectionList.add(section);
            return this;
        }

        
        /**
         * Set released
         * 
         * @return the object
         */
        public Builder setReleased() {
            isReleased = true;
            return this;
        }


        /**
         * Set unreleased
         * 
         * @return the object
         */
        public Builder setUnreleased() {
            isReleased = false;
            return this;
        }


        /**
         * Set yanked
         * 
         * @param yanked true if it was yanked
         * @return the object
         */
        public Builder yanked(boolean yanked) {
            this.wasYanked = yanked;
            return this;
        }


        /**
         * Build
         * 
         * @return the change-log entry
         */
        public ChangelogEntry build() {
            return new ChangelogEntry(releaseVersion, releaseDate, releaseDescription, releaseInfo, isReleased, wasYanked, sectionList);
        }
    }
}
