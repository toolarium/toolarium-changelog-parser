/*
 * ChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.changelog.ChangelogFactory;
import com.github.toolarium.changelog.config.ChangelogConfig;
import java.io.IOException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;


/**
 * Test the changelog
 * 
 * @author patrick
 */
public class ChangelogTest {

    private static final String ADDED = "### Added\n";
    private static final String NEW_FEATURE_X = "- New feature x\n";


    /**
     * Test empty changelog
     */
    @Test
    public void createChangelog() {
        Changelog changelog = new Changelog("my-project", "my \n description \n \n ");
        assertEquals("my-project", changelog.getProjectName());
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
        final String projectName = "my-project";
        final String description = "my \n description \n \n ";
        Changelog changelog = new Changelog(projectName, description);
        assertEquals(projectName, changelog.getProjectName());
        assertEquals(description, changelog.getDescription());
        assertNotNull(changelog.getEntries());
        assertTrue(changelog.getEntries().isEmpty());

        final String version100 = "1.0.0";
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
}
