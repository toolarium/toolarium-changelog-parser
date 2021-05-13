/*
 * MyLibrary.java
 *
 * Copyright by toolarium, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.github.toolarium.changelog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 
/**
 * MyLibrary.
 *
 * <p>! This is just a sample please remove it. !</p>
 */
public class MyLibrary {
    private static final Logger LOG = LoggerFactory.getLogger(MyLibrary.class);

    /**
     * Library method.
     *
     * @return returns the result
     */
    public boolean someLibraryMethod() {
        LOG.debug("Debug log message.");
        LOG.info("Info log message.");
        LOG.warn("Warn log message.");
        LOG.error("Error log message.");
        return true;
    }
}
