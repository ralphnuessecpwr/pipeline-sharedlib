pipeline {
  agent { label 'AgentOrange'}
  parameters {
        string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
    }
  stages {
    stage('Build') {
      steps {
        echo "Hello from $PERSON"
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
