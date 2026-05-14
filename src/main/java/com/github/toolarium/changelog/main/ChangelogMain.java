/*
 * ChangelogMain.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.main;

import com.github.toolarium.ansi.AnsiStringBuilder;
import com.github.toolarium.ansi.color.ForegroundColor;
import com.github.toolarium.changelog.ChangelogFactory;
import com.github.toolarium.changelog.Version;
import com.github.toolarium.changelog.config.ChangelogConfig;
import com.github.toolarium.changelog.dto.Changelog;
import com.github.toolarium.changelog.dto.ChangelogErrorList;
import com.github.toolarium.changelog.parser.ChangelogParseResult;
import com.github.toolarium.changelog.validator.ValidationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.regex.Pattern;


/**
 * Implement the change-log main application.
 *
 * @author patrick
 */
public class ChangelogMain {
    private static final PrintStream OUT = System.out; // CHECKSTYLE_IGNORE_THIS_LINE
    private static final String VALIDATE = "--validate";
    private static final String NO_HEADER = "--no-header";
    private static final String VERBOSE = "--verbose";
    private static final Pattern LINK_PATTERN = Pattern.compile(ChangelogConfig.LINK_IN_CONTENT);

    private ChangelogConfig changelogConfig;
    private String file;
    private boolean suppressHeader;
    private boolean verbose;


    /**
     * Constructor for ChangelogMain
     */
    public ChangelogMain() {
        this.changelogConfig = new ChangelogConfig();
        file = null;
        suppressHeader = false;
        verbose = false;
    }


    /**
     * The main entry-point of an application.
     *
     * @param args The arguments to run the main method.
     */
    public static void main(String[] args) {
        ChangelogMain app = new ChangelogMain();
        app.parseArguments(args);
        app.execute();
    }


    /**
     * Parse command line arguments.
     *
     * @param args the arguments
     */
    protected void parseArguments(String[] args) {
        int idx = 0;
        while (idx < args.length) {
            if (VALIDATE.equals(args[idx]) && idx + 1 < args.length) {
                file = args[idx + 1];
                idx += 2;
            } else if (NO_HEADER.equals(args[idx])) {
                suppressHeader = true;
                idx++;
            } else if (VERBOSE.equals(args[idx])) {
                verbose = true;
                idx++;
            } else {
                if (!args[idx].startsWith("-")) {
                    file = args[idx];
                }
                idx++;
            }
        }
    }


    /**
     * Execute the changelog validation.
     */
    protected void execute() {
        if (file == null || file.isBlank()) {
            logToConsole(new AnsiStringBuilder()
                    .color(ForegroundColor.YELLOW, "Could not find the changelog file.")
                    .toString());
            printHelp();
            return;
        }

        try {
            // check if we have remote file
            if (LINK_PATTERN.matcher(file).matches()) {
                executeRemoteValidation();
            } else {
                executeLocalValidation();
            }
        } catch (IOException e) {
            logToConsole(new AnsiStringBuilder()
                    .color(ForegroundColor.RED, "Could not read file ")
                    .bold(file)
                    .color(ForegroundColor.RED, ": " + e.getMessage())
                    .toString());
        } catch (ValidationException e) {
            if (!suppressHeader) {
                printVersion();
                logToConsole(new AnsiStringBuilder()
                        .bold().color(ForegroundColor.RED, "Validation errors found in file ")
                        .resetBold().color(ForegroundColor.RED, file + ":")
                        .toString());
            }

            if (e.getValidationErrorList() != null) {
                logToConsole(new AnsiStringBuilder()
                        .color(ForegroundColor.YELLOW, e.getValidationErrorList().prepareString())
                        .toString());
            }
        }
    }


    /**
     * Execute validation for a remote changelog file.
     *
     * @throws IOException In case of I/O errors
     * @throws ValidationException In case of validation errors
     */
    protected void executeRemoteValidation() throws IOException, ValidationException {
        Changelog changelog = null;
        ChangelogErrorList parseChangelogErrorList = null;
        try {
            String content = readContent(file);
            ChangelogParseResult result = ChangelogFactory.getInstance().parse(content);
            parseChangelogErrorList = result.getChangelogErrorList();

            ChangelogFactory.getInstance().validate(changelogConfig, result.getChangelog());
            changelog = result.getChangelog();
        } catch (ValidationException e) {
            if (e.getValidationErrorList() != null && !e.getValidationErrorList().isEmpty()) {
                parseChangelogErrorList.add(e.getValidationErrorList());
            }

            throw new ValidationException(e.getMessage(), parseChangelogErrorList);
        }

        if (parseChangelogErrorList != null && !parseChangelogErrorList.isEmpty()) {
            throw new ValidationException("Changelog parse errors.", parseChangelogErrorList);
        }

        printVerboseOutput(changelog);
    }


    /**
     * Execute validation for a local changelog file.
     *
     * @throws IOException In case of I/O errors
     * @throws ValidationException In case of validation errors
     */
    protected void executeLocalValidation() throws IOException, ValidationException {
        Changelog changelog = ChangelogFactory.getInstance().validate(changelogConfig, Paths.get(file));
        printVerboseOutput(changelog);
    }


    /**
     * Print verbose changelog output if enabled.
     *
     * @param changelog the validated changelog
     * @throws IOException In case of formatting errors
     */
    protected void printVerboseOutput(Changelog changelog) throws IOException {
        if (verbose && changelog != null) {
            logToConsole(new AnsiStringBuilder()
                    .color(ForegroundColor.GREEN, "Validated change-log of file ")
                    .bold(file)
                    .color(ForegroundColor.GREEN, ":")
                    .toString());
            logToConsole(ChangelogFactory.getInstance().format(changelogConfig, changelog));
        }
    }


    /**
     * Read the content
     *
     * @param url the url
     * @return the content
     * @throws MalformedURLException In case of invalid url exception
     * @throws IOException In case of I/O errors
     */
    protected String readContent(String url) throws MalformedURLException, IOException {
        URL myUrl = URI.create(url).toURL();
        HttpURLConnection conn = (HttpURLConnection) myUrl.openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);

        try (InputStream is = conn.getInputStream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                content.append(inputLine).append("\n");
            }

            return content.toString();
        } finally {
            conn.disconnect();
        }
    }


    /**
     * Print version information.
     */
    protected void printVersion() {
        logToConsole(new AnsiStringBuilder()
                .append("toolarium-changelog-parser v")
                .bold().color(ForegroundColor.CYAN, Version.getVersion()).resetBold()
                .toString());
    }


    /**
     * Print help information.
     */
    protected void printHelp() {
        printVersion();
        logToConsole(new AnsiStringBuilder()
                .append("Usage: changelog-parser [options]")
                .toString());
        logToConsole(new AnsiStringBuilder()
                .append("  ").bold(VALIDATE).append(" <file>  Sets the changelog to validate.")
                .toString());
        logToConsole(new AnsiStringBuilder()
                .append("  ").bold(NO_HEADER).append("         Suppress the additional header information.")
                .toString());
        logToConsole(new AnsiStringBuilder()
                .append("  ").bold(VERBOSE).append("          Enable verbose mode.")
                .toString());
    }


    /**
     * Log a message to the console using stdout.
     *
     * @param message the message to log
     */
    protected void logToConsole(String message) {
        OUT.println(message);
    }
}
