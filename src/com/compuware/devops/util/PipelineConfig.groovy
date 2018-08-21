package com.compuware.devops.util

class PipelineConfig implements Serializable 
{

/* 
Class to hold setup information required for the pipeline for any user
*/

public String ISPW_RuntimeConfig   = "ISPW"
public String Git_Branch           = "master"
public String SQ_Scanner_Name      = "scanner" 
public String SQ_Server_Name       = "localhost"  
public String MF_Source            = "MF_Source"
public String XLR_Template         = "A Release from Jenkins"
public String XLR_User             = "admin"	

}