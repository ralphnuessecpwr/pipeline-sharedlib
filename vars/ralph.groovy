#!/usr/bin/env groovy
import hudson.model.*
import hudson.EnvVars
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import java.net.URL
import com.compuware.devops.util.*

String Git_Credentials      = "github"
String Git_URL              = "https://github.com/${Git_Project}"
String Git_TTT_Repo         = "${ISPW_Stream}_${ISPW_Application}_Unit_Tests.git"
String Git_Branch           = "master"
String SQ_Scanner_Name      = "scanner" 
String SQ_Server_Name       = "localhost"  
String MF_Source            = "MF_Source"
String XLR_Template         = "A Release from Jenkins"
String XLR_User	            = "admin"	

def call(Map pipelineParams)
{
    node
    {
        println "Params " + pipelineParams.firstname
        Helper helper = new Helper(this)
        helper.helloWorld(pipelineParams.firstname)
        
       println "XLR_User " + XLR_User 
    }
}