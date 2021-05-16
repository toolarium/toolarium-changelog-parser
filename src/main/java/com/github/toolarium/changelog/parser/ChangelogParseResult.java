/*
 * ChangelogParseResult.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.parser;

import com.github.toolarium.changelog.dto.Changelog;
import com.github.toolarium.changelog.dto.ChangelogErrorList;
import java.io.Serializable;
import java.util.Objects;

/**
 * The change-log parse result.
 * 
 * @author patrick
 */
public class ChangelogParseResult implements Serializable {
    private static final long serialVersionUID = 1589437584385437895L;
    private Changelog changelog;
    private ChangelogErrorList changelogErrorList;

    
    /**
     * Constructor for ChangelogParseResult
     */
    public ChangelogParseResult() {
        changelog = null;
        changelogErrorList = new ChangelogErrorList();
    }
    
    /**
     * Get the change-log
     * 
     * @return the change-log
     */
    public Changelog getChangelog() {
        return changelog;
    }


    /**
     * Set the change-log
     * 
     * @param changelog the change-log
     */
    public void setChangelog(Changelog changelog) {
        this.changelog = changelog;
    }


    /**
     * Get the error list
     * 
     * @return the error list
     */
    public ChangelogErrorList getChangelogErrorList() {
        return changelogErrorList;
    }

    
    /**
     * Set the error message list
     * 
     * @param changelogErrorList the error list
     */
    public void setChangelogErrorList(ChangelogErrorList changelogErrorList) {
        this.changelogErrorList = changelogErrorList;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(changelog, changelogErrorList);
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
        ChangelogParseResult other = (ChangelogParseResult) obj;
        return Objects.equals(changelog, other.changelog) && Objects.equals(changelogErrorList, other.changelogErrorList);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ChangelogParseResult [changelog=" + changelog + ", changelogErrorList=" + changelogErrorList + "]";
    }
}
