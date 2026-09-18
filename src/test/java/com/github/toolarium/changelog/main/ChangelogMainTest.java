/*
 * ChangelogMainTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.main;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Test the ChangelogMain application entry point
 *
 * @author patrick
 */
public class ChangelogMainTest {

    /**
     * Subclass that captures console output instead of printing to stdout
     */
    private static final class TestableChangelogMain extends ChangelogMain {
        private final List<String> output = new ArrayList<>();

        /**
         * Returns captured console output lines.
         * @return output list
         */
        List<String> getOutput() {
            return output;
        }

        @Override
        protected void logToConsole(String message) {
            final String msg;
            if (message != null) {
                msg = message;
            } else {
                msg = "";
            }
            output.add(msg);
        }
    }


    /**
     * Test that execute with no file argument prints usage hint
     */
    @Test
    public void testExecuteWithNoFile() {
        TestableChangelogMain main = new TestableChangelogMain();
        main.parseArguments(new String[]{});
        main.execute();
        assertTrue(main.getOutput().stream().anyMatch(s -> s.contains("Could not find the changelog file.")),
                "Expected hint message, got: " + main.getOutput());
    }


    /**
     * Test that --validate flag sets the file and validates successfully
     */
    @Test
    public void testValidateFlagWithValidChangelog() {
        TestableChangelogMain main = new TestableChangelogMain();
        main.parseArguments(new String[]{"--validate", "src/test/resources/CHANGELOG-valid.md"});
        main.execute();
        assertTrue(main.getOutput().isEmpty(), "Expected no output for valid changelog, got: " + main.getOutput());
    }


    /**
     * Test that a positional file argument is accepted
     */
    @Test
    public void testPositionalFileArgument() {
        TestableChangelogMain main = new TestableChangelogMain();
        main.parseArguments(new String[]{"src/test/resources/CHANGELOG-valid.md"});
        main.execute();
        assertTrue(main.getOutput().isEmpty(), "Expected no output for valid changelog, got: " + main.getOutput());
    }


    /**
     * Test that --verbose prints formatted changelog output after validation
     */
    @Test
    public void testVerboseFlag() {
        TestableChangelogMain main = new TestableChangelogMain();
        main.parseArguments(new String[]{"--verbose", "src/test/resources/CHANGELOG-valid.md"});
        main.execute();
        assertTrue(main.getOutput().stream().anyMatch(s -> s.contains("Validated change-log of file")),
                "Expected verbose output, got: " + main.getOutput());
    }


    /**
     * Test that validation errors are reported without version header when --no-header is set
     */
    @Test
    public void testNoHeaderFlagSuppressesVersionHeader() {
        TestableChangelogMain main = new TestableChangelogMain();
        main.parseArguments(new String[]{"--no-header", "src/test/resources/CHANGELOG-invalid.md"});
        main.execute();
        assertFalse(main.getOutput().isEmpty(), "Expected validation error output");
        assertTrue(main.getOutput().stream().noneMatch(s -> s.contains("toolarium-changelog-parser v")),
                "Version header should be suppressed with --no-header");
    }


    /**
     * Test that a non-existent file produces an I/O error message
     */
    @Test
    public void testNonExistentFileProducesError() {
        TestableChangelogMain main = new TestableChangelogMain();
        main.parseArguments(new String[]{"nonexistent-file.md"});
        main.execute();
        assertTrue(main.getOutput().stream().anyMatch(s -> s.contains("Could not read file")),
                "Expected I/O error message, got: " + main.getOutput());
    }


    /**
     * Test that an invalid changelog produces validation errors with version header
     */
    @Test
    public void testInvalidChangelogPrintsVersionHeaderAndErrors() {
        TestableChangelogMain main = new TestableChangelogMain();
        main.parseArguments(new String[]{"src/test/resources/CHANGELOG-invalid.md"});
        main.execute();
        assertFalse(main.getOutput().isEmpty(), "Expected validation error output");
        assertTrue(main.getOutput().stream().anyMatch(s -> s.contains("toolarium-changelog-parser")),
                "Expected version header in output, got: " + main.getOutput());
    }


    /**
     * Test printHelp outputs usage information
     */
    @Test
    public void testPrintHelp() {
        TestableChangelogMain main = new TestableChangelogMain();
        main.printHelp();
        assertTrue(main.getOutput().stream().anyMatch(s -> s.contains("Usage: changelog-parser")),
                "Expected usage line, got: " + main.getOutput());
        assertTrue(main.getOutput().stream().anyMatch(s -> s.contains("--validate")),
                "Expected --validate option, got: " + main.getOutput());
    }


    /**
     * Test that a blank file argument (from --validate with whitespace) is treated as missing
     */
    @Test
    public void testBlankFileArgumentTreatedAsMissing() {
        TestableChangelogMain main = new TestableChangelogMain();
        main.parseArguments(new String[]{"--validate", "   "});
        main.execute();
        assertTrue(main.getOutput().stream().anyMatch(s -> s.contains("Could not find the changelog file.")),
                "Expected missing-file hint, got: " + main.getOutput());
    }


    /**
     * Test that unknown flags before a file are ignored and the file is still processed
     */
    @Test
    public void testUnknownFlagsAreSkipped() {
        TestableChangelogMain main = new TestableChangelogMain();
        main.parseArguments(new String[]{"--unknown-flag", "src/test/resources/CHANGELOG-valid.md"});
        main.execute();
        assertTrue(main.getOutput().isEmpty(), "Expected no output for valid changelog, got: " + main.getOutput());
    }
}
