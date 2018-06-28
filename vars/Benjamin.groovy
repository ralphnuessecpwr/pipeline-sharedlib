def call(Map pipelineParams) {
pipeline {
  agent { label 'AgentOrange'}
  
  stages {
    stage('Build') {
      steps {
        echo "Hello from $pipelineParams.nom"
        sleep 10
        input message: 'On y va ?', ok: 'Oui'
     
     
      }
    }
    stage('Test') {
      steps {
          echo 'Testing Testing 123'
      }
    }
    stage('Integration Test') {
      steps {
          echo 'Integration Testing'
      }
    }
    stage('Deploy') {
      steps {
        echo 'Deploy some things'
        step([$class: 'CordellWalkerRecorder']) 
      }
    }
  }
}
}
            
