pipeline {
    agent any

    environment {
        MAVEN_HOME = tool name: 'Maven 3.9', type: 'maven'
        JAVA_HOME = tool name: 'JDK 17', type: 'jdk'
        PATH = "${env.MAVEN_HOME}/bin:${env.JAVA_HOME}/bin:${env.PATH}"
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📥 Checking out code...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Building project...'
                sh 'mvn clean package -DskipTests -B'
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                }
            }
        }

        stage('Unit Tests') {
            steps {
                echo '🧪 Running unit tests...'
                sh 'mvn test -B'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Tests') {
            steps {
                echo '🔗 Running integration tests...'
                sh 'mvn verify -B -P integration-test'
            }
            post {
                always {
                    junit '**/target/failsafe-reports/*.xml'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo '🚪 Checking quality gate...'
                script {
                    // Call Test Engine Platform API
                    def response = sh(
                        script: "curl -s -X POST http://localhost:8080/api/test/run -H 'Content-Type: application/json' -d '{\"packagePath\": \"com.example.functional\", \"groups\": [\"functional\"], \"checkQualityGate\": true}'",
                        returnStdout: true
                    ).trim()

                    def result = readJSON text: response
                    if (!result.success || !result.data?.qualityGateResult?.passed) {
                        error("Quality gate failed!")
                    }
                }
            }
        }

        stage('Performance Tests') {
            when {
                branch 'main'
            }
            steps {
                echo '⚡ Running performance tests...'
                script {
                    sh """
                        curl -s -X POST http://localhost:8080/api/performance/run \
                        -H 'Content-Type: application/json' \
                        -d '{
                            "url": "http://localhost:8080/api/health",
                            "threads": 50,
                            "rampUpSeconds": 10,
                            "durationSeconds": 120
                        }'
                    """
                }
            }
        }

        stage('Docker Build') {
            when {
                branch 'main'
            }
            steps {
                echo '🐳 Building Docker image...'
                sh 'docker build -t test-engine-platform:${BUILD_NUMBER} .'
                sh 'docker tag test-engine-platform:${BUILD_NUMBER} test-engine-platform:latest'
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                echo '🚀 Deploying to staging...'
                sh 'docker-compose -f docker-compose.yml up -d'
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully!'
            script {
                // Send success notification
                sh """
                    curl -s -X POST http://localhost:8080/api/inspection/alerts/test \
                    -H 'Content-Type: application/json'
                """
            }
        }
        failure {
            echo '❌ Pipeline failed!'
            script {
                // Send failure notification
                sh """
                    curl -s -X POST http://localhost:8080/api/webhook/jenkins \
                    -H 'Content-Type: application/json' \
                    -H 'X-Jenkins-Token: \${WEBHOOK_TOKEN}' \
                    -d '{
                        "name": "${JOB_NAME}",
                        "buildNumber": ${BUILD_NUMBER},
                        "status": "FAILURE"
                    }'
                """
            }
        }
        always {
            cleanWs()
        }
    }
}