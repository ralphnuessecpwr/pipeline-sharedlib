#!/usr/bin/env groovy
import hudson.model.*
import hudson.EnvVars
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import java.net.URL
import com.compuware.devops.util.*

/**
 Helper Methods for the Pipeline Script
*/

/**
 Determine the ISPW Path Number for use in Total Test
 @param Level - Level Parameter is the Level returned in the ISPW Webhook
*/
def String getPathNum(String Level)
{
    return Level.charAt(Level.length() - 1)
}

/**
 Wrapper around the Git Plugin's Checkout Method
 @param URL - URL for the git server
 @param Branch - The branch that will be checked out of git
 @param Credentials - Jenkins credentials for logging into git
 @param Folder - Folder relative to the workspace that git will check out files into
*/
def gitcheckout(String URL, String Branch, String Credentials, String Folder)
{
    println "Scenario " + URL
    println "Scenario " + Branch
    println "Scenario " + Credentials

    steps.checkout changelog: false, poll: false, 
    scm: [$class: 'GitSCM', 
    branches: [[name: "*/${Branch}"]], 
    doGenerateSubmoduleConfigurations: false, 
    extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: "${Folder}"]], 
    submoduleCfg: [], 
    userRemoteConfigs: [[credentialsId: "${Credentials}", name: 'origin', url: "${URL}"]]]
}

