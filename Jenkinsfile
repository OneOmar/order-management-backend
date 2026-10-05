pipeline {
    agent any

    stages {

        stage('Docker Deploy') {
            steps {
                sh 'docker-compose down'
                sh 'docker-compose up -d --build'
            }
        }

    }
}