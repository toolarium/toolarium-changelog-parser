/*
 * ChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.changelog.ChangelogFactory;
import com.github.toolarium.changelog.config.ChangelogConfig;
import com.github.toolarium.changelog.parser.ChangelogParseResult;
import com.github.toolarium.changelog.validator.ValidationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Test the changelog
 * 
 * @author patrick
 */
public class ChangelogTest {

    private static final String MY_PROJECT = "my-project";
    private static final String ADDED = "### Added\n";
    private static final String NEW_FEATURE_X = "- New feature x\n";
    private static final String VERSION_100 = "1.0.0";
    private static final String DESCRIPTION = "description.";
    private static final String FEATURE = "Feature.";
    private static final String SNAPSHOT = "SNAPSHOT";


    /**
     * Test empty changelog
     */
    @Test
    public void createChangelog() {
        Changelog changelog = new Changelog(MY_PROJECT, "my \n description \n \n ");
        assertEquals(MY_PROJECT, changelog.getProjectName());
        assertEquals("my \n description \n \n ", changelog.getDescription());
        assertNotNull(changelog.getEntries());
        assertTrue(changelog.getEntries().isEmpty());

        assertNull(changelog.getEntry(null));
        assertTrue(changelog.getEntries().isEmpty());
        
        assertNull(changelog.getEntry(null));
        assertTrue(changelog.getEntries().isEmpty());

        assertNull(changelog.getEntry(Changelog.UNRELEASED_ENTRY_NAME));
        assertTrue(changelog.getEntries().isEmpty());
        
        assertNull(changelog.getEntry("2.0.0"));
        assertTrue(changelog.getEntries().isEmpty());
    }


    /**
     * Test empty changelog
     * 
     * @throws IOException In case of an IO error 
     */
    @Test
    public void addChangelogEntry() throws IOException {
        final String projectName = MY_PROJECT;
        final String description = "my \n description \n \n ";
        Changelog changelog = new Changelog(projectName, description);
        assertEquals(projectName, changelog.getProjectName());
        assertEquals(description, changelog.getDescription());
        assertNotNull(changelog.getEntries());
        assertTrue(changelog.getEntries().isEmpty());

        final String version100 = VERSION_100;
        assertNull(changelog.getEntry(version100));
        assertTrue(changelog.getEntries().isEmpty());
        
        ChangelogEntry entry = changelog.addEntry(version100, null);
        assertNotNull(entry);
        assertEquals(1, changelog.getEntries().size());
        
        assertNull(entry.getDescription());
        assertNull(entry.getInfo());
        assertNotNull(entry.getReleaseDate());
        assertNull(entry.getReleaseLink());
        assertNotNull(entry.getReleaseVersion());
        assertEquals(1, entry.getReleaseVersion().getMajorNumber());
        assertEquals(0, entry.getReleaseVersion().getMinorNumber());
        assertEquals(0, entry.getReleaseVersion().getBuildNumber());
        assertNull(entry.getReleaseVersion().getBuildInfo());
        assertNotNull(entry.getSectionList());
        assertTrue(entry.getSectionList().isEmpty());

        ChangelogEntry entry2 = changelog.addEntry(version100, null);
        assertNotNull(entry2);
        assertEquals(entry, entry2);

        ChangelogEntry entry3 = changelog.addEntry(version100, "2015-05-03");
        assertNotNull(entry3);
        assertEquals(entry, entry3);

        assertEquals("2015-05-03", "" + entry.getReleaseDate());
        ChangelogSection section = entry.addSection(ChangelogChangeType.ADDED);
        assertNotNull(section);
        section.add("New feature x");
        
        final String formatDescription =  "# " + projectName + "\n" + description + "\n\n";
        assertEquals(formatDescription 
                     + "## [ 1.0.0 ] - 2015-05-03\n" 
                     + ADDED 
                     + NEW_FEATURE_X, 
                     ChangelogFactory.getInstance().format(null, changelog));

        ChangelogConfig config = new ChangelogConfig();
        config.setSupportSpaceAroundVersion(false);
        assertEquals(formatDescription 
                + "## [1.0.0] - 2015-05-03\n" 
                + ADDED 
                + NEW_FEATURE_X, 
                ChangelogFactory.getInstance().format(config, changelog));

        changelog.addEntry("1.0.1", null).addSection(ChangelogChangeType.CHANGED).add("changeset");
        
        assertEquals(formatDescription 
                + "## [ 1.0.1 ] - " + LocalDate.now() + "\n" 
                + "### Changed\n" 
                + "- changeset\n" 
                + "\n"
                + "## [ 1.0.0 ] - 2015-05-03\n" 
                + ADDED 
                + NEW_FEATURE_X, 
                ChangelogFactory.getInstance().format(null, changelog));

        changelog.addEntry("1.0.1", "2020-12-04");

        assertEquals(formatDescription 
                + "## [ 1.0.1 ] - 2020-12-04\n" 
                + "### Changed\n" 
                + "- changeset\n" 
                + "\n"
                + "## [ 1.0.0 ] - 2015-05-03\n" 
                + ADDED 
                + NEW_FEATURE_X, 
                ChangelogFactory.getInstance().format(null, changelog));
        
        changelog.removeEntry("1.0.1");        
        assertEquals(formatDescription 
                + "## [ 1.0.0 ] - 2015-05-03\n" 
                + ADDED 
                + NEW_FEATURE_X, 
                ChangelogFactory.getInstance().format(null, changelog));
        
        assertNull(changelog.getEntry(version100).removeSection(ChangelogChangeType.DEPRECATED));
        assertEquals(formatDescription 
                + "## [ 1.0.0 ] - 2015-05-03\n" 
                + ADDED 
                + NEW_FEATURE_X, 
                ChangelogFactory.getInstance().format(null, changelog));

        changelog.addEntry(version100, "2016-04-22").addSection(ChangelogChangeType.DEPRECATED).add("changeset 1");
        changelog.addEntry(version100, null).addSection(ChangelogChangeType.CHANGED).add("changeset 2");
        changelog.getEntry(version100).removeSection(ChangelogChangeType.ADDED);

        assertEquals(formatDescription
                + "## [ 1.0.0 ] - 2016-04-22\n"
                + "### Deprecated\n"
                + "- changeset 1\n"
                + "\n"
                + "### Changed\n"
                + "- changeset 2\n",
                ChangelogFactory.getInstance().format(null, changelog));
    }


