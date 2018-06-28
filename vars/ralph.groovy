def call() 
{
    pipeline 
    {
        agent any
        stages 
        {
            stage('Build') 
            {
                steps 
                {
                    echo 'Hello from Build'
                }
            }
            stage('Test') 
            {
                steps 
                {
                    echo 'Testing Testing 123'
                }
            }
            stage('Integration Test') 
            {
                steps 
                {
                    echo 'Integration Testing Testing 666'
                }
            }
            stage('Deploy') 
            {
                steps 
                {
                    echo 'Deploy some things'
                    step([$class: 'CordellWalkerRecorder'])
                }
            }
        }

}