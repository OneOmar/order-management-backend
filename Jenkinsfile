pipeline {
    agent any

    environment {
        IMAGE_TAG = "build-${BUILD_NUMBER}"
    }

    stages {

        stage('Docker Deploy') {
            steps {
                sh 'echo IMAGE_TAG=${IMAGE_TAG}'

                // CLEAN ancien déploiement
                sh 'docker compose down --remove-orphans || true'

                // nouveau déploiement
                sh 'IMAGE_TAG=${IMAGE_TAG} docker compose up -d --build'
            }
        }

    }
}