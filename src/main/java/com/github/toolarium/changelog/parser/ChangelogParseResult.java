/*
 * ChangelogParseResult.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.parser;

import com.github.toolarium.changelog.dto.Changelog;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * The changelog parse result.
 * 
 * @author patrick
 */
public class ChangelogParseResult implements Serializable {
    private static final long serialVersionUID = 1589437584385437895L;
    private Changelog changelog;
    private List<String> errorMessageList;
    
    
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
     * Get the error message list
     * 
     * @return the error message list
     */
    public List<String> getErrorMessageList() {
        return errorMessageList;
    }

    
    /**
     * Set the error message list
     * 
     * @param errorMessageList the error message list
     */
    public void setErrorMessageList(List<String> errorMessageList) {
        this.errorMessageList = errorMessageList;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(changelog, errorMessageList);
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
        return Objects.equals(changelog, other.changelog) && Objects.equals(errorMessageList, other.errorMessageList);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ChangelogParseResult [changelog=" + changelog + ", errorMessageList=" + errorMessageList + "]";
    }
}
