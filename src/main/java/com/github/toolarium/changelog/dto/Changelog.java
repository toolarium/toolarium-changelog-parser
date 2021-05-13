/*
 * Changelog.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jptools.parser.ParseException;
import jptools.util.version.Version;

/**
 * The change-log
 * 
 * @author patrick
 */
public class Changelog implements Serializable {
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
    public ChangelogEntry getEntry(String version) throws ParseException {
        if (entries != null) {
            Version searchVersion = null;
            if (version != null && !"Unreleased".equals(version)) {
                searchVersion = new Version(version);
            }
            
            for (ChangelogEntry entry : entries) {
                if (!entry.isReleased() && searchVersion == null) {
                    return entry;
                }

                if (entry.getReleaseVersion() != null && entry.getReleaseVersion().equals(searchVersion)) {
                    return entry;
                }
            }
        }

        return null;
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
