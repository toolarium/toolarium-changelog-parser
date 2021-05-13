/*
 * ChangelogChangeType.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.dto;


/**
 * Defines the change-log change types. 
 * 
 * @author patrick
 */
public enum ChangelogChangeType {
    /** ADDED */
    ADDED("Added"),

    /** CHANGED */
    CHANGED("Changed"),

    /** DEPRECATED */
    DEPRECATED("Deprecated"),

    /** REMOVED */
    REMOVED("Removed"),

    /** FIXED */
    FIXED("Fixed"),

    /** SECURITY */
    SECURITY("Security");
    
    private String typeName;

    
    /**
     * Constructor for ChangelogChangeType
     * 
     * @param typeName the type name
     */
    ChangelogChangeType(String typeName) {
        this.typeName = typeName;
    }
    
    /**
     * Get the type name
     * 
     * @return the type name
     */
    public String getTypeName() {
        return typeName;
    }
}
