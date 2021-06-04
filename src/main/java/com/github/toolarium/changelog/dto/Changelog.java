/*
 * Changelog.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.dto;

import com.github.toolarium.changelog.ChangelogFactory;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


/**
 * The change-log
 * 
 * @author patrick
 */
public class Changelog implements Serializable {
    public static final String UNRELEASED_ENTRY_NAME = "Unreleased";
    private static final long serialVersionUID = 5434389778943L;
    private String projectName = "";
    private String description = "";
    private List<ChangelogEntry> entries;

    
    /**
     * Create a new change-log
     *
     * @param projectName the name of the project
     * @param description the description of the project
     */
    public Changelog(String projectName, String description) {
        this(projectName, description, new ArrayList<>());
    }
    
    /**
     * Create a new change-log
     *
     * @param projectName the name of the project
     * @param description the description of the project
     * @param entries any set of entries in any order; the entries will be sorted when added
     */
    public Changelog(String projectName, String description, List<ChangelogEntry> entries) {
        this.projectName = projectName;
        this.description = description;
        this.entries = entries;
    }

    
    /**
     * Get the project name.
     *
     * @return the name of the project
     */
    public String getProjectName() {
        return projectName;
    }

    
    /**
     * The description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    
    /**
     * The change-log entries.
     *
     * @return the entries
     */
    public List<ChangelogEntry> getEntries() {
        return entries;
    }

    
    /**
     * Search a version entry.
     *
     * @param version the version to search
     * @return the entry or null
     * @throws ParseException In case the input version is not in a proper format
     */
    public ChangelogEntry getEntry(String version)  {
        if (entries == null || entries.isEmpty()) {
            return null;
        }
            
        ChangelogReleaseVersion searchVersion = null;
        if (version != null && !UNRELEASED_ENTRY_NAME.equals(version)) {
            searchVersion = ChangelogFactory.getInstance().createChangelogParser().parseVersion(version); 
        }
        
        for (ChangelogEntry entry : entries) {
            if (!entry.isReleased() && searchVersion == null) {
                return entry;
            }

            if (entry.getReleaseVersion() != null && entry.getReleaseVersion().equals(searchVersion)) {
                return entry;
            }
        }

        return null;
    }
    
    
    /**
     * Add a new entry; if it exist, it will be updated.
     *
     * @param inputVersion the version
     * @param inputReleaseDate the release date
     * @return the changelog entry
     */
    public ChangelogEntry addEntry(String inputVersion, String inputReleaseDate) {
        String version = inputVersion;
        if (version == null || UNRELEASED_ENTRY_NAME.equals(version.trim())) {
            version = UNRELEASED_ENTRY_NAME;
        }

        if (entries == null) {
            entries = new ArrayList<ChangelogEntry>();
        }

        ChangelogEntry changelogEntry = getEntry(version);
        if (changelogEntry == null) {
            ChangelogReleaseVersion releaseVersion = ChangelogFactory.getInstance().createChangelogParser().parseVersion(version);
            changelogEntry = new ChangelogEntry(releaseVersion, null);
            entries.add(0, changelogEntry);
        } 

        if (inputReleaseDate != null) {
            LocalDate releaseDate = LocalDate.parse(inputReleaseDate);
            if (!releaseDate.equals(changelogEntry.getReleaseDate())) {
                changelogEntry.setReleaseDate(releaseDate);
            }
        }
        
        return changelogEntry;
    }


    /**
     * Remove an entry
     *
     * @param version the version
     * @return the removed entry
     */
    public ChangelogEntry removeEntry(String version) {
        if (entries == null || entries.isEmpty()) {
            return null;
        }
            
        ChangelogReleaseVersion searchVersion = null;
        if (version != null && !UNRELEASED_ENTRY_NAME.equals(version)) {
            searchVersion = ChangelogFactory.getInstance().createChangelogParser().parseVersion(version); 
        }

        ChangelogEntry removedChangelogEntry = null;
        for (Iterator<ChangelogEntry> it = entries.iterator(); it.hasNext();) {
            ChangelogEntry entry = it.next();
            if (!entry.isReleased() && searchVersion == null) {
                removedChangelogEntry = entry;
                it.remove();
                break;
            }

            if (entry.getReleaseVersion() != null && entry.getReleaseVersion().equals(searchVersion)) {
                removedChangelogEntry = entry;
                it.remove();
                break;
            }
        }

        return removedChangelogEntry;
    }

    
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(description, entries, projectName);
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
        Changelog other = (Changelog) obj;
        return Objects.equals(description, other.description) && Objects.equals(entries, other.entries) && Objects.equals(projectName, other.projectName);
    }

    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Changelog [projectName=" + projectName + ", description=" + description + ", entries=" + entries + "]";
    }
}
