/*
 * ChangelogValidatorTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.toolarium.changelog.ChangelogFactory;
import com.github.toolarium.changelog.config.ChangelogConfig;
import com.github.toolarium.changelog.dto.Changelog;
import com.github.toolarium.changelog.dto.ChangelogChangeType;
import com.github.toolarium.changelog.dto.ChangelogEntry;
import com.github.toolarium.changelog.dto.ChangelogErrorList.ErrorType;
import com.github.toolarium.changelog.dto.ChangelogReleaseVersion;
import com.github.toolarium.changelog.dto.ChangelogSection;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;


/**
 * Change-log validator test
 * 
 * @author patrick
 */
public class ChangelogValidatorTest {
    private static final String TEST = "test";
    private static final String MY_PROJECT = "my-project";
    private static final String EXPECTED_VALIDATION_EXCEPTION = "Expected ValidationException";


    /**
     * Validate the valid change-log
     * 
     * @throws ValidationException in case of a validation exception
     * @throws IOException in case of an I/O exception
     */
    @Test public void testValidateChangelog() throws ValidationException, IOException {
        ChangelogConfig changelogConfig = new ChangelogConfig();
        changelogConfig.setSupportReleaseLink(true);
    
        ChangelogFactory.getInstance().validate(changelogConfig, Paths.get("src", TEST, "resources", "CHANGELOG-valid.md"), "<project-name>", "", "1.1.1");
    }


