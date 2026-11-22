// PropertyHub Ireland - Jenkins CI/CD pipeline
// build -> test -> code quality (SpotBugs, SonarQube gate) -> security (Trivy, Gitleaks, IaC) ->
// container build, scan & smoke test -> push (main) -> deploy to AWS k3s via SSM (main) -> verify
//
// Tools run in pinned throwaway containers through the host Docker socket, so the Jenkins
// controller stays small and every run uses the same tool versions.

def MAVEN_IMAGE = 'maven:3.9.11-eclipse-temurin-21'
def MAVEN_ARGS  = '-v /var/jenkins_home/.m2:/var/jenkins_home/.m2 --network ci -e MAVEN_CONFIG= -e HOME=/tmp'
def MVN         = 'mvn -B -ntp -Dmaven.repo.local=/var/jenkins_home/.m2/repository -Duser.home=/tmp'
// Single-quoted on purpose: $WORKSPACE is expanded by the shell, so Trivy sees the workspace at the same path
def TRIVY       = 'docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v /var/jenkins_home/.trivy-cache:/root/.cache -v "$WORKSPACE:$WORKSPACE" aquasec/trivy:0.57.1'

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
        timeout(time: 60, unit: 'MINUTES')
        ansiColor('xterm')
    }

    parameters {
        booleanParam(name: 'RUN_SONAR', defaultValue: true,
                     description: 'Run SonarQube analysis + quality gate (set up the sonar-token credential first)')
        booleanParam(name: 'DEPLOY', defaultValue: true,
                     description: 'Deploy to AWS when building main')
    }

    stages {
        stage('Init') {
            steps {
                script {
                    env.FULL_SHA = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    env.TAG = "sha-${env.FULL_SHA.take(12)}"
                    env.IMAGE = env.DOCKERHUB_REPO ?: 'propertyhub'
                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${env.BRANCH_NAME ?: ''} ${env.FULL_SHA.take(7)}"
                }
                sh 'mkdir -p /var/jenkins_home/.m2 /var/jenkins_home/.trivy-cache'
            }
        }

        stage('Build & test') {
            agent { docker { image "${MAVEN_IMAGE}"; args "${MAVEN_ARGS}"; reuseNode true } }
            steps {
                // compile + unit tests (Surefire) + integration tests (Failsafe) + JaCoCo 70% coverage gate
                sh "${MVN} verify"
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml, target/failsafe-reports/*.xml'
                    recordCoverage(tools: [[parser: 'JACOCO', pattern: 'target/site/jacoco/jacoco.xml']],
                                   sourceDirectories: [[path: 'src/main/java']])
                }
                success {
                    archiveArtifacts artifacts: 'target/propertyhub.jar', fingerprint: true
                }
            }
        }

        stage('Code quality') {
            agent { docker { image "${MAVEN_IMAGE}"; args "${MAVEN_ARGS}"; reuseNode true } }
            steps {
                // SpotBugs + FindSecBugs: fails on high-priority bugs
                sh "${MVN} -DskipTests spotbugs:check"
                script {
                    if (params.RUN_SONAR) {
                        withSonarQubeEnv('sonarqube') {
                            sh "${MVN} -DskipTests org.sonarsource.scanner.maven:sonar-maven-plugin:sonar " +
                               "-Dsonar.projectKey=L00205200-propertyhub -Dsonar.projectName=L00205200-propertyhub"
                        }
                    }
                }
            }
            post {
                always {
                    recordIssues(tools: [spotBugs(pattern: 'target/spotbugsXml.xml')])
                }
            }
        }

        stage('Quality gate') {
            when { expression { params.RUN_SONAR } }
            steps {
                // Needs the SonarQube webhook -> http://jenkins:8080/sonarqube-webhook/
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Security scans') {
            parallel {
                stage('Trivy dependencies') {
                    steps {
                        sh "${TRIVY} fs --scanners vuln,secret --severity CRITICAL --ignore-unfixed --exit-code 1 " +
                           "--ignorefile ${WORKSPACE}/.trivyignore ${WORKSPACE}"
                    }
                }
                stage('Gitleaks secrets') {
                    steps {
                        sh 'docker run --rm -v "$WORKSPACE:/repo" ' +
                           '-e GIT_CONFIG_COUNT=1 -e GIT_CONFIG_KEY_0=safe.directory -e GIT_CONFIG_VALUE_0=* ' +
                           'zricethezav/gitleaks:v8.21.2 detect --source /repo --redact --verbose'
                    }
                }
                stage('IaC & Dockerfile lint') {
                    steps {
                        sh 'docker run --rm -i hadolint/hadolint:v2.12.0 hadolint --failure-threshold error - < Dockerfile'
                        sh 'docker run --rm -v "$WORKSPACE:/src" -w /src python:3.12-slim sh -c ' +
                           '"pip install -q --root-user-action=ignore checkov cfn-lint && ' +
                           'cfn-lint infra/cloudformation/*.yaml && ' +
                           'checkov -d infra/cloudformation --framework cloudformation --compact --quiet && ' +
                           'checkov -f Dockerfile --framework dockerfile --compact --quiet"'
                    }
                }
            }
        }

        stage('Docker build') {
            steps {
                sh 'docker build --pull --build-arg APP_VERSION=$TAG --build-arg GIT_COMMIT=$FULL_SHA -t $IMAGE:$TAG .'
            }
        }

        stage('Image scan & SBOM') {
            steps {
                sh "${TRIVY} image --severity CRITICAL --ignore-unfixed --exit-code 1 --ignorefile \$WORKSPACE/.trivyignore \$IMAGE:\$TAG"
                sh "${TRIVY} image --format cyclonedx --output \$WORKSPACE/sbom.cdx.json \$IMAGE:\$TAG"
                archiveArtifacts artifacts: 'sbom.cdx.json', allowEmptyArchive: true
            }
        }

        stage('Container smoke test') {
            steps {
                sh 'docker run -d --name ph-smoke-$BUILD_NUMBER --network ci --read-only --tmpfs /tmp $IMAGE:$TAG'
                sh 'bash scripts/smoke-test.sh http://ph-smoke-$BUILD_NUMBER:8080 $FULL_SHA'
            }
            post {
                always {
                    sh 'docker logs ph-smoke-$BUILD_NUMBER > smoke-container.log 2>&1 || true; docker rm -f ph-smoke-$BUILD_NUMBER || true'
                    archiveArtifacts artifacts: 'smoke-container.log', allowEmptyArchive: true
                }
            }
        }

        stage('Push image') {
            when { branch 'main' }
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'DH_USER', passwordVariable: 'DH_TOKEN')]) {
                    sh '''
                        echo "$DH_TOKEN" | docker login -u "$DH_USER" --password-stdin
                        docker tag "$IMAGE:$TAG" "$IMAGE:latest"
                        docker push "$IMAGE:$TAG"
                        docker push "$IMAGE:latest"
                        docker logout
                    '''
                }
            }
        }

        stage('Deploy to AWS') {
            when {
                allOf {
                    branch 'main'
                    expression { params.DEPLOY && env.EC2_INSTANCE_ID?.trim() }
                }
            }
            environment {
                INSTANCE_ID = "${env.EC2_INSTANCE_ID}"
                IMAGE_REPO  = "${env.IMAGE}"
                IMAGE_TAG   = "${env.TAG}"
                GIT_SHA     = "${env.FULL_SHA}"
            }
            steps {
                // AWS credentials come from the Jenkins EC2 instance role (no keys stored in Jenkins).
                // The node runs helm --atomic plus a Prometheus gate, and rolls back on failure.
                sh 'aws sts get-caller-identity'
                sh 'bash scripts/ssm-deploy.sh'
            }
        }

        stage('Verify production') {
            when {
                allOf {
                    branch 'main'
                    expression { params.DEPLOY && env.APP_URL?.trim() }
                }
            }
            steps {
                sh 'bash scripts/smoke-test.sh "$APP_URL" "$FULL_SHA"'
            }
        }
    }

    post {
        always {
            sh 'docker image rm $IMAGE:$TAG >/dev/null 2>&1 || true'
        }
        success {
            echo "Pipeline passed for ${env.TAG}"
        }
        failure {
            echo 'Pipeline failed - check the stage logs and the Tests / Coverage / Warnings reports.'
        }
        cleanup {
            cleanWs(deleteDirs: true, disableDeferredWipeout: true, notFailBuild: true)
        }
    }
}
