// =============================================================================
// Homestay Host Management Platform - full CI/CD pipeline (Tasks 8, 10, 12, 15)
//
//   Checkout -> Build -> Unit Tests -> Package -> Selenium quality gate
//            -> Deploy to Tomcat -> Docker build -> Push -> Deploy container -> Smoke test
//
// Works on a Windows Jenkins agent (uses `bat`) and on Linux agents (uses `sh`).
// Required Jenkins setup (see docs/LAB-GUIDE-WINDOWS.md):
//   * Global Tool Configuration: JDK named "JDK17", Maven named "Maven3"
//   * Plugins: Pipeline, Git, JUnit, Timestamper (all in "suggested plugins")
//   * PUSH_IMAGE pushes to DOCKER_REPO. Default = local registry localhost:5000 (started by the pipeline).
//     For Docker Hub set DOCKER_REPO=<user>/homestay-host-platform and add credential "dockerhub-creds".
// =============================================================================

def runCmd(String cmd) {
    if (isUnix()) { sh cmd } else { bat cmd }
}

def runStatus(String cmd) {
    return isUnix() ? sh(script: cmd, returnStatus: true) : bat(script: cmd, returnStatus: true)
}

pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    parameters {
        choice(name: 'APP_ENV', choices: ['dev', 'staging', 'prod'],
               description: 'Environment label baked into the build (shown in the app footer and /api/version)')
        booleanParam(name: 'RUN_SELENIUM', defaultValue: true,
                     description: 'Run the Selenium UI suite as a quality gate')
        booleanParam(name: 'DEPLOY_TOMCAT', defaultValue: true,
                     description: 'Copy the WAR into the local Tomcat webapps folder')
        string(name: 'TOMCAT_WEBAPPS', defaultValue: 'C:\\apache-tomcat-10.1\\webapps',
               description: 'Tomcat webapps folder on the Jenkins machine')
        string(name: 'TOMCAT_URL', defaultValue: 'http://localhost:8080/homestay',
               description: 'Base URL of the Tomcat deployment (for the smoke test)')
        booleanParam(name: 'DOCKER_DEPLOY', defaultValue: true,
                     description: 'Build a versioned Docker image and (re)deploy the container')
        booleanParam(name: 'PUSH_IMAGE', defaultValue: false,
                     description: 'Push the image to Docker Hub (needs credential dockerhub-creds)')
        string(name: 'DOCKER_REPO', defaultValue: 'localhost:5000/homestay-host-platform',
               description: 'Image repository, e.g. <dockerhub-user>/homestay-host-platform or localhost:5000/homestay-host-platform')
        string(name: 'CONTAINER_PORT', defaultValue: '8085',
               description: 'Host port for the deployed container')
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '15'))
        disableConcurrentBuilds()
        timeout(time: 45, unit: 'MINUTES')
    }

    triggers {
        // Poll GitHub every ~5 minutes; replace with a GitHub webhook if Jenkins is reachable from the internet
        pollSCM('H/5 * * * *')
    }

    environment {
        IMAGE_TAG = "1.0.${env.BUILD_NUMBER}"
        CONTAINER_NAME = 'homestay-app'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    def commit = isUnix() ? sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                                          : bat(script: '@git rev-parse --short HEAD', returnStdout: true).trim()
                    currentBuild.description = "env=${params.APP_ENV} commit=${commit} image=${env.IMAGE_TAG}"
                }
            }
        }

        stage('Build') {
            steps {
                runCmd "mvn -B clean compile -Dapp.env=${params.APP_ENV}"
            }
        }

        stage('Unit Tests') {
            steps {
                runCmd "mvn -B test -Dapp.env=${params.APP_ENV}"
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Package') {
            steps {
                runCmd "mvn -B package -DskipTests -Dapp.env=${params.APP_ENV}"
                archiveArtifacts artifacts: 'target/homestay.war', fingerprint: true
            }
        }

        stage('Selenium UI Tests') {
            when { expression { params.RUN_SELENIUM } }
            steps {
                // Failing UI tests fail this stage, so every deployment stage below is skipped.
                runCmd "mvn -B verify -Pselenium -DskipUTs=true -Dheadless=true -Dapp.env=${params.APP_ENV}"
            }
            post {
                always {
                    junit testResults: 'target/failsafe-reports/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'target/selenium-screenshots/**', allowEmptyArchive: true
                }
            }
        }

        stage('Deploy to Tomcat') {
            when { expression { params.DEPLOY_TOMCAT } }
            steps {
                script {
                    if (isUnix()) {
                        sh "cp target/homestay.war '${params.TOMCAT_WEBAPPS}/homestay.war'"
                    } else {
                        bat "copy /Y target\\homestay.war \"${params.TOMCAT_WEBAPPS}\\homestay.war\""
                    }
                }
                script {
                    timeout(time: 3, unit: 'MINUTES') {
                        waitUntil(initialRecurrencePeriod: 5000) {
                            runStatus("curl -fsS ${params.TOMCAT_URL}/api/version") == 0
                        }
                    }
                }
                echo "Deployed to Tomcat: ${params.TOMCAT_URL}"
            }
        }

        stage('Docker Build') {
            when { expression { params.DOCKER_DEPLOY } }
            steps {
                runCmd "docker build --build-arg APP_ENV=${params.APP_ENV} -t ${params.DOCKER_REPO}:${env.IMAGE_TAG} -t ${params.DOCKER_REPO}:latest ."
                runCmd "docker image ls ${params.DOCKER_REPO}"
            }
        }

        stage('Start Local Registry') {
            when { expression { params.DOCKER_DEPLOY && params.PUSH_IMAGE && params.DOCKER_REPO.startsWith('localhost:5000/') } }
            steps {
                // Local Docker registry (registry:2) on port 5000 - started once, kept running
                script {
                    if (runStatus("docker inspect -f {{.State.Running}} homestay-registry") != 0) {
                        runCmd "docker run -d --name homestay-registry --restart unless-stopped -p 5000:5000 -v homestay-registry:/var/lib/registry mirror.gcr.io/library/registry:2"
                    } else {
                        runStatus("docker start homestay-registry")
                    }
                }
            }
        }

        stage('Docker Push') {
            when { expression { params.DOCKER_DEPLOY && params.PUSH_IMAGE } }
            steps {
                script {
                    if (!params.DOCKER_REPO.startsWith('localhost:')) {
                        // Docker Hub (or another remote registry) needs credential "dockerhub-creds"
                        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                          usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
                            if (isUnix()) {
                                sh 'echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin'
                            } else {
                                bat '@echo %DH_PASS%| docker login -u %DH_USER% --password-stdin'
                            }
                        }
                    }
                }
                runCmd "docker push ${params.DOCKER_REPO}:${env.IMAGE_TAG}"
                runCmd "docker push ${params.DOCKER_REPO}:latest"
                script {
                    if (params.DOCKER_REPO.startsWith('localhost:5000/')) {
                        def name = params.DOCKER_REPO.substring('localhost:5000/'.length())
                        runCmd "curl -fsS http://localhost:5000/v2/${name}/tags/list"
                    }
                }
            }
        }

        stage('Deploy Container') {
            when { expression { params.DOCKER_DEPLOY } }
            steps {
                // Remove the old container (ignore "no such container" on the first run)
                script { runStatus("docker rm -f ${env.CONTAINER_NAME}") }
                runCmd "docker run -d --name ${env.CONTAINER_NAME} --restart unless-stopped -p ${params.CONTAINER_PORT}:8080 -v homestay-data:/app/data ${params.DOCKER_REPO}:${env.IMAGE_TAG}"
            }
        }

        stage('Smoke Test') {
            when { expression { params.DOCKER_DEPLOY } }
            steps {
                script {
                    timeout(time: 3, unit: 'MINUTES') {
                        waitUntil(initialRecurrencePeriod: 5000) {
                            runStatus("curl -fsS http://localhost:${params.CONTAINER_PORT}/actuator/health") == 0
                        }
                    }
                }
                runCmd "curl -fsS http://localhost:${params.CONTAINER_PORT}/api/version"
                runCmd "docker ps --filter name=${env.CONTAINER_NAME}"
            }
        }
    }

    post {
        success {
            echo "SUCCESS: commit -> build -> tests -> ${params.DOCKER_DEPLOY ? 'container ' + env.IMAGE_TAG + ' on port ' + params.CONTAINER_PORT : 'Tomcat'}"
        }
        failure {
            echo 'FAILED: deployment stages were skipped. Check the test report and selenium-screenshots artefact.'
        }
    }
}
