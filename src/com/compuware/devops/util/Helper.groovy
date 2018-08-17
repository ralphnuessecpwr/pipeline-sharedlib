package com.compuware.devops.util

class Helper implements Serializable {
    def steps
    // String credentialsId

    Helper(steps) 
    {
        this.steps = steps
    }

    def echoValue(String value)
    {
        steps.bat "echo $value"
    }
}