    /**
     * Test addEntry with a pre-built ChangelogEntry
     */
    @Test
    public void addPreBuiltEntry() {
        Changelog changelog = new Changelog(MY_PROJECT, DESCRIPTION);
        assertTrue(changelog.getEntries().isEmpty());

        ChangelogReleaseVersion version = new ChangelogReleaseVersion(2, 0, 0, null);
        ChangelogEntry entry = new ChangelogEntry(version, LocalDate.of(2024, 1, 15));
        entry.addSection(ChangelogChangeType.ADDED).add("New feature.");
        changelog.addEntry(entry);

        assertEquals(1, changelog.getEntries().size());
        assertNotNull(changelog.getEntry("2.0.0"));
        assertEquals(version, changelog.getEntry("2.0.0").getReleaseVersion());
    }


    /**
     * Test addSection with a pre-built ChangelogSection
     */
    @Test
    public void addPreBuiltSection() {
        ChangelogEntry entry = new ChangelogEntry(new ChangelogReleaseVersion(1, 0, 0, null), LocalDate.of(2024, 1, 15));
        assertNotNull(entry.getSectionList());
        assertTrue(entry.getSectionList().isEmpty());

        ChangelogSection section = new ChangelogSection(ChangelogChangeType.FIXED);
        section.add("Fixed a bug.");
        entry.addSection(section);

        assertEquals(1, entry.getSectionList().size());
        assertEquals(ChangelogChangeType.FIXED, entry.getSectionList().get(0).getChangeType());
        assertEquals("Fixed a bug.", entry.getSectionList().get(0).getChangeCommentList().get(0));
    }


    /**
     * Test that getEntries returns an unmodifiable list
     */
    @Test
    public void getEntriesReturnsUnmodifiableList() {
        Changelog changelog = new Changelog(MY_PROJECT, DESCRIPTION);
        changelog.addEntry(VERSION_100, null);
        assertThrows(UnsupportedOperationException.class, () -> {
            changelog.getEntries().add(new ChangelogEntry());
        });
    }


    /**
     * Test that getSectionList returns an unmodifiable list
     */
    @Test
    public void getSectionListReturnsUnmodifiableList() {
        ChangelogEntry entry = new ChangelogEntry(new ChangelogReleaseVersion(1, 0, 0, null), LocalDate.of(2024, 1, 15));
        entry.addSection(ChangelogChangeType.ADDED).add(FEATURE);
        assertThrows(UnsupportedOperationException.class, () -> {
            entry.getSectionList().add(new ChangelogSection(ChangelogChangeType.FIXED));
        });
    }


