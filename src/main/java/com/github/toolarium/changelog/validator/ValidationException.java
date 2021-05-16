/*
 * ValidationException.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.validator;

import com.github.toolarium.changelog.dto.ChangelogErrorList;

/**
 * Defines the validation exception
 * 
 * @author patrick
 */
public class ValidationException extends Exception {
    private static final long serialVersionUID = -291893037239967317L;
    private ChangelogErrorList validationErrorList;

    
    /**
     * Constructor for ValidationException
     * 
     * @param message the message
     * @param validationErrorList the validation error list
     */
    public ValidationException(String message, ChangelogErrorList validationErrorList) {
        super(message);
        this.validationErrorList = validationErrorList;
    }
    
    
    /**
     * Get the validation error list
     * 
     * @return the validation error list.
     */
    public ChangelogErrorList getValidationErrorList() {
        return validationErrorList;
    }

    
    /**
     * @see java.lang.Throwable#toString()
     */
    @Override
    public String toString() {
        String result = getMessage();
        if (validationErrorList != null) {
            result += "\n" + validationErrorList.prepareString();
        }
        
        return result; 
    }
}
