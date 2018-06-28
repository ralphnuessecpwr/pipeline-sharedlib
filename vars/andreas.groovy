
def call(Map pipelineParams) {

    pipeline {
        agent any
        stages {
            stage('Build') {
                steps {
                    echo "Hello from Build $pipelineParams.key1"
                    echo 'Hello from Build $pipelineParams.key1'
                    git "$pipelineParams.gitURL"
                }
            }
            stage('Test') {
                steps {
                    echo 'Testing Testing 123'
                }
            }
            stage('Integrationest') {
                steps {
                    echo 'Integrationtest '
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
