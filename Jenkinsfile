pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['qa', 'dev'], description: 'Target environment')
        booleanParam(name: 'SMOKE_ONLY', defaultValue: false, description: 'Run only the smoke suite')
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    tools {
        jdk 'jdk-17'
        maven 'maven-3.9'
    }

    environment {
        ENV = "${params.ENVIRONMENT}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Run API tests') {
            steps {
                script {
                    def suiteArgs = params.SMOKE_ONLY ? '-Psmoke' : ''
                    sh "./mvnw -B -ntp clean test -Denv=${ENV} ${suiteArgs}"
                }
            }
            post {
                always {
                    // Publish TestNG results and attach the Allure output.
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/allure-results/**', allowEmptyArchive: true
                }
            }
        }
    }

    post {
        failure {
            echo 'API tests failed. Download target/allure-results and run: allure serve'
        }
        success {
            echo 'API tests passed.'
        }
    }
}
