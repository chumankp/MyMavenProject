pipeline {
  agent any

  environment {
    IMAGE_NAME = "chumankp/mymavenproject"
    IMAGE_TAG = "${env.BUILD_NUMBER}"
    WAR_NAME = "MyMavenProject-1.0-SNAPSHOT.war"
  }

  tools {
    maven 'Maven' // name of Maven in Global Tool Config
  }

  stages {
    stage('Checkout') {
      steps {
        checkout([$class: 'GitSCM',
          branches: [[name: '*/master']],
          userRemoteConfigs: [[
            url: 'https://github.com/chumankp/MyMavenProject.git',
          ]]
        ])
      }
    }

    stage('Build with Maven') {
      steps {
        bat 'mvn -B clean package -DskipTests=false'
        archiveArtifacts artifacts: 'target\\*.war', fingerprint: true
      }
    }

    stage('Prepare Docker Context') {
      steps {
        bat '''
          if not exist docker-build mkdir docker-build
          copy target\\*.war docker-build\\app.war

          (echo FROM tomcat:9.0-jdk11-openjdk)> docker-build\\Dockerfile
          (echo RUN rm -rf /usr/local/tomcat/webapps/*)>> docker-build\\Dockerfile
          (echo COPY app.war /usr/local/tomcat/webapps/ROOT.war)>> docker-build\\Dockerfile
          (echo EXPOSE 8080)>> docker-build\\Dockerfile
          (echo CMD ["catalina.sh","run"])>> docker-build\\Dockerfile
        '''
      }
    }

    stage('Build Docker Image') {
      steps {
        script {
          dockerImage = docker.build("${IMAGE_NAME}:${IMAGE_TAG}", "docker-build")
        }
      }
    }

    stage('Push to Docker Hub') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'dockerhub-creds',
          usernameVariable: 'DOCKER_USER',
          passwordVariable: 'DOCKER_PASS'
        )]) {
          bat """
            echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin
            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
            docker push ${IMAGE_NAME}:${IMAGE_TAG}
            docker push ${IMAGE_NAME}:latest
            docker logout
          """
        }
      }
    }
  }

  post {
    success {
      echo "✅ Build and Docker push succeeded: ${IMAGE_NAME}:${IMAGE_TAG}"
    }
    failure {
      echo "❌ Pipeline failed. Check the console output."
    }
  }
}
