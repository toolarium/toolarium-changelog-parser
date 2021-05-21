/*
 * ChangeComment.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.validator.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * The change comment
 *  
 * @author patrick
 */
public class ChangeComment implements Serializable {
    private static final long serialVersionUID = -3207681305539631299L;
    private String comment;
    private List<String> idList;
    
    
    /**
     * Constructor for ChangeComment
     * 
     * @param comment the comment
     * @param idList the extracted id list
     */
    public ChangeComment(String comment, List<String> idList) {
        this.comment = comment;
        this.idList = idList;
    }

    
    /**
     * Gets the comment
     *
     * @return Returns the comment.
     */
    public String getComment() {
        return comment;
    }

    
    /**
     * Sets the comment
     *
     * @param comment The comment to set.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    
    /**
     * Gets the idList
     *
     * @return Returns the idList.
     */
    public List<String> getIdList() {
        return idList;
    }

    
    /**
     * Sets the idList
     *
     * @param idList The idList to set.
     */
    public void setIdList(List<String> idList) {
        this.idList = idList;
    }

    
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(comment, idList);
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
        ChangeComment other = (ChangeComment) obj;
        return Objects.equals(comment, other.comment) && Objects.equals(idList, other.idList);
    }

    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ChangeComment [comment=" + comment + ", idList=" + idList + "]";
    }
}
