pipeline {
    agent any

    // Allow manual rollback (optional)
    parameters {
        string(name: 'IMAGE_TAG', defaultValue: '', description: 'ex: build-7 (leave empty = build and deploy a new image)')
    }

    environment {
        // If param is empty → use current build number
        IMAGE_TAG  = "${params.IMAGE_TAG ?: "build-${BUILD_NUMBER}"}"
        HEALTH_URL = 'http://localhost:8081/actuator/health'
    }

    options {
        skipDefaultCheckout()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log -1 --oneline'
            }
        }

        stage('Prepare') {
            steps {
                script {
                    def mode = params.IMAGE_TAG ? 'ROLLBACK' : 'NEW BUILD'
                    currentBuild.displayName = "#${BUILD_NUMBER} ${env.IMAGE_TAG}"
                    currentBuild.description = mode
                    echo "Mode: ${mode} | IMAGE_TAG=${env.IMAGE_TAG}"
                }

                // docker-compose.yml needs the .env file (DB credentials, JWT secret)
                sh '''
                    if [ ! -f .env ]; then
                        echo "ERROR: .env file is missing in the workspace"
                        exit 1
                    fi
                    docker --version
                    docker compose version
                '''
            }
        }

        stage('Build') {
            // Skip on rollback: we redeploy an existing image
            when {
                expression { !params.IMAGE_TAG }
            }
            steps {
                // mvnw is not executable in git
                sh 'chmod +x mvnw'
                sh './mvnw -B clean package -DskipTests'
            }
        }

        // TODO: add 'SonarQube Analysis' stage here (needs target/classes from Build)

        stage('Docker Build') {
            // Skip on rollback: the image already exists
            when {
                expression { !params.IMAGE_TAG }
            }
            steps {
                sh 'docker compose build app'
            }
        }

        stage('Stop Previous Containers') {
            steps {
                // Ignore errors if no containers exist
                sh 'docker compose down --remove-orphans || true'
            }
        }

        stage('Deploy') {
            steps {
                sh 'docker compose up -d --no-build'
                sh 'docker compose ps'
            }
        }

        stage('Health Check') {
            steps {
                echo "Waiting for application at ${HEALTH_URL}..."
                // Retry until the app answers, fail after 2 minutes
                timeout(time: 2, unit: 'MINUTES') {
                    script {
                        waitUntil {
                            sh(script: 'curl -sf "$HEALTH_URL"', returnStatus: true) == 0
                        }
                    }
                }
                echo 'Application is healthy!'
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
               Image: order-app:${env.IMAGE_TAG}
               URL: ${env.BUILD_URL}
               """
           )
       }

       failure {
           // Help debugging: show the last application logs
           sh 'docker compose logs --tail=100 app || true'

           emailext(
               from: 'elmanssouriomar@gmail.com',
               to: 'elmanssouriomar@gmail.com',
               subject: "❌ FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
               body: """
               Deployment failed!

               Job: ${env.JOB_NAME}
               Build: #${env.BUILD_NUMBER}
               Image: order-app:${env.IMAGE_TAG}
               URL: ${env.BUILD_URL}
               """
           )
       }
   }

}
