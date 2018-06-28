

def call() {
pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        echo 'Hello from Build'
      }
    }
    stage('Test') {
      steps {
          echo 'Testing Testing 123'
      }
	    }
    stage('Integration Testing') {
      steps {
          echo 'Testing Testing integration'
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