/**
Call method to execute the pipeline from a shared library
@param pipelineParams - Map of paramter/value pairs 
*/
def call(Map pipelineParams)
{
    node
    {

        // Store parameter values in variables (easier to retrieve during code)
        def ISPW_Stream         = pipelineParams.ISPW_Stream
        def ISPW_Application    = pipelineParams.ISPW_Application
        def ISPW_Container      = pipelineParams.ISPW_Container
        def ISPW_Level          = pipelineParams.ISPW_Level
        def SetId               = pipelineParams.SetId
        def ISPW_Release        = pipelineParams.ISPW_Release
        def Owner               = pipelineParams.Owner
        def Git_Project         = pipelineParams.Git_Project
        def Git_Credentials     = pipelineParams.Git_Credentials
        def CES_Token           = pipelineParams.CES_Token
        def HCI_Conn_ID         = pipelineParams.HCI_Conn_ID
        def HCI_Token           = pipelineParams.HCI_Token
        def CC_repository       = pipelineParams.CC_repository

        def Git_URL             = "https://github.com/${Git_Project}"
        def Git_TTT_Repo        = "${ISPW_Stream}_${ISPW_Application}_Unit_Tests.git"


        // PipelineConfig is a class storing constants independant from user used throuout the pipeline
        PipelineConfig pConfig = new PipelineConfig()

        // Store properties values in variables (easier to retrieve during code)
        def Git_Branch          = pConfig.Git_Branch
        def MF_Source           = pConfig.MF_Source
        def ISPW_RuntimeConfig  = pConfig.ISPW_RuntimeConfig
        def XLR_User            = pConfig.XLR_User
        def XLR_Template        = pConfig.XLR_Template
        def SQ_Scanner_Name     = pConfig.SQ_Scanner_Name 

        // Determine the current ISPW Path and Level that the code Promotion is from
        def PathNum = getPathNum(ISPW_Level)

        // Use the Path Number to determine the right Runner JCL to use (different STEPLIB concatenations)
        def TTT_Jcl = "Runner_PATH" + PathNum + ".jcl"
        // Also set the Level that the code currently resides in
        def ISPW_Target_Level = "QA" + PathNum

        stage("Retrieve Code From ISPW")
        {
            /*
                //Retrieve the code from ISPW that has been promoted 
                steps.checkout([$class: 'IspwContainerConfiguration', 
                    componentType: '',                  // optional filter for component types in ISPW
                    connectionId: "${HCI_Conn_ID}",     
                    credentialsId: "${HCI_Token}",      
                    containerName: "${SetId}",   
                    containerType: '2',                 // 0-Assignment 1-Release 2-Set
                    ispwDownloadAll: true,             // false will not download files that exist in the workspace and haven't previous changed
                    serverConfig: '',                   // ISPW runtime config.  if blank ISPW will use the default runtime config
                    serverLevel: ''])                   // level to download the components from
            */

            steps.checkout(changelog: false, poll: false, 
                scm: [$class: 'IspwConfiguration', 
                    componentType: 'COB, COPY', 
                    connectionId: "${HCI_Conn_ID}", 
                    credentialsId: "${HCI_Token}", 
                    folderName: '', 
                    ispwDownloadAll: true, 
                    levelOption: '1', 
                    serverApplication: "${ISPW_Application}", 
                    serverConfig: "${ISPW_RuntimeConfig}", 
                    serverLevel: "${ISPW_Target_Level}", 
                    serverStream: "${ISPW_Stream}"])

        }

        stage("Retrieve Tests")
        {
            //Retrieve the Tests from Github that match that ISPWW Stream and Application
            Git_URL = "${Git_URL}/${Git_TTT_Repo}"

            //call gitcheckout wrapper function
            gitcheckout(Git_URL, Git_Branch, Git_Credentials, "tests")
        }

        // findFiles method requires the "Pipeline Utilities Plugin"
        // Get all testscenario files in the current workspace into an array
        def TTTListOfScenarios = findFiles(glob: '**/*.testscenario')

        // Get all Cobol Sources in the MF_Source folder into an array 
        def ListOfSources  = findFiles(glob: "**/${ISPW_Application}/${MF_Source}/*.cbl")

        // Define a empty array for the list of programs
        def ListOfPrograms = []
        
        // Determine program names for each source member
        ListOfSources.each
        {
            // The split method uses regex to search for patterns, therefore
            // Backslashes, Dots and Underscores which mean certain patterns in regex need to be escaped 
            // The backslash in Windows paths is duplicated in Java, therefore it need to be escaped twice
            // Trim ./cbl from the Source members to populate the array of program names
            ListOfPrograms.add(it.name.trim().split("\\.")[0])
        }

        /* 
        This stage executes any Total Test Projects related to the mainframe source that was downloaded
        */ 
        stage("Execute related Unit Tests")
        {
            // Loop through all downloaded Topaz for Total Test scenarios
            TTTListOfScenarios.each
            {

                // Get root node of the path, i.e. the name of the Total Test project
                def TTTScenarioPath        = it.path // Fully qualified name of the Total Test Scenario file
                def TTTProjectName         = it.path.trim().split("\\\\")[0] + "\\"+ it.path.trim().split("\\\\")[1]  // Total Test Project name is the root folder of the full path to the testscenario 
                def TTTScenarioFullName    = it.name  // Get the full name of the testscenario file i.e. "name.testscenario"
                def TTTScenarioName        = it.name.trim().split("\\.")[0]  // Get the name of the scenario file without ".testscenario"
                def TTTScenarioTarget      = TTTScenarioName.split("\\_")[0]  // Target Program will be the first part of the scenario name (convention)
        
                // For each of the scenarios walk through the list of source files and determine if the target matches one of the programs
                // In that case, execute the unit test.  Determine if the program name matches the target of the Total Test scenario
                if(ListOfPrograms.contains(TTTScenarioTarget))
                {
                    // Log which 
                    println "*************************"
                    println "Scenario " + TTTScenarioFullName
                    println "Path " + TTTScenarioPath
                    println "Project " + TTTProjectName
                    println "*************************"
                
                    steps.step([$class: 'TotalTestBuilder', 
                        ccClearStats: false,                // Clear out any existing Code Coverage stats for the given ccSystem and ccTestId
                        ccRepo: "${CC_repository}",
                        ccSystem: "${ISPW_Application}", 
                        ccTestId: "${BUILD_NUMBER}",        // Jenkins environment variable, resolves to build number, i.e. #177 
                        credentialsId: "${HCI_Token}", 
                        deleteTemp: true,                   // (true|false) Automatically delete any temp files created during the execution
                        hlq: '',                            // Optional - high level qualifier used when allocation datasets
                        connectionId: "${HCI_Conn_ID}",    
                        jcl: "${TTT_Jcl}",                  // Name of the JCL file in the Total Test Project to execute
                        projectFolder: "${TTTProjectName}", // Name of the Folder in the file system that contains the Total Test Project.  
                        testSuite: "${TTTScenarioFullName}",// Name of the Total Test Scenario to execute
                        useStubs: false])                   // (true|false) - Execute with or without stubs
                }
            }

            // Process the Total Test Junit result files into Jenkins
            //steps.junit allowEmptyResults: true, keepLongStdio: true, testResults: "TTTUnit/*.xml"
        }

        /* 
        This stage retrieve Code Coverage metrics from Xpediter Code Coverage for the test executed in the Pipeline
        */ 
        stage("Collect Coverage Metrics")
        {
                // Code Coverage needs to match the code coverage metrics back to the source code in order for them to be loaded in SonarQube
                // The source variable is the location of the source that was downloaded from ISPW
                def String sources="${ISPW_Application}\\${MF_Source}"

                // The Code Coverage Plugin passes it's primary configuration in the string or a file
                def ccproperties = 'cc.sources=' + sources + '\rcc.repos=' + CC_repository + '\rcc.system=' + ISPW_Application  + '\rcc.test=' + BUILD_NUMBER

                steps.step([$class: 'CodeCoverageBuilder',
                analysisProperties: ccproperties,       // Pass in the analysisProperties as a string
                analysisPropertiesPath: '',             // Pass in the analysisProperties as a file.  Not used in this example
                connectionId: "${HCI_Conn_ID}", 
                credentialsId: "${HCI_Token}"])
        }

        /* 
        This stage pushes the Source Code, Test Metrics and Coverage metrics into SonarQube and then checks the status of the SonarQube Quality Gate.  
        If the SonarQube quality date fails, the Pipeline fails and stops
        */ 
        stage("Check SonarQube Quality Gate") 
        {
            // Requires SonarQube Scanner 2.8+
            // Retrieve the location of the SonarQube Scanner.  
            def scannerHome = tool "${SQ_Scanner_Name}";   // 'scanner' is the name defined for the SonarQube scanner defined in Jenkins / Global Tool Configuration / SonarQube Scanner section
            withSonarQubeEnv('localhost')       // 'localhost' is the name of the SonarQube server defined in Jenkins / Configure Systems / SonarQube server section
            {
                // Finds all of the Total Test results files that will be submitted to SonarQube
                def TTTListOfResults = findFiles(glob: 'TTTSonar/*.xml')   // Total Test SonarQube result files are stored in TTTSonar directory

                // Build the sonar testExecutionReportsPaths property
                // Start will the property itself
                def SQ_TestResult          = "-Dsonar.testExecutionReportPaths="    

                // Loop through each result Total Test results file found
                TTTListOfResults.each 
                {
                    def TTTResultName    = it.name   // Get the name of the Total Test results file   
                    SQ_TestResult = SQ_TestResult + "TTTSonar/" + it.name +  ',' // Append the results file to the property
                }

                // Build the rest of the SonarQube Scanner Properties
                
                // Test and Coverage results
                def SQ_Scanner_Properties   = " -Dsonar.tests=tests ${SQ_TestResult} -Dsonar.coverageReportPaths=Coverage/CodeCoverage.xml"
                // SonarQube project to load results into
                SQ_Scanner_Properties = SQ_Scanner_Properties + " -Dsonar.projectKey=${JOB_NAME} -Dsonar.projectName=${JOB_NAME} -Dsonar.projectVersion=1.0"
                // Location of the Cobol Source Code to scan
                SQ_Scanner_Properties = SQ_Scanner_Properties + " -Dsonar.sources=${ISPW_Application}\\MF_Source"
                // Location of the Cobol copybooks to scan
                SQ_Scanner_Properties = SQ_Scanner_Properties + " -Dsonar.cobol.copy.directories=${ISPW_Application}\\MF_Source"  
                // File extensions for Cobol and Copybook files.  The Total Test files need that contain tests need to be defined as cobol for SonarQube to process the results
                SQ_Scanner_Properties = SQ_Scanner_Properties + " -Dsonar.cobol.file.suffixes=cbl,testsuite,testscenario,stub -Dsonar.cobol.copy.suffixes=cpy -Dsonar.sourceEncoding=UTF-8"
                
                // Call the SonarQube Scanner with properties defined above
                bat "${scannerHome}/bin/sonar-scanner" + SQ_Scanner_Properties
            }
/*
            // Wait for the results of the SonarQube Quality Gate
            timeout(time: 2, unit: 'MINUTES') 
            {
                
                // Wait for webhook call back from SonarQube.  SonarQube webhook for callback to Jenkins must be configured on the SonarQube server.
                def qg = waitForQualityGate()
                
                // Evaluate the status of the Quality Gate
                if (qg.status != 'OK')
                {
                    echo "Pipeline aborted due to quality gate failure: ${qg.status}"
                    error "Exiting Pipeline" // Exit the pipeline with an error if the SonarQube Quality Gate is failing
                }
            }   
*/
        }

        /* 
        This stage triggers a XL Release Pipeline that will move code into the high levels in the ISPW Lifecycle  
        */ 
        stage("Start release in XL Release")
        {
                // Determine the current ISPW Path and Level that the code Promotion is from
                PathNum = getPathNum(ISPW_Level)

                // Use the Path Number to determine what QA Path to Promote the code from in ISPW.  This example has seperate QA paths in ISPW Lifecycle (i.e. DEV1->QA1->STG->PRD / DEV2->QA2->STG->PRD)
                def XLRPath = "QA" + PathNum 

                // Trigger XL Release Jenkins Plugin to kickoff a Release
                steps.xlrCreateRelease releaseTitle: 'A Release for $BUILD_TAG',
                    serverCredentials: "${XLR_User}",
                    startRelease: true,
                    template: "${XLR_Template}"
                    variables:
                    [[propertyName:'ISPW_Dev_level', propertyValue: "${ISPW_Target_Level}"], // Level in ISPW that the Code resides currently
                    [propertyName: 'ISPW_RELEASE_ID', propertyValue: "${ISPW_Release}"],     // ISPW Release value from the ISPW Webhook
                    [propertyName: 'CES_Token', propertyValue: "${CES_Token}"]]
        }

    }
}