/*
 * ChangelogSection.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Defines the change-log section which is part of an entry.
 * It contains the ChangelogChangeType and a list with comments.
 * 
 * @author patrick
 */
public class ChangelogSection implements Serializable {
    private static final long serialVersionUID = 34234723894723L;
    private ChangelogChangeType changeType;
    private List<String> changeCommentList;

    
    /**
     * Constructor for ChangelogSection
     * 
     * @param changeType the change type
     */
    public ChangelogSection(ChangelogChangeType changeType) {
        this(changeType, new ArrayList<>());
    }

    
    /**
     * Constructor for ChangelogSection
     *
     * @param changeType the change type
     * @param changeCommentList the comment list
     */
    public ChangelogSection(ChangelogChangeType changeType, List<String> changeCommentList) {
        this.changeType = changeType;
        this.changeCommentList = changeCommentList;
    }

    
    /**
     * Get the change type
     * 
     * @return the change type
     */
    public ChangelogChangeType getChangeType() {
        return changeType;
    }


    /**
     * Add a comment
     * 
     * @param comment the comment
     */
    public void add(String comment) {
        if (comment != null && !comment.isBlank()) {
            changeCommentList.add(comment);
        }
    }
    
    
    /**
     * Get the change comment list
     * 
     * @return the change comment list
     */
    public List<String> getChangeCommentList() {
        return changeCommentList;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(changeCommentList, changeType);
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
        ChangelogSection other = (ChangelogSection) obj;
        return Objects.equals(changeCommentList, other.changeCommentList) && changeType == other.changeType;
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ChangelogSection [changeType=" + changeType + ", changeCommentList=" + changeCommentList + "]";
    }
}
