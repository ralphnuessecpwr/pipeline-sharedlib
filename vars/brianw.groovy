
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
          echo 'Testes Testes 123'
      }
    }
     stage('Integration Test') {
      steps {
          echo 'Integration Test'
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
