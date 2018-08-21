package com.compuware.devops.util

class PropertiesInfo implements Serializable 
{

/* 
Class to hold setup information required for the pipeline
*/

    PropertiesInfo(Map propertiesInfo)
    {

    def Git_Credentials      = "87763671-db9a-47e1-80e7-33c1aba803b1"
    def Git_URL              = "https://github.com/${proprtiesInfo.Git_Project}"
    def Git_TTT_Repo         = "${propertiesInfo.ISPW_Stream}_${propertiesInfo.ISPW_Application}_Unit_Tests.git"
    def Git_Branch           = "master"
    def SQ_Scanner_Name      = "scanner" 
    def SQ_Server_Name       = "localhost"  
    def MF_Source            = "MF_Source"
    def XLR_Template         = "A Release from Jenkins"
    def XLR_User	            = "admin"	

    }
    
}