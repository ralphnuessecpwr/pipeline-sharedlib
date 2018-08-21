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

        def Git_URL             = "https://github.com/${Git_Project}"
        def Git_TTT_Repo        = "${ISPW_Stream}_${ISPW_Application}_Unit_Tests.git"


        // PipelineConfig is a class storing constants independant from user used throuout the pipeline
        PipelineConfig pConfig = new PipelineConfig()

        // Store properties values in variables (easier to retrieve during code)
        def Git_Branch          = pConfig.Git_Branch
        def MF_Source           = pConfig.MF_Source
        def ISPW_RuntimeConfig  = pConfig.ISPW_RuntimeConfig
        def CC_repository       = pConfig.CC_repository

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
            steps.junit allowEmptyResults: true, keepLongStdio: true, testResults: "TTTUnit/*.xml"
        }

    }
}