    /**
     * Test that getChangeCommentList returns an unmodifiable list
     */
    @Test
    public void getChangeCommentListReturnsUnmodifiableList() {
        ChangelogSection section = new ChangelogSection(ChangelogChangeType.ADDED);
        section.add("A comment.");
        assertThrows(UnsupportedOperationException.class, () -> {
            section.getChangeCommentList().add("another");
        });
    }


    /**
     * Test that getGeneralErrors returns an unmodifiable map
     */
    @Test
    public void getGeneralErrorsReturnsUnmodifiableMap() {
        ChangelogErrorList errorList = new ChangelogErrorList();
        errorList.addGeneralError(ChangelogErrorList.ErrorType.HEADER, "test error");
        assertThrows(UnsupportedOperationException.class, () -> {
            errorList.getGeneralErrors().put(ChangelogErrorList.ErrorType.CHANGELOG, null);
        });
    }


    /**
     * Test that getReleaseErrors returns an unmodifiable map
     */
    @Test
    public void getReleaseErrorsReturnsUnmodifiableMap() {
        ChangelogErrorList errorList = new ChangelogErrorList();
        errorList.addReleaseError(new ChangelogReleaseVersion(1, 0, 0, null), "test error");
        assertThrows(UnsupportedOperationException.class, () -> {
            errorList.getReleaseErrors().put(new ChangelogReleaseVersion(2, 0, 0, null), null);
        });
    }


    /**
     * Test addEntry with blank version treats it as Unreleased
     */
    @Test
    public void addEntryWithBlankVersion() {
        Changelog changelog = new Changelog(MY_PROJECT, DESCRIPTION);
        ChangelogEntry entry = changelog.addEntry("  ", null);
        assertNotNull(entry);
        assertNull(entry.getReleaseVersion());
        assertEquals(1, changelog.getEntries().size());

        ChangelogEntry unreleased = changelog.getEntry(Changelog.UNRELEASED_ENTRY_NAME);
        assertEquals(entry, unreleased);

        // blank and empty should return the same unreleased entry
        ChangelogEntry entry2 = changelog.addEntry("", null);
        assertEquals(entry, entry2);
        assertEquals(1, changelog.getEntries().size());
    }


    /**
     * Test PERFORMANCE change type
     *
     * @throws IOException In case of an IO error
     */
    @Test
    public void performanceChangeType() throws IOException {
        Changelog changelog = new Changelog("test-project", DESCRIPTION);
        ChangelogEntry entry = changelog.addEntry(VERSION_100, "2024-01-15");
        entry.addSection(ChangelogChangeType.PERFORMANCE).add("Optimized queries.");

        String formatted = ChangelogFactory.getInstance().format(null, changelog);
        assertTrue(formatted.contains("### Performance"));
        assertTrue(formatted.contains("- Optimized queries."));
    }


    /**
     * Test ChangelogReleaseVersion with buildInfo
     */
    @Test
    public void changelogReleaseVersionWithBuildInfo() {
        ChangelogReleaseVersion v = new ChangelogReleaseVersion(1, 2, 3, SNAPSHOT);
        assertEquals(1, v.getMajorNumber());
        assertEquals(2, v.getMinorNumber());
        assertEquals(3, v.getBuildNumber());
        assertEquals(SNAPSHOT, v.getBuildInfo());
        assertEquals("1.2.3-SNAPSHOT", v.toString());

        // blank buildInfo treated as null
        ChangelogReleaseVersion vblank = new ChangelogReleaseVersion(1, 0, 0, "  ");
        assertNull(vblank.getBuildInfo());
        assertEquals(VERSION_100, vblank.toString());

        // null buildInfo
        ChangelogReleaseVersion vnull = new ChangelogReleaseVersion(1, 0, 0, null);
        assertNull(vnull.getBuildInfo());
        assertEquals(VERSION_100, vnull.toString());

        // equality
        assertEquals(new ChangelogReleaseVersion(1, 2, 3, SNAPSHOT), v);
        assertNotEquals(new ChangelogReleaseVersion(1, 2, 3, null), v);
    }


