#!/usr/bin/env groovy
import hudson.model.*
import hudson.EnvVars
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import java.net.URL
import com.compuware.devops.util.*

String Git_Credentials      = "87763671-db9a-47e1-80e7-33c1aba803b1"
String Git_URL              
String Git_TTT_Repo         
String Git_Branch           = "master"
String SQ_Scanner_Name      = "scanner" 
String SQ_Server_Name       = "localhost"  
String MF_Source            = "MF_Source"
String XLR_Template         = "A Release from Jenkins"
String XLR_User	            = "admin"	

def String getPathNum(String Level)
{
    return Level.charAt(Level.length() - 1)
}

def call(Map pipelineParams)
{
    node
    {

        Git_URL             = "https://github.com/${pipelineParams.Git_Project}"
        Git_TTT_Repo        = "${pipelineParams.ISPW_Stream}_${pipelineParams.ISPW_Application}_Unit_Tests.git"

        def pathNum         = getPathNum(pipelineParams.ISPW_Level)

        Helper helper = new Helper(this)

        echo "Git_URL: " + Git_URL
        echo "Git_TTT_Repo: " + Git_TTT_Repo
        echo "Git_Branch: " + Git_Branch
        echo "pathNum: " + pathNum

    }
}