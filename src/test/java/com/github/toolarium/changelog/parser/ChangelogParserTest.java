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
import com.github.toolarium.changelog.dto.ChangelogReleaseVersion;
import com.github.toolarium.changelog.parser.impl.ChangelogContentParser;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Test the change-log parser
 * 
 * @author patrick
 */
public class ChangelogParserTest extends AbstractChangelogParserTest {
    private static final String TEST_RESOURCE_PATH = "src/test/resources";
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
     *
     */
    @Test public void testValidChangelog() throws IOException {
        Changelog changelog = assertChangelogFile(new ChangelogConfig('-', '-', true, false, false, false, true, true, true, true), Paths.get(TEST_RESOURCE_PATH, "CHANGELOG-valid.md"), null);

        ChangelogEntry unReleaseEntry = changelog.getEntry(Changelog.UNRELEASED_ENTRY_NAME);
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
     *
     */
    @Test public void testValidChangelogWithSpacesBeginningComment() throws IOException {
        ChangelogErrorList changelogErrorList = new ChangelogErrorList();
        changelogErrorList.addReleaseError(new ChangelogReleaseVersion(1, 0, 2, null), "Space before comment list in section type Changed!");
        changelogErrorList.addReleaseError(new ChangelogReleaseVersion(1, 0, 0, null), "Space before comment list in section type Changed!");
        
        Changelog changelog = assertChangelog(new ChangelogConfig('-', '-', true, false, false, false, true, true, true, true),
                                              readContent(Paths.get(TEST_RESOURCE_PATH, "CHANGELOG-valid.md")),
                                              parseFile(Paths.get(TEST_RESOURCE_PATH, "CHANGELOG-valid-with-spaces-beginning-comment.md")),
                                              changelogErrorList);

        ChangelogEntry unReleaseEntry = changelog.getEntry(Changelog.UNRELEASED_ENTRY_NAME);
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
     *
     */
    @Test public void testValidChangelogDifferentFormat() throws IOException {
        Changelog changelog = assertChangelogFile(new ChangelogConfig('/', '*', true, false, false, false, true, false, true, true), Paths.get(TEST_RESOURCE_PATH, "CHANGELOG-different-format-valid.md"), null);

        ChangelogEntry unReleaseEntry = changelog.getEntry(Changelog.UNRELEASED_ENTRY_NAME);
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
     *
     */
    @Test public void testValidChangelogWithBracket() throws IOException {
        Changelog changelog = assertChangelogFile(new ChangelogConfig('-', '-', true, false, true, true, true, false, true, true), Paths.get(TEST_RESOURCE_PATH, "CHANGELOG-valid-with-brackets.md"), null);

        ChangelogEntry unReleaseEntry = changelog.getEntry(Changelog.UNRELEASED_ENTRY_NAME);
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
     * Parse and compare a change-log with Performance section
     *
     * @throws IOException In case of I/O error
     */
    @Test public void testChangelogWithPerformanceSection() throws IOException {
        Changelog changelog = assertChangelogFile(new ChangelogConfig('-', '-', false, false, false, false, false, false, true, true), Paths.get(TEST_RESOURCE_PATH, "CHANGELOG-with-performance.md"), null);

        ChangelogEntry entry = changelog.getEntry(VERSION_1_0_0);
        assertNotNull(entry);
        assertTrue(entry.isReleased());
        assertEquals(2, entry.getSectionList().size());

        assertEquals(ChangelogChangeType.PERFORMANCE, entry.getSectionList().get(0).getChangeType());
        assertNotNull(entry.getSectionList().get(0).getChangeCommentList());
        assertEquals(2, entry.getSectionList().get(0).getChangeCommentList().size());
        assertEquals("Optimized database queries.", entry.getSectionList().get(0).getChangeCommentList().get(0));
        assertEquals("Reduced memory footprint.", entry.getSectionList().get(0).getChangeCommentList().get(1));

        assertEquals(ChangelogChangeType.CHANGED, entry.getSectionList().get(1).getChangeType());
        assertEquals(1, entry.getSectionList().get(1).getChangeCommentList().size());
        assertEquals("Updated dependencies.", entry.getSectionList().get(1).getChangeCommentList().get(0));
    }


    /**
     * Test parseVersion with valid, SNAPSHOT and invalid inputs
     */
    @Test public void testParseVersion() {
        // valid plain version
        ChangelogReleaseVersion v = ChangelogFactory.getInstance().createChangelogParser().parseVersion("1.2.3");
        assertNotNull(v);
        assertEquals(1, v.getMajorNumber());
        assertEquals(2, v.getMinorNumber());
        assertEquals(3, v.getBuildNumber());

        // SNAPSHOT version parses with non-null result and correct numbers
        ChangelogReleaseVersion snap = ChangelogFactory.getInstance().createChangelogParser().parseVersion("1.0.0-SNAPSHOT");
        assertNotNull(snap);
        assertEquals(1, snap.getMajorNumber());
        assertEquals(0, snap.getMinorNumber());
        assertEquals(0, snap.getBuildNumber());

        // invalid version returns null
        assertNull(ChangelogFactory.getInstance().createChangelogParser().parseVersion("not-a-version"));
        assertNull(ChangelogFactory.getInstance().createChangelogParser().parseVersion("abc"));
    }


    /**
     * Test that a release header with an extra info field is parsed into getInfo()
     *
     * @throws IOException In case of I/O error
     */
    @Test public void testParseEntryWithInfo() throws IOException {
        String content = "# my-project\n\n## 1.0.0 - 2024-01-15 - hotfix\n### Added\n- New feature.\n";
        ChangelogParseResult result = ChangelogFactory.getInstance().parse(content);
        assertTrue(result.getChangelogErrorList().isEmpty(), "Expected no errors: " + result.getChangelogErrorList());

        ChangelogEntry entry = result.getChangelog().getEntry("1.0.0");
        assertNotNull(entry);
        assertEquals("hotfix", entry.getInfo());
        assertFalse(entry.wasYanked());
    }


    /**
     * Verify that a YANKED entry is parsed with wasYanked() == true and that the
     * entry with a release link has hasBracketsAroundVersion() == true
     *
     * @throws IOException In case of I/O error
     */
    @Test public void testYankedAndBracketsParsed() throws IOException {
        Path filename = Paths.get(TEST_RESOURCE_PATH, "CHANGELOG-valid.md");
        Changelog changelog = parseFile(filename).getChangelog();

        // 1.0.1 is marked YANKED in the file
        ChangelogEntry yankedEntry = changelog.getEntry("1.0.1");
        assertNotNull(yankedEntry);
        assertTrue(yankedEntry.wasYanked(), "Expected 1.0.1 to be yanked");
        assertFalse(yankedEntry.hasBracketsAroundVersion());

        // 1.1.1 uses [version](url) syntax → brackets around version
        ChangelogEntry linkedEntry = changelog.getEntry("1.1.1");
        assertNotNull(linkedEntry);
        assertFalse(linkedEntry.wasYanked());
        assertTrue(linkedEntry.hasBracketsAroundVersion(), "Expected 1.1.1 to have brackets around version");
        assertNotNull(linkedEntry.getReleaseLink());
    }


    /**
     * Parse and compare a change-log
     *
     * @throws IOException In case of I/O error
     *
     */
    @Test public void testUnsupportedUnreleased() throws IOException {
        Path filename = Paths.get(TEST_RESOURCE_PATH, "CHANGELOG-valid.md");
        ChangelogParseResult changelogParseResult = parseFile(filename);
        assertTrue(changelogParseResult.getChangelogErrorList().isEmpty(), "Expected no errors: " + changelogParseResult.getChangelogErrorList());

        Changelog changelog = changelogParseResult.getChangelog();

        ChangelogEntry unReleaseEntry = changelog.getEntry(Changelog.UNRELEASED_ENTRY_NAME);
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

        assertEquals(readContent(filename), format(new ChangelogConfig('-', '-', true, false, false, false, true, false, true, true), changelog));

    }


    /**
     * Test ChangelogContentParser read methods directly
     *
     * @throws IOException In case of I/O error
     */
    @Test
    public void testContentParserReadMethods() throws IOException {
        ChangelogContentParser parser = new ChangelogContentParser();

        // readChangelogSeparator + readVersion + readHeaderSeparator + readDate + readHeaderEnd
        parser.init("## 1.2.3 - 2024-06-01\n");
        String sep = parser.readChangelogSeparator();
        assertEquals("##", sep);
        String version = parser.readVersion();
        assertEquals("1.2.3", version);
        Character headerSep = parser.readHeaderSeparator();
        assertNotNull(headerSep);
        assertEquals('-', (char) headerSep);
        String date = parser.readDate();
        assertEquals("2024-06-01", date);
        String end = parser.readHeaderEnd();
        assertNotNull(end);

        // readItems
        parser.init("- First item.\n- Second item.\n");
        List<String> items = parser.readItems();
        assertEquals(2, items.size());
        assertEquals("First item.", items.get(0));
        assertEquals("Second item.", items.get(1));

        // readEOL
        parser.init("hello world\nnext line\n");
        String line = parser.readEOL();
        assertEquals("hello world", line);

        // readChangelogText stops at next section marker
        parser.init("Some text.\nMore text.\n## Next\n");
        String text = parser.readChangelogText();
        assertTrue(text.contains("Some text."));
        assertTrue(text.contains("More text."));
        assertFalse(text.contains("## Next"));

        // isEOL
        parser.init("");
        assertTrue(parser.isEOL());
        parser.init("x");
        assertFalse(parser.isEOL());
    }


    /**
     * Test that a date wrapped in parentheses triggers a date format warning
     *
     * @throws IOException In case of I/O error
     */
    @Test
    public void testParseInvalidDateFormat() throws IOException {
        String content = "# my-project\n\n## 1.0.0 - (2024-01-15)\n### Added\n- Feature one.\n";
        ChangelogParseResult result = ChangelogFactory.getInstance().parse(content);
        assertNotNull(result.getChangelogErrorList());
        assertFalse(result.getChangelogErrorList().isEmpty());
        assertNotNull(result.getChangelogErrorList().getGeneralErrors().get(ErrorType.ENTRIES));
        assertTrue(result.getChangelogErrorList().getGeneralErrors().get(ErrorType.ENTRIES).stream()
                .anyMatch(s -> s.contains("Invalid relase date format")));
    }


    /**
     * Test that mixed separator characters in a version header trigger an error
     *
     * @throws IOException In case of I/O error
     */
    @Test
    public void testParseMixedSeparator() throws IOException {
        String content = "# my-project\n\n## 1.0.0 - 2024-01-15 / extra\n### Added\n- Feature.\n";
        ChangelogParseResult result = ChangelogFactory.getInstance().parse(content);
        assertNotNull(result.getChangelogErrorList());
        assertFalse(result.getChangelogErrorList().isEmpty());
        ChangelogReleaseVersion v100 = ChangelogFactory.getInstance().createChangelogParser().parseVersion("1.0.0");
        assertNotNull(result.getChangelogErrorList().getReleaseErrors().get(v100));
        assertTrue(result.getChangelogErrorList().getReleaseErrors().get(v100).stream()
                .anyMatch(s -> s.contains("Found mixed separator character")));
    }


    /**
     * Test that an invalid version string in the entry header produces an error
     *
     * @throws IOException In case of I/O error
     */
    @Test
    public void testParseInvalidVersionInHeader() throws IOException {
        String content = "# my-project\n\n## xyzinvalid - 2024-01-15\n### Added\n- Feature.\n";
        ChangelogParseResult result = ChangelogFactory.getInstance().parse(content);
        assertNotNull(result.getChangelogErrorList());
        assertFalse(result.getChangelogErrorList().isEmpty());
        // invalid version → addReleaseError(null, ...) falls back to HEADER general error
        assertNotNull(result.getChangelogErrorList().getGeneralErrors().get(ErrorType.HEADER));
        assertTrue(result.getChangelogErrorList().getGeneralErrors().get(ErrorType.HEADER).stream()
                .anyMatch(s -> s.contains("Invalid relase version")));
    }
}
