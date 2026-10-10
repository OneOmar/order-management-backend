pipeline {
    agent any

    parameters {
        string(name: 'IMAGE_TAG', defaultValue: '', description: 'Image tag for rollback (e.g. build-7)')
    }

    environment {
        IMAGE_TAG = "${params.IMAGE_TAG ?: "build-${BUILD_NUMBER}"}"
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
                // Set deployment tag and mode
                script {
                    env.IMAGE_TAG = params.IMAGE_TAG?.trim() ?: "build-${BUILD_NUMBER}"
                    currentBuild.displayName = "#${BUILD_NUMBER} ${env.IMAGE_TAG}"
                    currentBuild.description = params.IMAGE_TAG?.trim() ? 'ROLLBACK' : 'NEW BUILD'
                }

                // Check required tools and configuration
                sh '''
                    test -f .env || { echo "Missing .env file"; exit 1; }
                    docker --version
                    docker compose version
                '''
            }
        }

        stage('Validate Rollback') {
            when {
                expression { params.IMAGE_TAG?.trim() }
            }
            steps {
                // Ensure the requested image exists
                sh 'docker image inspect "order-app:${IMAGE_TAG}" >/dev/null'
            }
        }

        stage('Build') {
            when {
                expression { !params.IMAGE_TAG?.trim() }
            }
            steps {
                // Build the application JAR
                sh 'chmod +x mvnw'
                sh './mvnw -B clean package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            // Analyze only new builds, not rollbacks
            when {
                expression { !params.IMAGE_TAG?.trim() }
            }
            steps {
                // Connect to the configured SonarQube server
                withSonarQubeEnv('SonarQube') {
                    // Analyze the compiled Java code
                    sh './mvnw -B org.sonarsource.scanner.maven:sonar-maven-plugin:3.11.0.3922:sonar -Dsonar.projectKey=order-management-backend'
                }
            }
        }

        stage('Quality Gate') {
            // Check only new builds, not rollbacks
            when {
                expression { !params.IMAGE_TAG?.trim() }
            }
            steps {
                // Wait up to 5 minutes for SonarQube's result
                timeout(time: 5, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()

                        // Stop the pipeline if quality criteria fail
                        if (qg.status != 'OK') {
                            error "Quality Gate failed: ${qg.status}"
                        }
                    }
                }
            }
        }

        stage('Docker Build') {
            when {
                expression { !params.IMAGE_TAG?.trim() }
            }
            steps {
                // Build the tagged application image
                sh 'IMAGE_TAG=${IMAGE_TAG} docker compose build app'
            }
        }

        stage('Deploy') {
            steps {
                // Deploy only the application
                sh 'IMAGE_TAG=${IMAGE_TAG} docker compose up -d --no-build --no-deps app'
                sh 'docker compose ps app'
            }
        }

        stage('Health Check') {
            steps {
                // Wait up to 2 minutes for the application
                timeout(time: 2, unit: 'MINUTES') {
                    script {
                        waitUntil {
                            sh(
                                script: 'curl -fsS http://localhost:8081/actuator/health',
                                returnStatus: true
                            ) == 0
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
                to: 'elmanssouriomar@gmail.com',
                subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                Deployment successful!

                Image: order-app:${env.IMAGE_TAG}
                Build URL: ${env.BUILD_URL}
                """
            )
        }

        failure {
            // Show application logs for troubleshooting
            sh 'docker compose logs --tail=100 app || true'

            emailext(
                to: 'elmanssouriomar@gmail.com',
                subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                Deployment failed.

                Image: order-app:${env.IMAGE_TAG}
                Build URL: ${env.BUILD_URL}
                """
            )
        }
    }
}
