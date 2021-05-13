/*
 * ValidationException.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.exception;

import java.util.List;

/**
 * Defines the validation exception
 * 
 * @author patrick
 */
public class ValidationException extends Exception {
    private static final long serialVersionUID = -291893037239967317L;
    private List<String> validationErrorList;

    
    /**
     * Constructor for ValidationException
     * 
     * @param message the message
     * @param validationErrorList the validation error list
     */
    public ValidationException(String message, List<String> validationErrorList) {
        super(message);
        this.validationErrorList = validationErrorList;
    }
    
    
    /**
     * Get the validation error list
     * 
     * @return the validation error list.
     */
    public List<String> getValidationErrorList() {
        return validationErrorList;
    }

    
    /**
     * @see java.lang.Throwable#toString()
     */
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder(super.toString() + "\n");
        validationErrorList.stream().forEach((comment) -> result.append(comment + "\n"));
        return result.toString();
    }
}
