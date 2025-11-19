pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-11'
    }

    environment {
        AWS_REGION       = 'us-east-1'
        AWS_ACCOUNT_ID   = '615740708882'
        ECR_REGISTRY     = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

        USER_API_IMAGE        = "${ECR_REGISTRY}/user-api-service"
        PROFILE_API_IMAGE     = "${ECR_REGISTRY}/profile-api-service"
        NOTIFICATION_IMAGE    = "${ECR_REGISTRY}/notification-service"

        ECS_CLUSTER           = 'microservices-cluster-v2'
        USER_API_SERVICE      = 'profile-api-service'
        PROFILE_API_SERVICE   = 'profile-api-service'

        LAMBDA_FUNCTION       = 'profile-api'

        BUILD_TAG             = "${env.BUILD_NUMBER}"
    }

    options {
        timeout(time: 60, unit: 'MINUTES')        // Protect Jenkins from long loops
        disableConcurrentBuilds()                // Avoid parallel pipeline conflicts
        skipStagesAfterUnstable()
    }

    stages {

       
        /*------------------------------------------
          BUILD SERVICES (SEQUENTIAL = STABLE)
        ------------------------------------------*/
        stage('Build Services') {
            steps {
                script {
                    echo "=== Building all microservices sequentially ==="

                    def modules = [
                        'user-api-service',
                        'profile-api-service',
                        'notification-service'
                    ]

                    modules.each { svc ->
                        echo "=== Building $svc ==="
                        dir(svc) {
                            retry(2) {
                                timeout(time: 10, unit: 'MINUTES') {
                                    sh 'mvn clean package'
                                }
                            }
                        }
                    }
                }
            }
        }

        /*------------------------------------------
          RUN TESTS
        ------------------------------------------*/
        stage('Run Tests') {
            steps {
                script {
                    echo "=== Running tests ==="

                    def modules = [
                        'user-api-service',
                        'profile-api-service',
                        'notification-service'
                    ]

                    modules.each { svc ->
                        dir(svc) {
                            retry(2) {
                                sh 'mvn test'
                            }
                        }
                        junit allowEmptyResults: true, testResults: '**/${svc}/target/surefire-reports/*.xml'
                    }
                }
            }
        }

        /*------------------------------------------
          CODE QUALITY (Optional)
        ------------------------------------------*/
        stage('Code Quality Analysis') {
            steps {
                echo '=== Skipping SonarQube (optional) ==='
            }
        }

        /*------------------------------------------
          DOCKER LOGIN
        ------------------------------------------*/
        stage('Docker Login') {
            steps {
                echo "=== Logging into AWS ECR ==="
                sh """
                    aws ecr get-login-password --region ${AWS_REGION} | \
                    docker login --username AWS --password-stdin ${ECR_REGISTRY}
                """
            }
        }

        /*------------------------------------------
          BUILD DOCKER IMAGES
        ------------------------------------------*/
        stage('Build Docker Images') {
            steps {
                script {
                    echo "=== Building Docker images ==="

                    buildDocker("user-api-service", USER_API_IMAGE)
                    buildDocker("profile-api-service", PROFILE_API_IMAGE)
                    buildDocker("notification-service", NOTIFICATION_IMAGE)
                }
            }
        }

        /*------------------------------------------
          PUSH DOCKER IMAGES TO ECR
        ------------------------------------------*/
        stage('Push to ECR') {
            steps {
                script {
                    echo "=== Pushing Docker images to ECR ==="

                    pushDocker(USER_API_IMAGE)
                    pushDocker(PROFILE_API_IMAGE)
                    pushDocker(NOTIFICATION_IMAGE)
                }
            }
        }

        /*------------------------------------------
          DEPLOY TO ECS
        ------------------------------------------*/
        stage('Deploy to ECS') {
            steps {
                script {
                    echo "=== Deploying microservices to AWS ECS ==="

                    updateECSService(ECS_CLUSTER, USER_API_SERVICE)
                    updateECSService(ECS_CLUSTER, PROFILE_API_SERVICE)
                }
            }
        }

        /*------------------------------------------
          DEPLOY LAMBDA
        ------------------------------------------*/
        stage('Deploy Lambda Function') {
            steps {
                echo '=== Deploying Lambda notification-service ==='
                sh """
                    aws lambda update-function-code \
                        --function-name ${LAMBDA_FUNCTION} \
                        --image-uri ${NOTIFICATION_IMAGE}:${BUILD_NUMBER} \
                        --region ${AWS_REGION}
                """
            }
        }

        /*------------------------------------------
          HEALTH CHECK
        ------------------------------------------*/
        stage('Health Check') {
            steps {
                script {
                    echo '=== Waiting for ECS services to stabilize ==='

                    timeout(time: 10, unit: 'MINUTES') {
                        sh """
                            aws ecs wait services-stable \
                                --cluster ${ECS_CLUSTER} \
                                --services ${USER_API_SERVICE} \
                                --region ${AWS_REGION}

                            aws ecs wait services-stable \
                                --cluster ${ECS_CLUSTER} \
                                --services ${PROFILE_API_SERVICE} \
                                --region ${AWS_REGION}
                        """
                    }
                }
            }
        }
    }

    /*------------------------------------------
      POST STEPS
    ------------------------------------------*/
    post {
            success {
                echo '=== Pipeline completed successfully! ==='
                // Add notification integration here
                // emailext subject: "SUCCESS: ${currentBuild.fullDisplayName}",
                //          body: "Build ${BUILD_NUMBER} completed successfully",
                //          to: 'team@example.com'

                // slackSend channel: '#deployments',
                //           color: 'good',
                //           message: "SUCCESS: ${env.JOB_NAME} - ${env.BUILD_NUMBER}"
            }
            failure {
                echo '=== Pipeline failed! ==='
                // Add failure notification here
                // emailext subject: "FAILED: ${currentBuild.fullDisplayName}",
                //          body: "Build ${BUILD_NUMBER} failed",
                //          to: 'team@example.com'

                // slackSend channel: '#deployments',
                //           color: 'danger',
                //           message: "FAILED: ${env.JOB_NAME} - ${env.BUILD_NUMBER}"
            }
            always {
                echo '=== Cleaning up Docker images ==='
                sh '''
                    docker rmi ${USER_API_IMAGE}:${BUILD_NUMBER} || true
                    docker rmi ${USER_API_IMAGE}:latest || true
                    docker rmi ${PROFILE_API_IMAGE}:${BUILD_NUMBER} || true
                    docker rmi ${PROFILE_API_IMAGE}:latest || true
                    docker rmi ${NOTIFICATION_IMAGE}:${BUILD_NUMBER} || true
                    docker rmi ${NOTIFICATION_IMAGE}:latest || true
                '''

                // Clean workspace
                cleanWs()
            }
        }
}

/*------------------------------------------
  FUNCTIONS FOR CLEANER PIPELINE
------------------------------------------*/

def buildDocker(service, imageName) {
    dir(service) {
        sh """
            docker build --provenance=false --platform linux/amd64 -t ${imageName}:${BUILD_NUMBER} .
            docker tag ${imageName}:${BUILD_NUMBER} ${imageName}:latest
        """
    }
	
}

def pushDocker(imageName) {
    sh """
        docker push ${imageName}:${BUILD_NUMBER}
        docker push ${imageName}:latest
    """
}

def updateECSService(cluster, service) {
    sh """
        aws ecs update-service \
          --cluster ${cluster} \
          --service ${service} \
          --force-new-deployment \
          --region ${AWS_REGION}
    """
}
