pipeline {
  agent any

  environment {
    IMAGE_NAME = "chumankp/MyMavenProject"
    IMAGE_TAG = "${env.BUILD_NUMBER}"
    WAR_NAME = "MyMavenProject-1.0-SNAPSHOT.war"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout([$class: 'GitSCM',
                  branches: [[name: '*/master']],
                  userRemoteConfigs: [[
                    url: 'https://github.com/chumankp/MyMavenProject.git',
                  ]]])
      }
    }

    stage('Build with Maven') {
      tools {
        maven 'Maven' // name of maven in Global Tool Config
      }
      steps {
        bat 'mvn -B clean package -DskipTests=false'
        archiveArtifacts artifacts: 'target/*.war', fingerprint: true
      }
    }

    stage('Prepare Docker Context') {
      steps {
        // create a small docker build context that copies the WAR into Tomcat
        bat '''
          mkdir -p docker-build
          cp target/*.war docker-build/app.war
          cat > docker-build/Dockerfile <<'DOCK'
          FROM tomcat:9.0-jdk11-openjdk
          # remove default webapps
          RUN rm -rf /usr/local/tomcat/webapps/*
          # copy our war and rename ROOT.war so app listens at /
          COPY app.war /usr/local/tomcat/webapps/ROOT.war
          EXPOSE 8080
          CMD ["catalina.sh","run"]
          DOCK
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
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          bat '''
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
            docker push ${IMAGE_NAME}:${IMAGE_TAG}
            docker push ${IMAGE_NAME}:latest
            docker logout
          '''
        }
      }
    }
  } // stages

  post {
    success {
      echo "Build, image and push succeeded: ${IMAGE_NAME}:${IMAGE_TAG}"
    }
    failure {
      echo "Pipeline failed. See console output."
    }
  }
}