    /**
     * Validate the valid change-log 
     * 
     * @throws ValidationException in case of a validation exception
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void testInvalidChangelog() throws ValidationException, IOException {
        try {
            ChangelogFactory.getInstance().validate(new ChangelogConfig(), Paths.get("src", TEST, "resources", "CHANGELOG-invalid.md"), "<project-name>", "Comment", "1.1.1");
        } catch (ValidationException e) {
            assertEquals(18, e.getValidationErrorList().size());
            
            assertEquals(2, e.getValidationErrorList().getGeneralErrors().size());
            assertEquals(3, e.getValidationErrorList().countGeneralErrors());
            assertNull(e.getValidationErrorList().getGeneralErrors().get(ErrorType.CHANGELOG));
            assertNotNull(e.getValidationErrorList().getGeneralErrors().get(ErrorType.HEADER));
            assertEquals(2, e.getValidationErrorList().getGeneralErrors().get(ErrorType.HEADER).size());
            assertEquals("The name don't correspond to [<project-name>], current [<project-name]!", e.getValidationErrorList().getGeneralErrors().get(ErrorType.HEADER).get(0));
            assertEquals("The don't correspond to [Comment]!", e.getValidationErrorList().getGeneralErrors().get(ErrorType.HEADER).get(1));
            assertNull(e.getValidationErrorList().getGeneralErrors().get(ErrorType.ENTRIES));
            assertNull(e.getValidationErrorList().getGeneralErrors().get(ErrorType.REFERENCE));
            assertNotNull(e.getValidationErrorList().getGeneralErrors().get(ErrorType.UNRELEASED));
            assertEquals(1, e.getValidationErrorList().getGeneralErrors().get(ErrorType.UNRELEASED).size());
            assertEquals(1, e.getValidationErrorList().getGeneralErrors().get(ErrorType.UNRELEASED).size());
            assertEquals("Description text don't end with a punction mark!", e.getValidationErrorList().getGeneralErrors().get(ErrorType.UNRELEASED).get(0));

            assertEquals(5, e.getValidationErrorList().getReleaseErrors().size());
            assertEquals(15, e.getValidationErrorList().countReleaseErrors());
            
            
            ChangelogReleaseVersion releaseVersion = ChangelogFactory.getInstance().createChangelogParser().parseVersion("1.2.0");
            int counter = 0;
            assertEquals("Invalid relase date [2021-04-32]!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
            assertEquals("Invalid changelog change type: [Addeded]!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
            assertEquals("Invalid changelog change type: [Removet]!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
            assertEquals("Newer version in than [1.1.1] in changelog found!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
            assertEquals("Section has a link comment which is not allowed: [http://url.com/dd]!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
            assertEquals("Section has an id in comment which is not allowed: [AB-234]!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
            assertEquals("Invalid sentence in section type Section: [Changed]!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
            assertEquals("Empty comment list in section type Fixed!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
            
            releaseVersion = ChangelogFactory.getInstance().createChangelogParser().parseVersion("1.1.1");
            counter = 0;
            assertEquals("Could not find version 1.1.1!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
            
            releaseVersion = ChangelogFactory.getInstance().createChangelogParser().parseVersion("1.1.0");
            counter = 0;
            assertEquals("The version [1.1.1] should be referenced as first entry.", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
            assertEquals("Invalid sort order!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
            assertEquals("Description text don't end with a punction mark!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
            assertEquals("Added section text don't end with a punction mark!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
            
            releaseVersion = ChangelogFactory.getInstance().createChangelogParser().parseVersion("1.0.2");
            counter = 0;
            assertEquals("Changed section has a link comment which is not allowed: [http://my-private-url.com/see%20~files]!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));

            releaseVersion = ChangelogFactory.getInstance().createChangelogParser().parseVersion("1.0.1");
            counter = 0;
            assertEquals("Changed section text don't end with a punction mark!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
        }
    }


    /**
     * Validate the valid change-log 
     * 
     * @throws ValidationException in case of a validation exception
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void testInvalidChangelogWithoutDuplicatedMessages() throws ValidationException, IOException {
        try {
            String description = "\n"
                    + "All notable changes to this project will be documented in this file.\n"
                    + "\n"
                    + "The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),\n"
                    + "and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).";
            
            ChangelogFactory.getInstance().validate(new ChangelogConfig(), Paths.get("src", TEST, "resources", "CHANGELOG-invalid2.md"), MY_PROJECT, description, "0.0.2");
        } catch (ValidationException e) {
            assertEquals(2, e.getValidationErrorList().size());
            
            assertEquals(0, e.getValidationErrorList().getGeneralErrors().size());
            assertEquals(0, e.getValidationErrorList().countGeneralErrors());
            
            ChangelogReleaseVersion releaseVersion = ChangelogFactory.getInstance().createChangelogParser().parseVersion("0.0.2");
            int counter = 0;
            assertEquals("Empty comment list in section type Changed!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
            
            counter = 0;
            releaseVersion = ChangelogFactory.getInstance().createChangelogParser().parseVersion("0.0.1");
            assertEquals("Invalid empty section!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
            
            
        }
    }


    /**
     * Validate with an invalid reference version to verify the error message contains the input version
     *
     * @throws ValidationException in case of a validation exception
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void testInvalidReferenceVersion() throws ValidationException, IOException {
        try {
            ChangelogFactory.getInstance().validate(new ChangelogConfig(), Paths.get("src", TEST, "resources", "CHANGELOG-valid.md"), null, null, "invalid-version");
        } catch (ValidationException e) {
            assertNotNull(e.getValidationErrorList().getGeneralErrors().get(ErrorType.REFERENCE));
            assertEquals(1, e.getValidationErrorList().getGeneralErrors().get(ErrorType.REFERENCE).size());
            assertEquals("Invalid reference version [invalid-version]!", e.getValidationErrorList().getGeneralErrors().get(ErrorType.REFERENCE).get(0));
        }
    }


    /**
     * Validate an in-memory changelog without file I/O
     *
     * @throws ValidationException in case of a validation exception
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void testValidateInMemoryChangelog() throws ValidationException, IOException {
        Changelog changelog = new Changelog(MY_PROJECT, "");
        changelog.addEntry("1.0.0", "2024-01-15").addSection(ChangelogChangeType.ADDED).add("New feature.");
        ChangelogFactory.getInstance().validate(new ChangelogConfig(), changelog);
    }


    /**
     * Validate that an unreleased section triggers an error when not supported
     *
     * @throws ValidationException in case of a validation exception
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void testUnsupportedUnreleasedSection() throws IOException {
        try {
            Changelog changelog = ChangelogFactory.getInstance().parse(
                    "# my-project\n\n## Unreleased\n- Work in progress.\n\n## 1.0.0 - 2024-01-15\n### Added\n- Feature.\n")
                    .getChangelog();

            ChangelogConfig config = new ChangelogConfig();
            config.setSupportUnreleased(false);
            ChangelogFactory.getInstance().validate(config, changelog);
            fail(EXPECTED_VALIDATION_EXCEPTION);
        } catch (ValidationException e) {
            assertNotNull(e.getValidationErrorList().getGeneralErrors().get(ErrorType.UNRELEASED));
            assertEquals("The unreleased section is not supported!",
                    e.getValidationErrorList().getGeneralErrors().get(ErrorType.UNRELEASED).get(0));
        }
    }


    /**
     * Validate that release info triggers an error when not supported
     *
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void testReleaseInfoNotSupported() throws IOException {
        Changelog changelog = new Changelog(TEST, "");
        ChangelogEntry entry = new ChangelogEntry(
                new ChangelogReleaseVersion(1, 0, 0, null),
                LocalDate.of(2024, 1, 15));
        entry.setInfo("hotfix");
        entry.addSection(ChangelogChangeType.ADDED).add("Feature.");
        changelog.addEntry(entry);

        ChangelogConfig config = new ChangelogConfig();
        config.setSupportReleaseInfo(false);

        try {
            ChangelogFactory.getInstance().validate(config, changelog);
            fail(EXPECTED_VALIDATION_EXCEPTION);
        } catch (ValidationException e) {
            ChangelogReleaseVersion v = new ChangelogReleaseVersion(1, 0, 0, null);
            assertNotNull(e.getValidationErrorList().getReleaseErrors().get(v));
            assertTrue(e.getValidationErrorList().getReleaseErrors().get(v).get(0)
                    .contains("Additional release information is not supported"));
        }
    }


    /**
     * Validate that null changelog produces a validation error
     */
    @Test
    public void testNullChangelogValidation() {
        try {
            ChangelogFactory.getInstance().validate(new ChangelogConfig(), (Changelog) null);
            fail(EXPECTED_VALIDATION_EXCEPTION);
        } catch (ValidationException e) {
            assertNotNull(e.getValidationErrorList().getGeneralErrors().get(ErrorType.CHANGELOG));
            assertEquals("Invalid changelog!",
                    e.getValidationErrorList().getGeneralErrors().get(ErrorType.CHANGELOG).get(0));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }


    /**
     * Validate that duplicate section types produce an error
     *
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void testDuplicateSectionType() throws IOException {
        final Changelog changelog = new Changelog(TEST, "");
        ChangelogEntry entry = new ChangelogEntry(
                new ChangelogReleaseVersion(1, 0, 0, null),
                LocalDate.of(2024, 1, 15));
        ChangelogSection s1 = new ChangelogSection(ChangelogChangeType.ADDED);
        s1.add("Feature one.");
        entry.addSection(s1);
        ChangelogSection s2 = new ChangelogSection(ChangelogChangeType.ADDED);
        s2.add("Feature two.");
        entry.addSection(s2);
        changelog.addEntry(entry);

        try {
            ChangelogFactory.getInstance().validate(new ChangelogConfig(), changelog);
            fail(EXPECTED_VALIDATION_EXCEPTION);
        } catch (ValidationException e) {
            ChangelogReleaseVersion v = new ChangelogReleaseVersion(1, 0, 0, null);
            assertNotNull(e.getValidationErrorList().getReleaseErrors().get(v));
            assertTrue(e.getValidationErrorList().getReleaseErrors().get(v).stream()
                    .anyMatch(s -> s.contains("Duplicate section type Added")));
        }
    }


    /**
     * Validate that a link in the changelog header description triggers an error
     * when supportLinkInDescription is disabled
     *
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void testHeaderDescriptionWithForbiddenLink() throws IOException {
        Changelog changelog = new Changelog(MY_PROJECT,
                "See https://example.com for details.");
        changelog.addEntry("1.0.0", "2024-01-15").addSection(ChangelogChangeType.ADDED).add("Feature.");

        ChangelogConfig config = new ChangelogConfig();
        config.setSupportLinkInDescription(false);

        try {
            ChangelogFactory.getInstance().validate(config, changelog);
            fail(EXPECTED_VALIDATION_EXCEPTION);
        } catch (ValidationException e) {
            assertNotNull(e.getValidationErrorList().getGeneralErrors().get(ErrorType.HEADER));
            assertTrue(e.getValidationErrorList().getGeneralErrors().get(ErrorType.HEADER).stream()
                    .anyMatch(s -> s.contains("link comment which is not allowed")));
        }
    }


    /**
     * Validate that a changelog with no entries produces an ENTRIES error
     */
    @Test
    public void testEmptyEntries() {
        Changelog changelog = new Changelog(MY_PROJECT, "");
        try {
            ChangelogFactory.getInstance().validate(new ChangelogConfig(), changelog);
            fail(EXPECTED_VALIDATION_EXCEPTION);
        } catch (ValidationException e) {
            assertNotNull(e.getValidationErrorList().getGeneralErrors().get(ErrorType.ENTRIES));
            assertEquals("Missing changelog entries!",
                    e.getValidationErrorList().getGeneralErrors().get(ErrorType.ENTRIES).get(0));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }


    /**
     * Validate that duplicate version entries produce a release error
     */
    @Test
    public void testDuplicateVersion() {
        Changelog changelog = new Changelog(MY_PROJECT, "");
        ChangelogEntry entry1 = new ChangelogEntry(
                new ChangelogReleaseVersion(1, 0, 0, null),
                LocalDate.of(2024, 1, 15));
        entry1.addSection(ChangelogChangeType.ADDED).add("Feature one.");
        ChangelogEntry entry2 = new ChangelogEntry(
                new ChangelogReleaseVersion(1, 0, 0, null),
                LocalDate.of(2024, 1, 15));
        entry2.addSection(ChangelogChangeType.FIXED).add("Bug fix.");
        changelog.addEntry(entry1);
        changelog.addEntry(entry2);

        try {
            ChangelogFactory.getInstance().validate(new ChangelogConfig(), changelog, null, null, "1.0.0");
            fail(EXPECTED_VALIDATION_EXCEPTION);
        } catch (ValidationException e) {
            ChangelogReleaseVersion v = new ChangelogReleaseVersion(1, 0, 0, null);
            assertNotNull(e.getValidationErrorList().getReleaseErrors().get(v));
            assertTrue(e.getValidationErrorList().getReleaseErrors().get(v).stream()
                    .anyMatch(s -> s.contains("Found 2 times the same version 1.0.0")));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }


    /**
     * Validate that an entry description with a forbidden link produces a release error
     */
    @Test
    public void testEntryDescriptionWithForbiddenLink() {
        Changelog changelog = new Changelog(MY_PROJECT, "");
        ChangelogEntry entry = new ChangelogEntry(
                new ChangelogReleaseVersion(1, 0, 0, null),
                LocalDate.of(2024, 1, 15));
        entry.setDescription("See https://example.com for details.");
        entry.addSection(ChangelogChangeType.ADDED).add("Feature.");
        changelog.addEntry(entry);

        ChangelogConfig config = new ChangelogConfig();
        config.setSupportLinkInDescription(false);

        try {
            ChangelogFactory.getInstance().validate(config, changelog);
            fail(EXPECTED_VALIDATION_EXCEPTION);
        } catch (ValidationException e) {
            ChangelogReleaseVersion v = new ChangelogReleaseVersion(1, 0, 0, null);
            assertNotNull(e.getValidationErrorList().getReleaseErrors().get(v));
            assertTrue(e.getValidationErrorList().getReleaseErrors().get(v).stream()
                    .anyMatch(s -> s.contains("Description has a link comment which is not allowed")));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }


    /**
     * Validate that an entry description containing an ID pattern produces a release error
     */
    @Test
    public void testEntryDescriptionWithForbiddenId() {
        Changelog changelog = new Changelog(MY_PROJECT, "");
        ChangelogEntry entry = new ChangelogEntry(
                new ChangelogReleaseVersion(1, 0, 0, null),
                LocalDate.of(2024, 1, 15));
        entry.setDescription("Fixed AB-1234 issues.");
        entry.addSection(ChangelogChangeType.ADDED).add("Feature.");
        changelog.addEntry(entry);

        try {
            ChangelogFactory.getInstance().validate(new ChangelogConfig(), changelog);
            fail(EXPECTED_VALIDATION_EXCEPTION);
        } catch (ValidationException e) {
            ChangelogReleaseVersion v = new ChangelogReleaseVersion(1, 0, 0, null);
            assertNotNull(e.getValidationErrorList().getReleaseErrors().get(v));
            assertTrue(e.getValidationErrorList().getReleaseErrors().get(v).stream()
                    .anyMatch(s -> s.contains("Description has an id in comment which is not allowed")));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }


    /**
     * Validate the valid change-log
     *
     * @throws ValidationException in case of a validation exception
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void testChangelogWithSupportedEmptySection() throws ValidationException, IOException {
        try {
            String description = "\n"
                    + "All notable changes to this project will be documented in this file.\n"
                    + "\n"
                    + "The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),\n"
                    + "and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).";
            
            ChangelogConfig changelogConfig = new ChangelogConfig();
            changelogConfig.setSupportEmptySection(true);
            ChangelogFactory.getInstance().validate(changelogConfig, Paths.get("src", TEST, "resources", "CHANGELOG-invalid2.md"), MY_PROJECT, description, "0.0.2");
        } catch (ValidationException e) {
            assertEquals(1, e.getValidationErrorList().size());
            
            assertEquals(0, e.getValidationErrorList().getGeneralErrors().size());
            assertEquals(0, e.getValidationErrorList().countGeneralErrors());
            
            ChangelogReleaseVersion releaseVersion = ChangelogFactory.getInstance().createChangelogParser().parseVersion("0.0.2");
            int counter = 0;
            assertEquals("Empty comment list in section type Changed!", e.getValidationErrorList().getReleaseErrors().get(releaseVersion).get(counter++));
        }
    }
}
