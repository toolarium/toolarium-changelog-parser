/*
 * ChangelogReleaseVersion.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.dto;

import java.io.Serializable;
import java.util.Objects;


/**
 * Change-log release version.
 * 
 * @author patrick
 */
public class ChangelogReleaseVersion implements Comparable<ChangelogReleaseVersion>, Serializable {
    private static final long serialVersionUID = -4168625306023528355L;
    private final int majorNumber;
    private final int minorNumber;
    private final int buildNumber;
    private final String buildInfo;

    
    /**
     * Constructor
     * 
     * @param majorNumber the major number
     * @param minorNumber the minor number
     * @param buildNumber the build number
     * @param buildInfo the build information
     */
    public ChangelogReleaseVersion(int majorNumber, int minorNumber, int buildNumber, String buildInfo) {
        
        if (majorNumber >= 0) {
            this.majorNumber = majorNumber;
        } else {
            this.majorNumber = 0;
        }
        
        if (minorNumber >= 0) {
            this.minorNumber = minorNumber;
        } else {
            this.minorNumber = 0;
        }

        if (buildNumber >= 0) {
            this.buildNumber = buildNumber;
        } else {
            this.buildNumber = 0;
        }

        if (buildInfo != null && !buildInfo.isBlank()) {
            this.buildInfo = buildInfo;
        } else {
            this.buildInfo = null;
        }
    }


    /**
     * Gets the major number back
     * 
     * @return the major number
     */
    public int getMajorNumber() {
        return majorNumber;
    }

    
    /**
     * Gets the minor number
     * @return the minor number
     */
    public int getMinorNumber() {
        return minorNumber;
    }
    
    
    /**
     * Gets the build number
     * 
     * @return the build number
     */
    public int getBuildNumber() {
        return buildNumber;
    }
    
    
    /**
     * Gets the additional build info
     * @return the additional build info
     */
    public String getBuildInfo() {
        return buildInfo;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(buildInfo, buildNumber, majorNumber, minorNumber);
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
        ChangelogReleaseVersion other = (ChangelogReleaseVersion) obj;
        return Objects.equals(buildInfo, other.buildInfo) && buildNumber == other.buildNumber && majorNumber == other.majorNumber && minorNumber == other.minorNumber;
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder msg = new StringBuilder();

        msg.append(majorNumber);
        msg.append('.');
        msg.append(minorNumber);
        msg.append('.');
        msg.append(buildNumber);
        
        if (buildInfo != null) {
            msg.append('-');
            msg.append(buildInfo);
        }

        return msg.toString();
    }


    /**
     * Test if the given version is newer than the current
     * 
     * @param changelogReleaseVersion the version to compare
     * @return true if the given version is newer
     */
    public boolean isNewer(ChangelogReleaseVersion changelogReleaseVersion) {
        return (changelogReleaseVersion.compareTo(this) > 0);
    }

    
    /**
     * Test if the given version is olrder than the current
     * 
     * @param changelogReleaseVersion the version to compare
     * @return true if the given version is older
     */
    public boolean isOlder(ChangelogReleaseVersion changelogReleaseVersion) {
        return (compareTo(changelogReleaseVersion) > 0);
    }


    /**
     * Compares two ChangelogReleaseVersion's
     * 
     * @param changelogReleaseVersion the object to compare
     * @return 0&lt; if the object is less; 0 if the objects are equal; &gt;0 if the object is bigger
     */
    @Override
    public int compareTo(ChangelogReleaseVersion changelogReleaseVersion)    {
        if (changelogReleaseVersion == null) {
            return -1;
        }
        
        return compareMajorVersion(changelogReleaseVersion);
    }


    /**
     * Compares the major version number
     * 
     * @param changelogReleaseVersion the version to compare
     * @return 0&lt; if the object is less; 0 if the objects are equal; &gt;0 if the object is bigger
     */
    protected int compareMajorVersion(ChangelogReleaseVersion changelogReleaseVersion) {
        if (changelogReleaseVersion == null) {
            return -1;
        }
        
        int result = compareValues(changelogReleaseVersion.getMajorNumber(), getMajorNumber());
        if (result == 0) {
            result = compareMinorVersion(changelogReleaseVersion);
        } 
        
        return result;
    }

    /**
     * Compares the minor version number
     * 
     * @param changelogReleaseVersion the version to compare
     * @return 0&lt; if the object is less; 0 if the objects are equal; &gt;0 if the object is bigger
     */
    protected int compareMinorVersion(ChangelogReleaseVersion changelogReleaseVersion) {
        if (changelogReleaseVersion == null) {
            return -1;
        }
        
        int result = compareValues(changelogReleaseVersion.getMinorNumber(), getMinorNumber());
        if (result == 0) {
            result = compareBuildVersion(changelogReleaseVersion);
        }

        return result;
    }

    /**
     * Compares the build version number
     * 
     * @param changelogReleaseVersion the version to compare
     * @return 0&lt; if the object is less; 0 if the objects are equal; &gt;0 if the object is bigger
     */
    protected int compareBuildVersion(ChangelogReleaseVersion changelogReleaseVersion) {
        if (changelogReleaseVersion == null) {
            return -1;
        }

        int result = compareValues(changelogReleaseVersion.getBuildNumber(), getBuildNumber());
        if ((changelogReleaseVersion.getBuildNumber() == -1 && getBuildNumber() == -1) || (result == 0)) {
            return compareValues(changelogReleaseVersion.buildInfo, buildInfo);
        }

        return result;
    }

    
    /**
     * Compares two integer values
     * 
     * @param v1 the version to compare
     * @param v2 the version to compare
     * @return 0&lt; if the object is less; 0 if the objects are equal; &gt;0 if the object is bigger
     */
    protected int compareValues(int v1, int v2) {
        if ((v1 >= 0) && (v2 >= 0)) {
            if (v1 == v2) {
                return 0;
            } else if (v2 > v1) {
                return 1;
            } else {
                return -1;
            }
        } else if ((v2 >= 0) && (v1 < 0)) {
            return 1;
        } else if ((v2 < 0) && (v1 >= 0)) {
            return -1;
        } else if ((v2 < 0) && (v1 < 0)) {
            return 0;
        }

        return -1;
    }

    
    /**
     * Compares two integer values
     * 
     * @param v1 the version to compare
     * @param v2 the version to compare
     * @return 0&lt; if the object is less; 0 if the objects are equal; &gt;0 if the object is bigger
     */
    protected int compareValues(String v1, String v2) {
        if (v1 == null && v2 == null) {
            return 0;
        } else if (v1 != null && v2 == null) {
            return -1;
        } else if (v1 == null && v2 != null) {
            return 1;
        } else if (v1 != null && v1.length() == 0 && v2.length() == 0) {
            return 0;
        } else if (v1 != null && v1.length() > 0 && v2.length() == 0) {
            return -1;
        } else if (v1 != null && v1.length() == 0 && v2.length() > 0) {
            return 1;
        }
        
        return v2.compareTo(v1);
    }
}
