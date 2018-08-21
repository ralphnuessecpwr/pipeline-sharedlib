#!/usr/bin/env groovy
import hudson.model.*
import hudson.EnvVars
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import java.net.URL
import com.compuware.devops.util.*

def checkParams(Map params)
{
    try
    {
        echo "ISPW_Stream: " + params.ISPW_Stream        
    }
    /*
    ISPW_Stream:'FTSDEMO',
    ISPW_Container:'RXN100007',
    ISPW_Level:'DEV1',
    SetId:'S',
    ISPW_Release:'RXN1REL01',
    Owner:'HDDRXM0'
    */
// The Pipeline also takes the following parameters from the Jenkins Job
// @param CES_Token - CES Personal Access Token.  These are configured in Compuware Enterprise Services / Security / Personal Access Tokens 
// @param HCI_Conn_ID - HCI Connection ID configured in the Compuware Common Configuration Plugin.  Use Pipeline Syntax Generator to determine this value. 
// @param HCI_Token - The ID of the Jenkins Credential for the TSO ID that will used to execute the pipeline
// @param CES_Connection - The URL of Compuware Enterprise Services
// @param CC_repository - The Compuware Xpediter Code Coverage Repository that the Pipeline will use
// @param Git_Project - Github project/user used to store the Topaz for Total Test Projects    
}

def call(Map pipelineParams)
{
    node
    {
        checkParams(pipelineParams)
        Helper helper = new Helper(this)
/*        
        helper.echoValue("ISPW_Stream: " + pipelineParams.ISPW_Stream)
        helper.echoValue("ISPW_Container: " + pipelineParams.ISPW_Container)
        helper.echoValue("ISPW_Level: " + pipelineParams.ISPW_Level)
        helper.echoValue("SetId: " + pipelineParams.SetId)
        helper.echoValue("ISPW_Release: " + pipelineParams.ISPW_Level)
        helper.echoValue("Owner: " + pipelineParams.Owner)

        def Git_Credentials      = "github"
        def Git_URL              = "https://github.com/${Git_Project}"
        def Git_TTT_Repo         = "${ISPW_Stream}_${ISPW_Application}_Unit_Tests.git"
        def Git_Branch           = "master"
        def SQ_Scanner_Name      = "scanner" 
        def SQ_Server_Name       = "localhost"  
        def MF_Source            = "MF_Source"
        def XLR_Template         = "A Release from Jenkins"
        def XLR_User	         = "admin"	
*/
    }
}