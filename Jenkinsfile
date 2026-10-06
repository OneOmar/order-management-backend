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
                echo "Waiting for application..."
                sleep 30
                curl -f http://localhost:8081/actuator/health
                '''
            }
        }

    }

   post {
       success {
           emailext(
               from: 'elmanssouriomar@gmail.com',
               to: 'elmanssouriomar@gmail.com',
               subject: "✅ SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
               body: """
               Deployment successful!

               Job: ${env.JOB_NAME}
               Build: #${env.BUILD_NUMBER}
               URL: ${env.BUILD_URL}
               """
           )
       }

       failure {
           emailext(
               from: 'elmanssouriomar@gmail.com',
               to: 'elmanssouriomar@gmail.com',
               subject: "❌ FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
               body: """
               Deployment failed!

               Job: ${env.JOB_NAME}
               Build: #${env.BUILD_NUMBER}
               URL: ${env.BUILD_URL}
               """
           )
       }
   }

}