    /**
     * Test ChangelogReleaseVersion comparison methods
     */
    @Test
    public void changelogReleaseVersionComparison() {
        ChangelogReleaseVersion v100 = new ChangelogReleaseVersion(1, 0, 0, null);
        ChangelogReleaseVersion v110 = new ChangelogReleaseVersion(1, 1, 0, null);
        final ChangelogReleaseVersion v200 = new ChangelogReleaseVersion(2, 0, 0, null);
        final ChangelogReleaseVersion v100snap = new ChangelogReleaseVersion(1, 0, 0, SNAPSHOT);

        // compareTo: positive means this > other (this is newer), negative means this < other (this is older)
        assertTrue(v110.compareTo(v100) > 0);   // 1.1.0 > 1.0.0
        assertTrue(v100.compareTo(v110) < 0);   // 1.0.0 < 1.1.0
        assertEquals(0, v100.compareTo(new ChangelogReleaseVersion(1, 0, 0, null)));
        assertTrue(v200.compareTo(v110) > 0);   // 2.0.0 > 1.1.0
        assertTrue(v100.compareTo(null) < 0);

        // isNewer: is the argument newer than current?
        assertTrue(v100.isNewer(v110));    // v110 is newer than v100
        assertFalse(v110.isNewer(v100));   // v100 is not newer than v110

        // isOlder: is the argument older than current?
        assertTrue(v110.isOlder(v100));    // v100 is older than v110
        assertFalse(v100.isOlder(v110));   // v110 is not older than v100

        // buildInfo: version without buildInfo sorts before version with buildInfo
        assertTrue(v100.compareTo(v100snap) != 0);
    }


    /**
     * Test ChangelogReleaseVersion negative numbers are clamped to 0
     */
    @Test
    public void changelogReleaseVersionNegativeNumbers() {
        ChangelogReleaseVersion v = new ChangelogReleaseVersion(-1, -2, -3, null);
        assertEquals(0, v.getMajorNumber());
        assertEquals(0, v.getMinorNumber());
        assertEquals(0, v.getBuildNumber());
        assertEquals("0.0.0", v.toString());
    }


    /**
     * Test getSection on ChangelogEntry
     */
    @Test
    public void changelogEntryGetSection() {
        ChangelogEntry entry = new ChangelogEntry(new ChangelogReleaseVersion(1, 0, 0, null), LocalDate.of(2024, 1, 15));
        assertNull(entry.getSection(ChangelogChangeType.ADDED));
        assertNull(entry.getSection(null));

        entry.addSection(ChangelogChangeType.ADDED).add(FEATURE);
        assertNotNull(entry.getSection(ChangelogChangeType.ADDED));
        assertEquals(ChangelogChangeType.ADDED, entry.getSection(ChangelogChangeType.ADDED).getChangeType());
        assertNull(entry.getSection(ChangelogChangeType.FIXED));
    }


    /**
     * Test setReleaseVersion(null) marks entry as unreleased
     */
    @Test
    public void changelogEntrySetReleaseVersionNull() {
        ChangelogEntry entry = new ChangelogEntry(new ChangelogReleaseVersion(1, 0, 0, null), LocalDate.of(2024, 1, 15));
        assertTrue(entry.isReleased());
        assertNotNull(entry.getReleaseVersion());

        entry.setReleaseVersion(null);
        assertFalse(entry.isReleased());
    }


    /**
     * Test formatter with a YANKED entry
     *
     * @throws IOException In case of an IO error
     */
    @Test
    public void formatterYankedEntry() throws IOException {
        Changelog changelog = new Changelog("my-project", DESCRIPTION);
        ChangelogEntry entry = new ChangelogEntry(
                new ChangelogReleaseVersion(1, 0, 1, null),
                LocalDate.of(2021, 4, 14));
        entry.setWasYanked();
        entry.addSection(ChangelogChangeType.CHANGED).add("Bugfix.");
        changelog.addEntry(entry);

        String formatted = ChangelogFactory.getInstance().format(null, changelog);
        assertTrue(formatted.contains("YANKED"), "Expected YANKED in: " + formatted);
        assertTrue(formatted.contains("1.0.1"));
        assertTrue(entry.wasYanked());
    }


