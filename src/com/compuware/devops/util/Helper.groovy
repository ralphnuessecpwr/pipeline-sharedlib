package com.compuware.devops.util

class Helper implements Serializable {
    def steps
    // String credentialsId

    Helper(steps) {
        this.steps = steps

    }

    def helloWorld(String name){
        steps.sh "echo Hello $name from " + this.getClass().getName()

    }
}
