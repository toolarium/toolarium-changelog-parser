/*
 * ChangelogMain.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog.main;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import jptools.logger.Logger;
import jptools.parser.ParameterParser;
import jptools.util.ParameterExecutionHolder;
import jptools.util.application.AbstractApplication;
import jptools.util.application.GenericApplicationStarter;


/**
 * Implement the change-log main application.
 * 
 * @author patrick
 */
public class ChangelogMain extends AbstractApplication {
    private static final Logger log = Logger.getLogger(ChangelogMain.class);
    private static final String VALIDATE = "--validate";
    private static final String NO_HEADER = "--no-header";
    private static final String VERBOSE = "--verbose";

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
     * @param args The arguments to run the main method.
     */
    public static void main(String[] args) {
        List<String> baseArray = new ArrayList<>(Arrays.asList(new String[] {"-type", ChangelogMain.class.getName() }));
        
        // add all arguments
        baseArray.addAll(Arrays.asList(args));
        
        GenericApplicationStarter.main(baseArray.toArray(new String[baseArray.size()]));
    }

    
    /**
     * Sets the change-log file
     *
     * @param filename the filename
     */
    public void setChangelogFilename(String filename) {
        this.file = filename;
    }
    
    
    /**
     * Sets the suppress header
     */
    public void setSuppressHeader() {
        suppressHeader = true;
    }
    
    
    /**
     * Sets the verbose mode
     */
    public void setVerbose() {
        verbose = true;
    }
    
    
    //////////////////////////////////////////////////////////////////////////
    // methods form the jptools.util.application.AbstractApplication
    //////////////////////////////////////////////////////////////////////////

    
    /**
     * @see jptools.util.application.IApplication#getVersionNumber()
     */
    @Override
    public String getVersionNumber() {
        return Version.getVersion();
    }

    
    /**
     * @see jptools.util.application.AbstractApplication#getAdditionalVersionText()
     */
    @Override
    protected String getAdditionalVersionText() {
        return "The changelog validator.\n";
    }

    
    /**
     * @see jptools.util.application.AbstractApplication#initParameters()
     */
    @Override
    protected List<ParameterExecutionHolder> initParameters() {
        List<ParameterExecutionHolder> list = new ArrayList<>();
        list.add(new ParameterExecutionHolder(VALIDATE, this, "setChangelogFilename", null, "Sets the changelog to validate.", true));
        list.add(new ParameterExecutionHolder(NO_HEADER, this, "setSuppressHeader", null, "Suppress the additional header information.", true));
        list.add(new ParameterExecutionHolder(VERBOSE, this, "setVerbose", null, "Enable verbose mode.", true));
        
        return list;
    }

    
    /**
     * @see jptools.util.application.AbstractApplication#printApplicationStartup()
     */
    @Override
    public void printApplicationStartup() {
    }
    
    
    /**
     * @see jptools.util.application.AbstractApplication#executeCalls(java.util.List, jptools.parser.ParameterParser)
     */
    @Override
    protected void executeCalls(List<ParameterExecutionHolder> parameterCalls, ParameterParser parser)
        throws Exception {
        if (parser.hasParameters()) {
            super.executeCalls(parameterCalls, parser);
        } 

        if (file == null || file.isBlank()) {
            logToConsole("Could not find the changelog file.", true);
            printHelp(false);
            return;
        }

        try {
            // check if we have remote file
            Pattern pattern = Pattern.compile(ChangelogConfig.LINK_IN_CONTENT);
            if (pattern.matcher(file).matches()) {
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
                if (verbose && changelog != null) {
                    logToConsole("Validation change-log of file " + file + ":", true);
                    logToConsole(ChangelogFactory.getInstance().format(changelogConfig, changelog), true);
                }
            } else {
                Changelog changelog = ChangelogFactory.getInstance().validate(changelogConfig, Paths.get(file));
                if (verbose && changelog != null) {
                    logToConsole("Validation change-log of file " + file + ":", true);
                    logToConsole(ChangelogFactory.getInstance().format(changelogConfig, changelog), true);
                }
            }
        } catch (IOException e) {
            logToConsole("Could not read file " + file + ": " + e.getMessage(), true);
        } catch (ValidationException e) {
            if (!suppressHeader) {
                super.printApplicationStartup();
                logToConsole("Validation errors found in file " + file + ":", true);
            }
            
            if (e.getValidationErrorList() != null) {
                logToConsole(e.getValidationErrorList().prepareString(), true);
            }
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
        URL myUrl = new URL(url);
        HttpsURLConnection conn = (HttpsURLConnection)myUrl.openConnection();
        InputStream is = conn.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String content = "";
        String inputLine;
        while ((inputLine = br.readLine()) != null) {
            content += inputLine + "\n";
        }
   
        br.close();
        
        return content;
    }

    
    /**
     * @see jptools.util.application.AbstractApplication#getLogger()
     */
    @Override
    protected Logger getLogger() {
        return log;
    }
}
