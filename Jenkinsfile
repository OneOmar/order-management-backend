pipeline {
    agent any

    // Allow manual rollback (optional)
    parameters {
        string(name: 'IMAGE_TAG', defaultValue: '', description: 'ex: build-7 (leave empty = latest build)')
    }

    environment {
        // If param is empty → use current build number
        IMAGE_TAG = "${params.IMAGE_TAG ?: "build-${BUILD_NUMBER}"}"
    }

    stages {

        stage('Docker Deploy') {
            steps {
                // Display which version is being deployed
                sh 'echo Deploying IMAGE_TAG=${IMAGE_TAG}'

                // Stop and remove previous containers (ignore errors if none exist)
                sh 'docker compose down --remove-orphans || true'

                // Build and start containers with selected version
                sh 'IMAGE_TAG=${IMAGE_TAG} docker compose up -d --build'
            }
        }

        stage('Health Check') {
            steps {
                sh '''
                # Wait a bit for the app to start
                echo "Waiting for application to be ready..."
                sleep 10

                # Call health endpoint
                curl -f http://localhost:8081/actuator/health
                '''
            }
        }

    }

    post {

        // Triggered when pipeline fails
        failure {
            emailext(
                subject: "❌ FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                Build failed ❌

                Job: ${env.JOB_NAME}
                Build: #${env.BUILD_NUMBER}
                URL: ${env.BUILD_URL}
                """,
                to: "elmanssouriomar@gmail.com",
                from: "elmanssouriomar@gmail.com"
            )
        }

        // Triggered when pipeline succeeds
        success {
            emailext(
                subject: "✅ SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Deployment successful!",
                to: "elmanssouriomar@gmail.com",
                from: "elmanssouriomar@gmail.com"
            )
        }
    }

}