/*
 * ChangelogMain.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.changelog;

import com.github.toolarium.changelog.config.ChangelogConfig;
import com.github.toolarium.changelog.exception.ValidationException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jptools.logger.Logger;
import jptools.parser.ParameterParser;
import jptools.util.ParameterExecutionHolder;
import jptools.util.application.AbstractApplication;
import jptools.util.application.GenericApplicationStarter;


/**
 * Implement the change-log main applciation
 * 
 * @author patrick
 */
public class ChangelogMain extends AbstractApplication {
    private static final Logger log = Logger.getLogger(ChangelogMain.class);
    private static final String VALIDATE = "--validate";
    private ChangelogConfig changelogConfig;
    private String filename;

    
    /**
     * Constructor for ChangelogMain
     */
    public ChangelogMain() {
        this.changelogConfig = new ChangelogConfig();
        filename = null;
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
        this.filename = filename;
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
        list.add(new ParameterExecutionHolder(VALIDATE, this, "setChangelogFilename", null, "Sets the changelog to validate", true));
        return list;
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

        try {
            if (filename == null || filename.isBlank()) {
                logToConsole("Could not find the changelog file.", true);
                printHelp(false);
                return;
            }
            
            logToConsole("Validate file " + filename + "...", true);
            ChangelogFactory.getInstance().validate(changelogConfig, Paths.get(filename));
            
        } catch (IOException e) {
            logToConsole("Could not read file " + filename + ": " + e.getMessage(), true);
        } catch (ValidationException e) {
            StringBuilder outputInfo = new StringBuilder();
            for (String error : e.getValidationErrorList()) {
                outputInfo.append("\n");
                outputInfo.append(error);
            }
            
            logToConsole("Validation errors found in file " + filename + ": " + outputInfo, true);
        }
    }

    
    /**
     * @see jptools.util.application.AbstractApplication#getLogger()
     */
    @Override
    protected Logger getLogger() {
        return log;
    }
}
