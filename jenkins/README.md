# Jenkins CI/CD: step-by-step (Docker on AWS)

Jenkins and SonarQube run as Docker containers on one EC2 instance, created by CloudFormation
(`infra/cloudformation/jenkins.yaml`). Jenkins is **configured as code** (`jenkins/casc/jenkins.yaml`): the
admin user, global settings, SonarQube server and the pipeline job are created automatically, with no
setup wizard. The pipeline itself is the repository's **`Jenkinsfile`**.

```
GitHub repo ──(poll every 2 min)──► Jenkins (Docker on EC2) ──► Docker Hub
                                       │  tool containers: Maven, Trivy, Gitleaks, Hadolint, Checkov
                                       │  SonarQube (Docker, same EC2)
                                       └──(instance role → AWS SSM)──► k3s node: helm --atomic + Prometheus gate
```

## Pipeline stages (`Jenkinsfile`)

| # | Stage | Tools | Fails the build when |
|---|---|---|---|
| 1 | Build & test | Maven: unit (Surefire) + integration (Failsafe) tests, JaCoCo | a test fails or line coverage < 70% |
| 2 | Code quality | SpotBugs + FindSecBugs, SonarQube analysis | a high-priority bug is found |
| 3 | Quality gate | SonarQube webhook | the Sonar quality gate fails |
| 4 | Security scans (parallel) | Trivy (dependencies + secrets), Gitleaks (git history), Hadolint, cfn-lint, Checkov | a CRITICAL CVE, a leaked secret, or an IaC violation is found |
| 5 | Docker build | multi-stage distroless image | |
| 6 | Image scan & SBOM | Trivy image scan, CycloneDX SBOM | a CRITICAL CVE is in the image |
| 7 | Container smoke test | runs the image, `scripts/smoke-test.sh` | health, pages, API or headers fail |
| 8 | Push image (*main only*) | Docker Hub | |
| 9 | Deploy to AWS (*main only*) | `scripts/ssm-deploy.sh` → `helm --atomic` + Prometheus gate | pods are unhealthy or critical alerts fire (the release is rolled back) |
| 10 | Verify production (*main only*) | smoke test against `APP_URL`, checks the new commit SHA is live | |

Jenkins shows the reports: **Tests**, **Coverage** (JaCoCo), **Warnings** (SpotBugs), the SBOM artifact and the Sonar link.

---

## Step 0: Before you start

1. Push the repo to GitHub and make it **public** (Jenkins clones it without credentials).
2. Create a free **Docker Hub** account and a **public** repository named `l00205200-propertyhub`.
   Then go to **Account settings → Personal access tokens → Generate** (Read & Write) and keep the token.
3. Find your IP address at https://checkip.amazonaws.com. You will enter it as `x.x.x.x/32`.
4. *(Optional)* To stop GitHub Actions building as well, delete `.github/workflows/pipeline.yml`.

## Step 1: Create the k3s deploy target (if you haven't already)

Deploy the app cluster stack (see the main README, Step 7 alternative):
```bash
bash scripts/cfn-deploy.sh learner-lab propertyhub us-east-1     # Learner Lab
bash scripts/cfn-deploy.sh personal    propertyhub eu-west-1     # personal account
```
Note the **InstanceId** and **AppUrl** outputs.

## Step 2: Create the Jenkins server with CloudFormation

**Console (as in the CloudFormation lab):** CloudFormation → *Create stack* → *With new resources* →
*Upload a template file* → `infra/cloudformation/jenkins.yaml` → stack name `propertyhub-ci`.

| Parameter | Value |
|---|---|
| AllowedCidr | `your-ip/32` |
| RepoUrl | `https://github.com/YOUR_USER/L00205200-propertyhub.git` |
| DockerHubRepo | `docker.io/YOUR_DOCKERHUB_USER/l00205200-propertyhub` |
| TargetInstanceId / AppUrl | outputs from Step 1 (blank = build only, no deploy) |
| CreateIamRoles | `false` in **Learner Lab** (uses `LabInstanceProfile`), `true` in a personal account |

Tick the IAM acknowledgement and click **Submit**. The stack shows `CREATE_COMPLETE` once Jenkins is answering,
after about **8–12 minutes**.

**CLI equivalent:**
```bash
aws cloudformation deploy --region us-east-1 --stack-name propertyhub-ci \
  --template-file infra/cloudformation/jenkins.yaml --capabilities CAPABILITY_NAMED_IAM \
  --parameter-overrides AllowedCidr=$(curl -s https://checkip.amazonaws.com)/32 \
      RepoUrl=https://github.com/YOUR_USER/L00205200-propertyhub.git \
      DockerHubRepo=docker.io/YOUR_DOCKERHUB_USER/l00205200-propertyhub \
      TargetInstanceId=i-0123456789abcdef0 AppUrl=http://1.2.3.4 \
      CreateIamRoles=false
aws cloudformation describe-stacks --region us-east-1 --stack-name propertyhub-ci \
  --query 'Stacks[0].Outputs' --output table
```

