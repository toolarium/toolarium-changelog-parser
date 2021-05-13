/*
 * ChangelogValidatorTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.toolarium.changelog.ChangelogFactory;
import com.github.toolarium.changelog.config.ChangelogConfig;
import com.github.toolarium.changelog.exception.ValidationException;
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
        ChangelogFactory.getInstance().validate(new ChangelogConfig(), Paths.get("src", "test", "resources", "CHANGELOG-valid.md"), "<project-name>", "", "1.1.1");
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
            //ystem.err.println("==>" + e.getValidationErrorList());
            assertEquals(10, e.getValidationErrorList().size());
            assertEquals("- Header: The name don't correspond to [<project-name>], current [<project-name]!", e.getValidationErrorList().get(0));
            assertEquals("- Header: The don't correspond to [Comment]!", e.getValidationErrorList().get(1));
            assertEquals("- 1.2.0: Newer version in than 1.1.1 in changelog found!", e.getValidationErrorList().get(2));
            assertEquals("- 1.2.0: Invalid relase date [2021-04-32]", e.getValidationErrorList().get(3));
            assertEquals("- 1.2.0: Invalid changelog change type: Removet", e.getValidationErrorList().get(4));
            assertEquals("- 1.2.0: Invalid changelog change type: Addeded", e.getValidationErrorList().get(5));
            assertEquals("- 1.2.0 / FIXED: Empty comment list!", e.getValidationErrorList().get(6));
            assertEquals("- 1.1.1: Could not find version 1.1.1!", e.getValidationErrorList().get(7));
            assertEquals("- 1.1.0: The version 1.1.1 should be referenced as first entry.", e.getValidationErrorList().get(8));
            assertEquals("- 1.1.0: Not valid sorted!", e.getValidationErrorList().get(9));
        }
    }
}
