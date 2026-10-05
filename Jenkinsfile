pipeline {
    agent any

    environment {
        IMAGE_TAG = "build-${BUILD_NUMBER}"
    }

    stages {

        stage('Docker Deploy') {
            steps {
                sh 'IMAGE_TAG=${IMAGE_TAG} docker-compose up -d --build'
            }
        }

    }
}