## Step 3: Log in to Jenkins

Open the **JenkinsUrl** output. The user is `admin`, and the password comes from the **JenkinsAdminPasswordCommand** output:
```bash
aws secretsmanager get-secret-value --secret-id <arn> --query SecretString --output text --region us-east-1
```
The job **L00205200-propertyhub** already exists. It was created by configuration as code.

## Step 4: Add the Docker Hub credential

**Manage Jenkins → Credentials → System → Global credentials → Add credentials**
- Kind: *Username with password*, ID **`dockerhub-creds`**
- Username: your Docker Hub user. Password: the access token from Step 0.

Secrets live only in Jenkins' encrypted credential store, never in the Jenkinsfile or git.

## Step 5: Connect SonarQube

1. Open the **SonarQubeUrl** output, log in as `admin` / `admin`, and set a new password.
2. **My Account → Security → Generate token** (type *Global analysis token*). Copy it.
3. In Jenkins, **Add credentials** → Kind *Secret text*, ID **`sonar-token`**, Secret = the token.
4. In SonarQube, **Administration → Configuration → Webhooks → Create**:
   name `jenkins`, URL **`http://jenkins:8080/sonarqube-webhook/`**. Both run on the same Docker network.
5. In the `Jenkinsfile`, change `RUN_SONAR` `defaultValue: false` to `true` and commit, or tick it under *Build with Parameters*.

## Step 6: Run the pipeline

- Jenkins scans the repo every 2 minutes. Click **L00205200-propertyhub → Scan Multibranch Pipeline Now** to start immediately.
- **Feature branches and PRs** run stages 1–7: build, test, scan and smoke test, with no push or deploy.
- **main** runs all 10 stages: push to Docker Hub, deploy to k3s, verify production.
- Open a build to see **Pipeline Overview** (stage graph), **Tests**, **Coverage** and **Warnings**.

Git workflow for the marks: `feature/*` branch → pull request on GitHub → Jenkins builds the branch → merge → main deploys.

## Step 7: Monitoring Jenkins itself (optional)

The Prometheus plugin exposes build metrics at `http://<jenkins>:8080/prometheus/` (build durations, success/failure counts),
which you can add as a scrape target and chart in Grafana next to the app dashboard.

---

## Run Jenkins on your laptop instead (Docker Desktop)

```bash
cd jenkins
cp .env.example .env        # set JENKINS_ADMIN_PASSWORD, REPO_URL, DOCKERHUB_REPO; DOCKER_GID=0 on Docker Desktop
docker compose up -d --build
# Jenkins http://localhost:8080 (admin / your password), SonarQube http://localhost:9000
```
To deploy from a laptop, Jenkins also needs AWS credentials. Add them as Jenkins credentials and wrap the deploy stage with
`withCredentials`. On EC2 the instance role does this, with no keys at all.

## Check the Jenkinsfile before pushing

```bash
curl -s -u admin -X POST -F "jenkinsfile=<Jenkinsfile" http://<jenkins>:8080/pipeline-model-converter/validate
```

## Learner Lab notes

- The lab stops EC2 instances when the session ends. Docker and both containers restart automatically next time
  (`restart: unless-stopped`), and the Elastic IP keeps the same URL.
- `LabRole` already allows SSM and Secrets Manager, so no keys are needed for the deploy stage.
- If `t3.large` is not allowed in your lab, use `t3.medium`. Builds will be slower, and SonarQube needs about 2 GB.

## Troubleshooting

| Problem | Fix |
|---|---|
| `permission denied ... docker.sock` | `DOCKER_GID` in `/opt/propertyhub/jenkins/.env` must equal `getent group docker` on the host. Then run `docker compose up -d` |
| Quality gate stage times out | Add the SonarQube webhook from Step 5.4 |
| `unauthorized` on push | Check the `dockerhub-creds` credential and that the token has write access |
| Deploy `AccessDenied` (personal account) | The k3s node needs the tag `Project=propertyhub`; the propertyhub stack adds it |
| Deploy stuck in `Pending` | The k3s node isn't registered with SSM yet, or is stopped. Start it and wait a minute |
| SonarQube container keeps restarting | The host needs `vm.max_map_count=524288` (the stack sets it). Check `docker logs ci-sonarqube-1` |
| Job not created | View `docker logs ci-jenkins-1` for configuration-as-code errors (often a wrong `REPO_URL`) |

**Teardown:** delete the `propertyhub-ci` stack (and `propertyhub`) in CloudFormation to stop all charges.
