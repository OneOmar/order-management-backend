pipeline {
    agent any

    // Allow manual rollback (optional)
    parameters {
        string(name: 'IMAGE_TAG', defaultValue: '', description: 'ex: build-7 (leave empty = latest build)')
    }

    environment {
        // If param empty → use current build
        IMAGE_TAG = "${params.IMAGE_TAG ?: "build-${BUILD_NUMBER}"}"
    }

    stages {

        stage('Docker Deploy') {
            steps {
                // Show version used
                sh 'echo Deploying IMAGE_TAG=${IMAGE_TAG}'

                // Clean previous deployment
                sh 'docker compose down --remove-orphans || true'

                // Deploy selected version
                sh 'IMAGE_TAG=${IMAGE_TAG} docker compose up -d --build'
            }
        }

    }
}