    /**
     * Test formatter with entry info field
     *
     * @throws IOException In case of an IO error
     */
    @Test
    public void formatterEntryWithInfo() throws IOException {
        Changelog changelog = new Changelog("my-project", DESCRIPTION);
        ChangelogEntry entry = new ChangelogEntry(
                new ChangelogReleaseVersion(1, 0, 0, null),
                LocalDate.of(2024, 1, 15));
        entry.setInfo("hotfix");
        entry.addSection(ChangelogChangeType.ADDED).add("New feature.");
        changelog.addEntry(entry);

        assertEquals("hotfix", entry.getInfo());
        String formatted = ChangelogFactory.getInstance().format(null, changelog);
        assertTrue(formatted.contains("hotfix"), "Expected info 'hotfix' in: " + formatted);
    }


    /**
     * Test formatter with release link
     *
     * @throws IOException In case of an IO error
     * @throws MalformedURLException In case of a malformed URL
     */
    @Test
    public void formatterWithReleaseLink() throws IOException, MalformedURLException {
        Changelog changelog = new Changelog("my-project", DESCRIPTION);
        ChangelogEntry entry = new ChangelogEntry(
                new ChangelogReleaseVersion(1, 0, 0, null),
                LocalDate.of(2024, 1, 15));
        URL link = URI.create("https://github.com/example/releases/v1.0.0").toURL();
        entry.setReleaseLink(link);
        entry.addSection(ChangelogChangeType.ADDED).add("New feature.");
        changelog.addEntry(entry);

        assertEquals(link, entry.getReleaseLink());

        ChangelogConfig config = new ChangelogConfig();
        config.setSupportReleaseLink(true);
        String formatted = ChangelogFactory.getInstance().format(config, changelog);
        assertTrue(formatted.contains("https://github.com/example/releases/v1.0.0"),
                "Expected release link in: " + formatted);
    }


    /**
     * Test formatter renders all change types correctly
     *
     * @throws IOException In case of an IO error
     */
    @Test
    public void formatterAllChangeTypes() throws IOException {
        Changelog changelog = new Changelog("my-project", DESCRIPTION);
        ChangelogEntry entry = changelog.addEntry("2.0.0", "2024-01-15");
        entry.addSection(ChangelogChangeType.ADDED).add("Added feature.");
        entry.addSection(ChangelogChangeType.CHANGED).add("Changed feature.");
        entry.addSection(ChangelogChangeType.DEPRECATED).add("Deprecated feature.");
        entry.addSection(ChangelogChangeType.REMOVED).add("Removed feature.");
        entry.addSection(ChangelogChangeType.FIXED).add("Fixed feature.");
        entry.addSection(ChangelogChangeType.SECURITY).add("Security feature.");
        entry.addSection(ChangelogChangeType.PERFORMANCE).add("Performance feature.");

        String formatted = ChangelogFactory.getInstance().format(null, changelog);
        assertTrue(formatted.contains("### Added"));
        assertTrue(formatted.contains("### Changed"));
        assertTrue(formatted.contains("### Deprecated"));
        assertTrue(formatted.contains("### Removed"));
        assertTrue(formatted.contains("### Fixed"));
        assertTrue(formatted.contains("### Security"));
        assertTrue(formatted.contains("### Performance"));
    }


    /**
     * Test ChangelogConfig link and ID pattern matching
     */
    @Test
    public void changelogConfigPatternMatching() {
        ChangelogConfig config = new ChangelogConfig();

        // link detection
        assertTrue(config.isLinkInCommentEnabled());
        assertNotNull(config.getLinkCommentCheckExpression());
        assertNotNull(config.hasLinkInComment("See https://example.com for details."));
        assertNull(config.hasLinkInComment("No link here."));
        assertNull(config.hasLinkInComment(null));

        // ID detection
        assertTrue(config.isIdInCommentEnabled());
        assertNotNull(config.getIdCommentCheckExpression());
        assertNotNull(config.hasIdInComment("Fixed AB-1234 issue."));
        assertNull(config.hasIdInComment("No ID here."));
        assertNull(config.hasIdInComment(null));

        // disable link pattern
        config.setLinkCommentCheckExpression(null);
        assertFalse(config.isLinkInCommentEnabled());
        assertNull(config.getLinkCommentCheckExpression());
        assertNull(config.hasLinkInComment("https://example.com"));

        // disable ID pattern
        config.setIdCommentCheckExpression(null);
        assertFalse(config.isIdInCommentEnabled());
        assertNull(config.getIdCommentCheckExpression());
        assertNull(config.hasIdInComment("AB-1234"));
    }


