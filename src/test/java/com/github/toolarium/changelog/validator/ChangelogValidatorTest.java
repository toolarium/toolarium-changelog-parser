/*
 * ChangelogValidatorTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.toolarium.changelog.ChangelogFactory;
import com.github.toolarium.changelog.config.ChangelogConfig;
import com.github.toolarium.changelog.dto.ChangelogErrorList.ErrorType;
import com.github.toolarium.changelog.dto.ChangelogReleaseVersion;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;


/**
 * Change-log validator test
 * 
 * @author patrick
 */
public class ChangelogValidatorTest {
    /**
     * Validate the valid change-log 
     * 
     * @throws ValidationException in case of a validation exception
     * @throws IOException in case of an I/O exception
     */
    @Test public void testValidateChangelog() throws ValidationException, IOException {
        ChangelogConfig changelogConfig = new ChangelogConfig();
        changelogConfig.setSupportReleaseLink(true);
    
        ChangelogFactory.getInstance().validate(changelogConfig, Paths.get("src", "test", "resources", "CHANGELOG-valid.md"), "<project-name>", "", "1.1.1");
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
            ChangelogFactory.getInstance().validate(new ChangelogConfig(), Paths.get("src", "test", "resources", "CHANGELOG-invalid.md"), "<project-name>", "Comment", "1.1.1");
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
            
            ChangelogFactory.getInstance().validate(new ChangelogConfig(), Paths.get("src", "test", "resources", "CHANGELOG-invalid2.md"), "my-project", description, "0.0.2");
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
            ChangelogFactory.getInstance().validate(changelogConfig, Paths.get("src", "test", "resources", "CHANGELOG-invalid2.md"), "my-project", description, "0.0.2");
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
