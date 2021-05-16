/*
 * IChangelogFormatter.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.formatter;

import com.github.toolarium.changelog.dto.Changelog;

/**
 * Defines the change-log formatter.
 * 
 * @author patrick
 */
public interface IChangelogFormatter {
    
    /**
     * Format the change-log.
     * 
     * @param changelog the change-log
     * @return the formated change-log 
     */
    String format(Changelog changelog);
}
