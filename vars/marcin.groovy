//Marcin pipeline for sharedlib
def call() {

    pipeline {
    agent any
    parameters {
        string(name: 'PERSON', defaultValue: 'Mr Jenkins')
            }
            stages {
                stage('Build') {
                steps {
                    echo "Hello ${params.PERSON} from Build"
                }
                }
                stage('Test') {
                steps {
                    echo 'Testing Testing 123'
                }
                }
                stage('Integration Test') {
                steps {
                    echo 'Integration Test3'
                }
                }
                stage('Deploy') {
                steps {
                    echo 'Deploy some things'
                }
                }
            }
    }
}
