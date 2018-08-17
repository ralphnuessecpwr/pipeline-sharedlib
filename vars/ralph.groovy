#!/usr/bin/env groovy
import hudson.model.*
import hudson.EnvVars
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import java.net.URL
import com.compuware.devops.util.*

def call(Map pipelineParams)
{
    node
    {
        Helper helper = new Helper(this)
        
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

    }
}