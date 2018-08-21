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

    checkout changelog: false, poll: false, 
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

        /* PropertiesInfo is a class storing constants used thruout the pipeline */
        PropertiesInfo pinfo = new PropertiesInfo()

        def Git_URL             = "https://github.com/${pipelineParams.Git_Project}"
        def Git_TTT_Repo        = "${pipelineParams.ISPW_Stream}_${pipelineParams.ISPW_Application}_Unit_Tests.git"

        // Determine the current ISPW Path and Level that the code Promotion is from
        def PathNum = getPathNum(pipelineParams.ISPW_Level)

        // Use the Path Number to determine the right Runner JCL to use (different STEPLIB concatenations)
        def TTT_Jcl = "Runner_PATH" + PathNum + ".jcl"
        // Also set the Level that the code currently resides in
        def ISPW_Target_Level = "QA" + PathNum

        stage("Retrieve Code From ISPW")
        {
                //Retrieve the code from ISPW that has been promoted 
                steps.checkout([$class: 'IspwContainerConfiguration', 
                    componentType: '',                  // optional filter for component types in ISPW
                    connectionId: "${pipelineParams.HCI_Conn_ID}",     
                    credentialsId: "${pipelineParams.HCI_Token}",      
                    containerName: "${pipelineParams.SetId}",   
                    containerType: '2',                 // 0-Assignment 1-Release 2-Set
                    ispwDownloadAll: false,             // false will not download files that exist in the workspace and haven't previous changed
                    serverConfig: '',                   // ISPW runtime config.  if blank ISPW will use the default runtime config
                    serverLevel: ''])                   // level to download the components from
        }
    }
}