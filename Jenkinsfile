pipeline {
  agent {
    docker {
      image 'kaiwinter/docker-java8-maven'
    }
    
  }
  stages {
    stage('') {
      steps {
        sh 'mvn clean install'
      }
    }
  }
}