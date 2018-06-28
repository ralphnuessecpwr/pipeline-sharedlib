//Marcin pipeline for sharedlib
def call(Map pipelineParms) {

    pipeline {
    agent any
/*    parameters {
        string(name: 'PERSON', defaultValue: 'Mr Jenkins')
            }
 */           
            stages {
                stage('Build') {
                steps {
                    echo "Hello $pipelineParms.key1 from Build"
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
