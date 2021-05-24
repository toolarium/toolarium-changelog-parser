/*
 * ChangelogValidatorTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.changelog.ChangelogFactory;
import com.github.toolarium.changelog.config.ChangelogConfig;
import com.github.toolarium.changelog.dto.Changelog;
import com.github.toolarium.changelog.dto.ChangelogChangeType;
import com.github.toolarium.changelog.dto.ChangelogEntry;
import com.github.toolarium.changelog.dto.ChangelogErrorList;
import com.github.toolarium.changelog.dto.ChangelogErrorList.ErrorType;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import jptools.parser.ParseException;
import org.junit.jupiter.api.Test;


/**
 * Test the change-log parser
 * 
 * @author patrick
 */
public class ChangelogParserTest extends AbstractChangelogParserTest {
    private static final String VERSION_1_0_0 = "1.0.0";


    /**
     * Test empty content
     * 
     * @throws IOException In case of an I/O exception
     */
    @Test void testEmptyChangelog() throws IOException {
        assertThrows(IOException.class, () -> {
            ChangelogFactory.getInstance().parse((Path) null);
        });

        assertNull(ChangelogFactory.getInstance().parse("").getChangelog());
        assertNotNull(ChangelogFactory.getInstance().parse("").getChangelogErrorList());
        assertTrue(ChangelogFactory.getInstance().parse("").getChangelogErrorList().isEmpty());
    }

    
    /**
     * Test empty content
     * 
     * @throws IOException In case of an I/O exception
     */
    @Test public void testInvalidChangelog() throws IOException {
        assertEquals("", ChangelogFactory.getInstance().parse("Test").getChangelog().getProjectName());
        ChangelogErrorList changelogErrorList = ChangelogFactory.getInstance().parse("Test").getChangelogErrorList();
        assertNotNull(changelogErrorList);
        
        assertEquals(1, changelogErrorList.countGeneralErrors());
        assertEquals(0, changelogErrorList.countReleaseErrors());
        assertNotNull(changelogErrorList.getGeneralErrors().get(ErrorType.CHANGELOG));
        assertEquals("Invalid empty changelog name!", changelogErrorList.getGeneralErrors().get(ErrorType.CHANGELOG).get(0));
    }

    
    /**
     * Test empty content
     * 
     * @throws IOException In case of an I/O exception
     */
    @Test public void testNameOnlyChangelog() throws IOException {
        assertEquals("Test", ChangelogFactory.getInstance().parse("#Test").getChangelog().getProjectName());
        assertEquals("Test", ChangelogFactory.getInstance().parse("#    Test").getChangelog().getProjectName());
    }

    
    /**
     * Parse and compare a change-log
     * 
     * @throws IOException In case of I/O error
     * @throws ParseException In case of a parse error
     */
    @Test public void testValidChangelog() throws IOException, ParseException {
        Changelog changelog = assertChangelogFile(new ChangelogConfig('-', '-', true, false, true, true, true, true), Paths.get("src", "test", "resources", "CHANGELOG-valid.md"), null);

        ChangelogEntry unReleaseEntry = changelog.getEntry("Unreleased");
        ChangelogEntry entry = changelog.getEntry(null);
        assertEquals(unReleaseEntry, entry);
        assertNull(entry.getReleaseVersion());
        assertFalse(entry.isReleased());
        assertEquals(entry.getDescription(), "- This is a test.");
        assertNotNull(entry.getSectionList());
        assertEquals(entry.getSectionList().size(), 0);

        entry = changelog.getEntry(VERSION_1_0_0);
        assertNotNull(entry);
        assertTrue(entry.isReleased());
        assertEquals(entry.getReleaseVersion().toString(), VERSION_1_0_0);
        assertEquals(entry.getReleaseDate().toString(), "2021-04-08");
        assertEquals(entry.getDescription(), "");
        assertNotNull(entry.getSectionList());
        assertEquals(entry.getSectionList().size(), 2);

        assertEquals(entry.getSectionList().get(0).getChangeType(), ChangelogChangeType.CHANGED);
        assertNotNull(entry.getSectionList().get(0).getChangeCommentList());
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().size(), 3);
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().get(0), "New visual identity.");
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().get(1), "Version navigation updated.");
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().get(2), "Links top navigation.");

        assertEquals(entry.getSectionList().get(1).getChangeType(), ChangelogChangeType.FIXED);
        assertNotNull(entry.getSectionList().get(1).getChangeCommentList());
        assertEquals(entry.getSectionList().get(1).getChangeCommentList().size(), 1);
        assertEquals(entry.getSectionList().get(1).getChangeCommentList().get(0), "Fix typos in service specifications.");
    }


    /**
     * Parse and compare a change-log
     * 
     * @throws IOException In case of I/O error
     * @throws ParseException In case of a parse error
     */
    @Test public void testValidChangelogDifferentFormat() throws IOException, ParseException {
        Changelog changelog = assertChangelogFile(new ChangelogConfig('/', '*', true, false, true, false, true, true), Paths.get("src", "test", "resources", "CHANGELOG-different-format-valid.md"), null);

        ChangelogEntry unReleaseEntry = changelog.getEntry("Unreleased");
        ChangelogEntry entry = changelog.getEntry(null);
        assertEquals(unReleaseEntry, entry);
        assertNull(entry.getReleaseVersion());
        assertFalse(entry.isReleased());
        assertEquals(entry.getDescription(), "* This is a test.");
        assertNotNull(entry.getSectionList());
        assertEquals(entry.getSectionList().size(), 0);

        entry = changelog.getEntry(VERSION_1_0_0);
        assertTrue(entry.isReleased());
        assertEquals(entry.getReleaseVersion().toString(), VERSION_1_0_0);
        assertEquals(entry.getReleaseDate().toString(), "2021-04-08");
        assertEquals(entry.getDescription(), "");
        assertNotNull(entry.getSectionList());
        assertEquals(entry.getSectionList().size(), 2);

        assertEquals(entry.getSectionList().get(0).getChangeType(), ChangelogChangeType.CHANGED);
        assertNotNull(entry.getSectionList().get(0).getChangeCommentList());
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().size(), 3);
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().get(0), "New visual identity.");
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().get(1), "Version navigation.");
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().get(2), "Links top navigation.");

        assertEquals(entry.getSectionList().get(1).getChangeType(), ChangelogChangeType.FIXED);
        assertNotNull(entry.getSectionList().get(1).getChangeCommentList());
        assertEquals(entry.getSectionList().get(1).getChangeCommentList().size(), 1);
        assertEquals(entry.getSectionList().get(1).getChangeCommentList().get(0), "Fix typos in service specifications.");
    }


    /**
     * Parse and compare a change-log
     * 
     * @throws IOException In case of I/O error
     * @throws ParseException In case of a parse error
     */
    @Test public void testValidChangelogWithBracket() throws IOException, ParseException {
        Changelog changelog = assertChangelogFile(new ChangelogConfig('-', '-', true, true, true, false, true, true), Paths.get("src", "test", "resources", "CHANGELOG-valid-with-brackets.md"), null);

        ChangelogEntry unReleaseEntry = changelog.getEntry("Unreleased");
        ChangelogEntry entry = changelog.getEntry(null);
        assertEquals(unReleaseEntry, entry);
        assertNull(entry.getReleaseVersion());
        assertFalse(entry.isReleased());
        assertEquals(entry.getDescription(), "- The test description.");
        assertNotNull(entry.getSectionList());
        assertEquals(entry.getSectionList().size(), 0);

        entry = changelog.getEntry(VERSION_1_0_0);
        assertTrue(entry.isReleased());
        assertEquals(entry.getReleaseVersion().toString(), VERSION_1_0_0);
        assertEquals(entry.getReleaseDate().toString(), "2021-04-08");
        assertEquals(entry.getDescription(), "");
        assertNotNull(entry.getSectionList());
        assertEquals(entry.getSectionList().size(), 2);

        assertEquals(entry.getSectionList().get(0).getChangeType(), ChangelogChangeType.CHANGED);
        assertNotNull(entry.getSectionList().get(0).getChangeCommentList());
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().size(), 3);
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().get(0), "New visual identity.");
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().get(1), "Version navigation.");
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().get(2), "Links top navigation.");

        assertEquals(entry.getSectionList().get(1).getChangeType(), ChangelogChangeType.FIXED);
        assertNotNull(entry.getSectionList().get(1).getChangeCommentList());
        assertEquals(entry.getSectionList().get(1).getChangeCommentList().size(), 1);
        assertEquals(entry.getSectionList().get(1).getChangeCommentList().get(0), "Fix typos in service specifications.");
    }


    /**
     * Parse and compare a change-log
     * 
     * @throws IOException In case of I/O error
     * @throws ParseException In case of a parse error
     */
    @Test public void testUnsupportedUnreleased() throws IOException, ParseException {
        Path filename = Paths.get("src", "test", "resources", "CHANGELOG-valid.md");
        ChangelogParseResult changelogParseResult = parseFile(filename);
        assertTrue(changelogParseResult.getChangelogErrorList().isEmpty(), "Expected no errors: " + changelogParseResult.getChangelogErrorList());

        Changelog changelog = changelogParseResult.getChangelog();

        ChangelogEntry unReleaseEntry = changelog.getEntry("Unreleased");
        ChangelogEntry entry = changelog.getEntry(null);
        assertEquals(unReleaseEntry, entry);
        assertNull(entry.getReleaseVersion());
        assertFalse(entry.isReleased());
        assertEquals(entry.getDescription(), "- This is a test.");
        assertNotNull(entry.getSectionList());
        assertEquals(entry.getSectionList().size(), 0);

        entry = changelog.getEntry(VERSION_1_0_0);
        assertEquals(entry.getReleaseVersion().toString(), VERSION_1_0_0);
        assertEquals(entry.getReleaseDate().toString(), "2021-04-08");
        assertEquals(entry.getDescription(), "");
        assertNotNull(entry.getSectionList());
        assertEquals(entry.getSectionList().size(), 2);

        assertEquals(entry.getSectionList().get(0).getChangeType(), ChangelogChangeType.CHANGED);
        assertNotNull(entry.getSectionList().get(0).getChangeCommentList());
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().size(), 3);
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().get(0), "New visual identity.");
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().get(1), "Version navigation updated.");
        assertEquals(entry.getSectionList().get(0).getChangeCommentList().get(2), "Links top navigation.");

        assertEquals(entry.getSectionList().get(1).getChangeType(), ChangelogChangeType.FIXED);
        assertNotNull(entry.getSectionList().get(1).getChangeCommentList());
        assertEquals(entry.getSectionList().get(1).getChangeCommentList().size(), 1);
        assertEquals(entry.getSectionList().get(1).getChangeCommentList().get(0), "Fix typos in service specifications.");

        assertEquals(readContent(filename), format(new ChangelogConfig('-', '-', true, false, true, false, true, true), changelog));
    }
}
