package com.compuware.devops.util

class PipelineConfig //implements Serializable 
{

/* 
Class to hold Jenkins server specific setup information
*/

public String ISPW_RuntimeConfig   = "ISPW"
public String Git_Branch           = "master"
public String SQ_Scanner_Name      = "Scanner"      // "scanner"
public String SQ_Server_Name       = "CWCC"         // "localhost"  
public String MF_Source            = "MF_Source"
public String XLR_Template         = "A Release from Jenkins - RNU" // "A Release from Jenkins"
public String XLR_User             = "xebialabs"    // "admin"	

}