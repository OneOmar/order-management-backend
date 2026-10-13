
pipeline {
    agent any

    environment {
        IMAGE_NAME = 'ghcr.io/oneomar/order-management-backend'
        IMAGE_TAG = "k8s-${BUILD_NUMBER}"
    }

    options {
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
                sh '''
                    test -f mvnw || {
                        echo "Maven Wrapper not found"
                        exit 1
                    }

                    docker --version
                '''
            }
        }

        stage('Build JAR') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw -B clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                    docker build \
                      -t ${IMAGE_NAME}:${IMAGE_TAG} .
                '''
            }
        }

        stage('Push to GHCR') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'ghcr-credentials',
                        usernameVariable: 'GHCR_USER',
                        passwordVariable: 'GHCR_TOKEN'
                    )
                ]) {
                    sh '''
                        set +x

                        echo "$GHCR_TOKEN" | docker login ghcr.io \
                          --username "$GHCR_USER" \
                          --password-stdin

                        docker push "${IMAGE_NAME}:${IMAGE_TAG}"
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "Image published: ${IMAGE_NAME}:${IMAGE_TAG}"
        }

        failure {
            echo 'Kubernetes image build/publish pipeline failed.'
        }

        always {
            sh 'docker logout ghcr.io || true'
        }
    }
}