    /**
     * Test ChangelogReleaseVersion hashCode consistency
     */
    @Test
    public void changelogReleaseVersionHashCode() {
        ChangelogReleaseVersion v1 = new ChangelogReleaseVersion(1, 2, 3, SNAPSHOT);
        ChangelogReleaseVersion v2 = new ChangelogReleaseVersion(1, 2, 3, SNAPSHOT);
        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());

        ChangelogReleaseVersion v3 = new ChangelogReleaseVersion(1, 2, 3, null);
        assertNotEquals(v1, v3);
        // objects that differ should (likely) have different hash codes
        assertNotEquals(v1.hashCode(), v3.hashCode());
    }


    /**
     * Test ChangelogEntry hasBracketsAroundVersion
     */
    @Test
    public void changelogEntryBracketsAroundVersion() {
        ChangelogEntry entry = new ChangelogEntry(new ChangelogReleaseVersion(1, 0, 0, null), LocalDate.of(2024, 1, 15));
        assertFalse(entry.hasBracketsAroundVersion());

        entry.setHasBracketsAroundVersion(true);
        assertTrue(entry.hasBracketsAroundVersion());

        entry.setHasBracketsAroundVersion(false);
        assertFalse(entry.hasBracketsAroundVersion());
    }


    /**
     * Test ChangelogEntry setReleaseDate(null) defaults to today
     */
    @Test
    public void changelogEntrySetReleaseDateNull() {
        ChangelogEntry entry = new ChangelogEntry(new ChangelogReleaseVersion(1, 0, 0, null), LocalDate.of(2020, 1, 1));
        assertEquals(LocalDate.of(2020, 1, 1), entry.getReleaseDate());

        entry.setReleaseDate(null);
        assertEquals(LocalDate.now(), entry.getReleaseDate());
    }


    /**
     * Test ChangelogErrorList.prepareString with no errors, general errors, release errors and custom indent
     */
    @Test
    public void changelogErrorListPrepareString() {
        ChangelogErrorList empty = new ChangelogErrorList();
        assertTrue(empty.isEmpty());
        assertEquals("", empty.prepareString());
        assertEquals("", empty.prepareString("  "));

        // general error
        ChangelogErrorList withGeneral = new ChangelogErrorList();
        withGeneral.addGeneralError(ChangelogErrorList.ErrorType.HEADER, "Bad header.");
        String generalStr = withGeneral.prepareString();
        assertTrue(generalStr.contains("HEADER"));
        assertTrue(generalStr.contains("Bad header."));
        assertTrue(generalStr.startsWith("- "));

        // custom indent
        String indentedStr = withGeneral.prepareString("  ");
        assertTrue(indentedStr.startsWith("  - "));
        assertTrue(indentedStr.contains("HEADER"));

        // release error
        ChangelogErrorList withRelease = new ChangelogErrorList();
        withRelease.addReleaseError(new ChangelogReleaseVersion(1, 0, 0, null), "Version error.");
        String releaseStr = withRelease.prepareString();
        assertTrue(releaseStr.contains(VERSION_100));
        assertTrue(releaseStr.contains("Version error."));

        // toString delegates to prepareString
        assertNotNull(withRelease.toString());
        assertTrue(withRelease.toString().contains(VERSION_100));
    }


    /**
     * Test ChangelogErrorList.add merges two error lists
     */
    @Test
    public void changelogErrorListMerge() {
        ChangelogErrorList list1 = new ChangelogErrorList();
        list1.addGeneralError(ChangelogErrorList.ErrorType.HEADER, "Error A.");

        ChangelogErrorList list2 = new ChangelogErrorList();
        list2.addGeneralError(ChangelogErrorList.ErrorType.HEADER, "Error B.");
        list2.addReleaseError(new ChangelogReleaseVersion(1, 0, 0, null), "Release error.");

        list1.add(list2);

        assertEquals(2, list1.getGeneralErrors().get(ChangelogErrorList.ErrorType.HEADER).size());
        assertEquals(1, list1.getReleaseErrors().get(new ChangelogReleaseVersion(1, 0, 0, null)).size());
        assertEquals(3, list1.size());
    }


    /**
     * Test addReleaseError with null version falls back to a general HEADER error
     */
    @Test
    public void changelogErrorListNullVersionFallsBackToHeader() {
        ChangelogErrorList errorList = new ChangelogErrorList();
        errorList.addReleaseError(null, "Null version error.");

        assertNotNull(errorList.getGeneralErrors().get(ChangelogErrorList.ErrorType.HEADER));
        assertEquals("Null version error.",
                errorList.getGeneralErrors().get(ChangelogErrorList.ErrorType.HEADER).get(0));
        assertTrue(errorList.getReleaseErrors().isEmpty());
    }


    /**
     * Test ChangelogSection equals, hashCode and toString
     */
    @Test
    public void changelogSectionEqualsHashCodeToString() {
        ChangelogSection s1 = new ChangelogSection(ChangelogChangeType.ADDED);
        s1.add("Feature one.");
        ChangelogSection s2 = new ChangelogSection(ChangelogChangeType.ADDED);
        s2.add("Feature one.");

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertEquals(s1, s1);
        assertNotEquals(s1, null);
        assertNotEquals(s1, "other");

        // different type
        ChangelogSection s3 = new ChangelogSection(ChangelogChangeType.FIXED);
        s3.add("Feature one.");
        assertNotEquals(s1, s3);

        // different comment
        ChangelogSection s4 = new ChangelogSection(ChangelogChangeType.ADDED);
        s4.add("Other text.");
        assertNotEquals(s1, s4);

        // toString contains key fields
        String str = s1.toString();
        assertNotNull(str);
        assertTrue(str.contains("ADDED"));
        assertTrue(str.contains("Feature one."));

        // add() ignores null and blank
        ChangelogSection s5 = new ChangelogSection(ChangelogChangeType.CHANGED);
        s5.add(null);
        s5.add("   ");
        assertTrue(s5.getChangeCommentList().isEmpty());
    }


    /**
     * Test ChangelogEntry equals, hashCode and toString
     */
    @Test
    public void changelogEntryEqualsHashCodeToString() {
        ChangelogReleaseVersion version = new ChangelogReleaseVersion(1, 0, 0, null);
        LocalDate date = LocalDate.of(2024, 1, 15);

        ChangelogEntry e1 = new ChangelogEntry(version, date);
        e1.addSection(ChangelogChangeType.ADDED).add(FEATURE);

        ChangelogEntry e2 = new ChangelogEntry(version, date);
        e2.addSection(ChangelogChangeType.ADDED).add(FEATURE);

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertEquals(e1, e1);
        assertNotEquals(e1, null);
        assertNotEquals(e1, "other");

        // different version
        ChangelogEntry e3 = new ChangelogEntry(new ChangelogReleaseVersion(2, 0, 0, null), date);
        e3.addSection(ChangelogChangeType.ADDED).add(FEATURE);
        assertNotEquals(e1, e3);

        // different wasYanked
        ChangelogEntry e4 = new ChangelogEntry(version, date);
        e4.addSection(ChangelogChangeType.ADDED).add(FEATURE);
        e4.setWasYanked();
        assertNotEquals(e1, e4);

        // toString contains key fields
        String str = e1.toString();
        assertNotNull(str);
        assertTrue(str.contains(VERSION_100));
        assertTrue(str.contains("2024-01-15"));
    }


    /**
     * Test ChangelogEntry.compareTo — unreleased sorts first, released entries sort newest first
     */
    @Test
    public void changelogEntryCompareTo() {
        ChangelogEntry unreleased = new ChangelogEntry();  // no version → not released
        ChangelogEntry v100 = new ChangelogEntry(new ChangelogReleaseVersion(1, 0, 0, null), LocalDate.of(2024, 1, 1));
        final ChangelogEntry v110 = new ChangelogEntry(new ChangelogReleaseVersion(1, 1, 0, null), LocalDate.of(2024, 2, 1));

        // unreleased always before released
        assertTrue(unreleased.compareTo(v100) < 0);
        assertTrue(v100.compareTo(unreleased) > 0);

        // two unreleased are equal
        ChangelogEntry unreleased2 = new ChangelogEntry();
        assertEquals(0, unreleased.compareTo(unreleased2));

        // released: newer version sorts first (lower compareTo result)
        assertTrue(v110.compareTo(v100) < 0);
        assertTrue(v100.compareTo(v110) > 0);
        assertEquals(0, v100.compareTo(new ChangelogEntry(new ChangelogReleaseVersion(1, 0, 0, null), LocalDate.of(2024, 1, 1))));
    }


    /**
     * Test ChangelogConfig accessors not covered elsewhere
     */
    @Test
    public void changelogConfigAccessors() {
        ChangelogConfig config = new ChangelogConfig();

        config.setSectionCharacter('/');
        assertEquals('/', config.getSectionCharacter());

        config.setHeaderSeparator('/');
        assertEquals('/', config.getHeaderSeparator());

        config.setItemSeparator('*');
        assertEquals('*', config.getItemSeparator());

        config.setSupportUnreleased(false);
        assertFalse(config.isSupportUnreleased());

        config.setSupportBracketsAroundVersion(false);
        assertFalse(config.isSupportBracketsAroundVersion());

        config.setSupportReleaseLink(false);
        assertFalse(config.isSupportReleaseLink());

        config.setSupportReleaseInfo(false);
        assertFalse(config.isSupportReleaseInfo());

        config.setSupportLinkInDescription(false);
        assertFalse(config.isSupportLinkInDescription());

        config.setSupportIdListOnEndOfTheComment(false);
        assertFalse(config.isSupportIdListOnEndOfTheComment());
    }


    /**
     * Test ChangelogParseResult equals, hashCode and toString
     */
    @Test
    public void changelogParseResultEqualsHashCodeToString() {
        ChangelogParseResult r1 = new ChangelogParseResult();
        ChangelogParseResult r2 = new ChangelogParseResult();

        // both empty → equal
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertEquals(r1, r1);
        assertNotEquals(r1, null);
        assertNotEquals(r1, "other");

        // set changelog on one
        r1.setChangelog(new Changelog("project", "desc."));
        assertNotEquals(r1, r2);

        r2.setChangelog(new Changelog("project", "desc."));
        assertEquals(r1, r2);

        // toString contains key content
        String str = r1.toString();
        assertNotNull(str);
        assertTrue(str.contains("project"));
    }


    /**
     * Test ChangelogConfig toString and basic identity equality
     */
    @Test
    public void changelogConfigEquality() {
        ChangelogConfig c1 = new ChangelogConfig();
        // reflexive: same instance equals itself
        assertEquals(c1, c1);
        assertNotEquals(c1, null);
        assertNotEquals(c1, "other");

        // toString contains key fields
        assertNotNull(c1.toString());
        assertTrue(c1.toString().contains("sectionCharacter"));
        assertTrue(c1.toString().contains("supportUnreleased"));
    }


    /**
     * Test the 7-argument ChangelogEntry constructor
     */
    @Test
    public void changelogEntryFullConstructor() {
        ChangelogReleaseVersion version = new ChangelogReleaseVersion(2, 3, 1, SNAPSHOT);
        LocalDate date = LocalDate.of(2025, 6, 1);
        List<ChangelogSection> sections = new ArrayList<>();
        ChangelogSection section = new ChangelogSection(ChangelogChangeType.ADDED);
        section.add("Full feature.");
        sections.add(section);

        ChangelogEntry entry = new ChangelogEntry(version, date, "My description.", "hotfix", true, false, sections);

        assertEquals(version, entry.getReleaseVersion());
        assertEquals(date, entry.getReleaseDate());
        assertEquals("My description.", entry.getDescription());
        assertEquals("hotfix", entry.getInfo());
        assertTrue(entry.isReleased());
        assertFalse(entry.wasYanked());
        assertEquals(1, entry.getSectionList().size());
        assertEquals(ChangelogChangeType.ADDED, entry.getSectionList().get(0).getChangeType());

        // yanked variant
        ChangelogEntry yanked = new ChangelogEntry(version, date, "Desc.", null, true, true, sections);
        assertTrue(yanked.wasYanked());
        assertNull(yanked.getInfo());
    }


    /**
     * Test ValidationException.toString() includes message and error list
     */
    @Test
    public void validationExceptionToString() {
        ChangelogErrorList errorList = new ChangelogErrorList();
        errorList.addGeneralError(ChangelogErrorList.ErrorType.HEADER, "Bad header found.");
        errorList.addReleaseError(new ChangelogReleaseVersion(1, 0, 0, null), "Invalid release date [2024-13-01]!");

        ValidationException ex = new ValidationException("Validation failed", errorList);

        String str = ex.toString();
        assertNotNull(str);
        assertTrue(str.contains("Validation failed"));
        assertTrue(str.contains("HEADER"));
        assertTrue(str.contains("Bad header found."));
        assertTrue(str.contains(VERSION_100));
        assertTrue(str.contains("Invalid release date"));
    }
}
