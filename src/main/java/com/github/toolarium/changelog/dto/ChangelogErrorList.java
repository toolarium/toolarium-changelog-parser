/*
 * ChangelogErrorList.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The change-log error list. It represents general errors but also version related errors.
 * 
 * @author patrick
 */
public class ChangelogErrorList implements Serializable {
    private static final long serialVersionUID = -5548185055661633916L;
    private Map<ErrorType, List<String>> generalErrors;
    private Map<ChangelogReleaseVersion, List<String>> releaseErrors;

    
    /**
     * Constructor for ChangelogErrorList
     */
    public ChangelogErrorList() {
        generalErrors = new LinkedHashMap<>();
        releaseErrors = new LinkedHashMap<>();
    }

    
    /**
     * Add a change-log error list.
     *
     * @param changelogErrorList the change-log error list
     */
    public void add(ChangelogErrorList changelogErrorList) {
        
        if (changelogErrorList.hasGeneralErrors()) {
            for (Map.Entry<ErrorType, List<String>> e : changelogErrorList.generalErrors.entrySet()) {
                for (String error : e.getValue()) {
                    addGeneralError(e.getKey(), error);
                }
            }
        }
        
        if (changelogErrorList.hasReleaseErrors()) {
            for (Map.Entry<ChangelogReleaseVersion, List<String>> e : changelogErrorList.releaseErrors.entrySet()) {
                for (String error : e.getValue()) {
                    addReleaseError(e.getKey(), error);
                }
            }
        }
    }


    
    /**
     * Adds a general error. It contains an ErrorTyp and a description.
     *
     * @param errorType the error type
     * @param error the error
     */
    public void addGeneralError(ErrorType errorType, String error) {
        if (error == null || error.isBlank()) {
            return;
        }
            
        List<String> errorList = generalErrors.get(errorType);
        if (errorList == null) {
            errorList = new ArrayList<>();
            generalErrors.put(errorType, errorList);
        }
        
        errorList.add(error);
    }

    
    /**
     * Adds an error which belongs to a version.
     *
     * @param releaseVersion the release version
     * @param error the error
     */
    public void addReleaseError(ChangelogReleaseVersion releaseVersion, String error) {
        if (error == null || error.isBlank()) {
            return;
        }
         
        if (releaseVersion != null) {
            List<String> errorList = releaseErrors.get(releaseVersion);
            if (errorList == null) {
                errorList = new ArrayList<>();
                releaseErrors.put(releaseVersion, errorList);
            }
            
            errorList.add(error);
        } else {
            addGeneralError(ErrorType.HEADER, error);
        }
    }

    
    /**
     * Check if there a are no general errors
     *
     * @return true if there are no general errors
     */
    public boolean hasGeneralErrors() {
        return generalErrors != null && !generalErrors.isEmpty();
    }

    
    /**
     * Count the total general errors.
     *
     * @return the total general errors
     */
    public long countGeneralErrors() {
        long result = 0;
        if (hasGeneralErrors()) {
            for (ErrorType errorType : ErrorType.values()) {
                List<String> errorList = getGeneralErrors().get(errorType);
                if (errorList != null && !errorList.isEmpty()) {
                    result += errorList.size();
                }
            }
        }
        
        return result;
    }

    
    /**
     * Get the general errors
     *
     * @return the general errors
     */
    public Map<ErrorType, List<String>> getGeneralErrors() {
        return generalErrors;
    }

    
    /**
     * Check if there a are no release errors
     *
     * @return true if there are no release errors
     */
    public boolean hasReleaseErrors() {
        return releaseErrors != null && !releaseErrors.isEmpty();
    }

    
    /**
     * Count the total release errors.
     *
     * @return the total release errors
     */
    public long countReleaseErrors() {
        long result = 0;
        if (hasReleaseErrors()) {
            for (Map.Entry<ChangelogReleaseVersion, List<String>> e : getReleaseErrors().entrySet()) {
                if (e.getValue() != null && !e.getValue().isEmpty()) {
                    result += e.getValue().size();
                }
            }
        }
        
        return result;
    }

    
    /**
     * Get the release errors
     *
     * @return the release errors
     */
    public Map<ChangelogReleaseVersion, List<String>> getReleaseErrors() {
        return releaseErrors;
    }

    
    /**
     * Check if there a are no errors
     *
     * @return true if there are no errors
     */
    public boolean isEmpty() {
        return !hasGeneralErrors() && !hasReleaseErrors();
    }

    
    /**
     * Get the total number of errors
     *
     * @return the total number of errors
     */
    public long size() {
        return countGeneralErrors() + countReleaseErrors();
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(generalErrors, releaseErrors);
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
        
        ChangelogErrorList other = (ChangelogErrorList) obj;
        return Objects.equals(generalErrors, other.generalErrors) && Objects.equals(releaseErrors, other.releaseErrors);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ChangelogErrorList: " + prepareString();
    }


    /**
     * Create a string representation of the change-log error list.
     * 
     * @return the prepare content. 
     */
    public String prepareString() {
        final StringBuilder result = new StringBuilder();
        
        if (hasGeneralErrors()) {
            for (ErrorType errorType : ErrorType.values()) {
                List<String> errorList = getGeneralErrors().get(errorType);
                if (errorList != null && !errorList.isEmpty()) {
                    getGeneralErrors().get(errorType).stream().forEach((comment) -> result.append("- " + errorType + ": " + comment + "\n"));
                }
            }
        }
    
        if (hasReleaseErrors()) {
            for (Map.Entry<ChangelogReleaseVersion, List<String>> e : getReleaseErrors().entrySet()) {
                e.getValue().stream().forEach((comment) -> result.append("- " + e.getKey() + ": " + comment + "\n"));
            }
        }
    
        return result.toString();
    }

    
    /**
     * The error types
     * 
     * @author patrick
     */
    public enum ErrorType {
        CHANGELOG,
        HEADER,
        ENTRIES,
        REFERENCE,
        UNRELEASED;
    }
}
