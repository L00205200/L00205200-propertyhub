# L00205200-propertyhub: CI/CD Pipeline Project

**Student:** Raju Augustine (L00205200)
**College:** Atlantic Technological University (ATU)
**Course:** Postgraduate DevOps
**Assignment:** Task 3: Design and deploy a basic CI/CD pipeline

---

## 1. About this project

For this assignment I built a small property website called **PropertyHub**, a bit like Daft.ie or Zillow, using
**Java and Spring Boot**. Then I built a **CI/CD pipeline in Jenkins** that takes my code from GitHub, builds and
tests it, checks it for security problems, puts it in a Docker container and deploys it to **AWS**. Every step
happens automatically when I merge a change into the `main` branch.

**Live links (while my AWS stacks are running):**

| What | Link |
|---|---|
| PropertyHub website | http://34.246.109.154 |
| About page (shows my student number and the deployed commit) | http://34.246.109.154/about |
| Grafana monitoring | http://34.246.109.154/grafana |
| Docker Hub image | https://hub.docker.com/r/rajuaugustine/l00205200-propertyhub |

### What the website does

- Search for homes to **buy or rent** by county, price, bedrooms and BER rating
- Get an **instant valuation** of a home
- See the **rental potential** of a property (estimated rent and yield)
- Work out **what you can afford**, using the Irish mortgage rules (4x income for first-time buyers), stamp duty and Help to Buy
- A mortgage calculator, market insights for each county, and a simple REST API (`/api/listings`, `/api/valuation` and more)

The listings are made-up demo data. The app keeps its data in memory, so it doesn't need a database.

---

## 2. Tools I used

| Stage | Tool | What I used it for |
|---|---|---|
| Source code | **Git + GitHub** | Store the code, feature branches, pull requests, branch protection on `main` |
| Dependency updates | **Dependabot** | Opens a pull request automatically when a library is out of date |
| CI/CD server | **Jenkins** (in Docker on AWS) | Runs the whole pipeline from the `Jenkinsfile` |
| Build | **Maven**, Java 21, Spring Boot 3.5 | Compile the code and build the jar |
| Testing | **JUnit 5, MockMvc, JaCoCo** | 44 unit tests, 25 integration tests, coverage check (build fails below 70%) |
| Code quality | **SpotBugs + FindSecBugs**, **SonarQube** | Find bugs, bad code and security mistakes in my Java code |
| Security | **Trivy**, **Gitleaks**, **Hadolint**, **Checkov**, **cfn-lint** | Find vulnerable libraries, leaked passwords, and mistakes in the Dockerfile and CloudFormation |
| Container | **Docker** | Package the app as a small, secure image (distroless, runs as non-root) |
| Image registry | **Docker Hub** | Store the images built by Jenkins |
| Cloud | **AWS** (EC2, IAM, Secrets Manager, SSM) | Run Jenkins and the app |
| Infrastructure as Code | **AWS CloudFormation** | Create all the AWS servers from YAML templates |
| Orchestration | **Kubernetes (k3s) + Helm** | Run the app on the AWS server, roll back automatically if a deploy fails |
| Monitoring | **Prometheus, Grafana, Alertmanager** | Metrics, dashboards and alerts |
| Logging | **Loki** | Collect the app logs so I can search them in Grafana |

---

## 3. How the pipeline works

```
 Me ──► feature branch ──► Pull Request ──► merge to main
                                                 │
                         Jenkins checks GitHub every 2 minutes
                                                 ▼
   ┌────────────────────────────── Jenkins pipeline ──────────────────────────────┐
   │ 1. Build & test        Maven, unit + integration tests, JaCoCo coverage       │
   │ 2. Code quality        SpotBugs + SonarQube analysis                          │
   │ 3. Quality gate        waits for SonarQube to say Passed                      │
   │ 4. Security scans      Trivy, Gitleaks, Hadolint, Checkov (run in parallel)   │
   │ 5. Docker build        builds the image                                       │
   │ 6. Image scan & SBOM   Trivy scans the image, makes a list of all components  │
   │ 7. Smoke test          starts the container and tests the pages and API       │
   │ 8. Push image          pushes to Docker Hub                (main only)        │
   │ 9. Deploy to AWS       AWS SSM → Helm on Kubernetes        (main only)        │
   │10. Verify production   tests the live site, checks the new commit is live    │
   └───────────────────────────────────────────────────────────────────────────────┘
                                                 ▼
                    AWS EC2 (k3s) ──► Prometheus + Grafana + Loki
```

- If **any stage fails, the pipeline stops** and nothing gets deployed.
- Feature branches run stages 1–7 only. Only `main` is pushed and deployed.
- Each image is tagged with the Git commit (for example `sha-1cfadb33f6b6`), so I always know exactly which code is running.
- If the new version is unhealthy after the deploy, Helm **rolls back** to the old version automatically.

---

## 4. Folder structure

```
├── src/main/java/ie/propertyhub   # the Java app (calculators, service, web pages, API)
├── src/test/java                  # unit tests (*Test) and integration tests (*IT)
├── pom.xml                        # Maven build, JaCoCo and SpotBugs settings
├── Dockerfile                     # multi-stage Docker build
├── docker-compose.yml             # run the app + monitoring on my laptop
├── Jenkinsfile                    # the CI/CD pipeline
├── jenkins/                       # Jenkins Docker image + configuration as code + SonarQube
├── infra/cloudformation/          # AWS templates: propertyhub.yaml (app server), jenkins.yaml (Jenkins server)
├── helm/propertyhub/              # Kubernetes deployment (Helm chart)
├── monitoring/                    # Prometheus, Grafana, Alertmanager, Loki settings
├── scripts/                       # deploy, smoke test, create/delete stack scripts
├── .trivyignore / .gitleaksignore # security findings I reviewed and accepted (with reasons)
└── .github/dependabot.yml         # automatic dependency updates
```

---

## 5. Steps I followed

### Step 1: GitHub repository

1. Created the public repo `L00205200/L00205200-propertyhub` and pushed the code.
2. Created a `develop` branch.
3. Protected the `main` branch with a ruleset: changes must come through a pull request, with no force pushes.
4. For every change I made a branch (`feature/...` or `fix/...`), pushed it, opened a pull request and merged it.

```bash
git checkout -b feature/my-change
git add .
git commit -m "feat: my change"
git push -u origin feature/my-change
# then open a Pull Request on GitHub and merge it into main
```

### Step 2: Run it on my laptop first

```bash
mvn verify                      # build + all tests + coverage check
docker compose up -d --build    # app + Prometheus + Grafana + Alertmanager + Loki
```

- App: http://localhost:8080
- Grafana: http://localhost:3000

This let me check everything worked before I spent money on AWS. `docker compose down` stops it.

### Step 3: Docker Hub

1. Created a public repository `rajuaugustine/l00205200-propertyhub`.
2. Created an access token (Read & Write) for Jenkins to push images.

### Step 4: AWS CLI

1. In my personal AWS account I created an IAM user for the CLI and an access key.
2. Ran `aws configure` (region `eu-west-1`, Ireland) and checked it with `aws sts get-caller-identity`.

### Step 5: Create the app server with CloudFormation

```bash
bash scripts/cfn-deploy.sh personal propertyhub eu-west-1
```

`infra/cloudformation/propertyhub.yaml` creates:
- a VPC, subnet, security group (only HTTP open, **no SSH**) and an Elastic IP
- an EC2 server (t3.large) that installs **k3s (Kubernetes)**, Prometheus, Grafana, Alertmanager and Loki when it starts
- the Grafana password, generated in **AWS Secrets Manager** (never in my code)

It took about 10–15 minutes. The stack only says `CREATE_COMPLETE` when the server reports that its setup has finished.

### Step 6: Create the Jenkins server with CloudFormation

`infra/cloudformation/jenkins.yaml` (stack name `propertyhub-ci`) creates a second EC2 server running
**Jenkins and SonarQube in Docker**. Jenkins is **configured as code** (`jenkins/casc/jenkins.yaml`), so the admin user,
the SonarQube connection and the pipeline job are all created automatically. The admin password is also in Secrets
Manager. The server's IAM role is only allowed to send deploy commands to my app server.

### Step 7: Set up Jenkins

1. Logged in at `http://<jenkins-ip>:8080`.
2. **Manage Jenkins → Credentials → Global → Add Credentials:**
   - `dockerhub-creds` (username + Docker Hub token)
   - `sonar-token` (secret text, from SonarQube)
3. Added a branch filter to the job, so it only builds `main`, `develop`, `feature/*` and `fix/*`.

### Step 8: Set up SonarQube

1. Logged in at `http://<jenkins-ip>:9000` and changed the admin password.
2. **My Account → Security** → generated a *Global Analysis Token* and added it to Jenkins as `sonar-token`.
3. **Administration → Configuration → Webhooks** → added `http://jenkins:8080/sonarqube-webhook/`, so Jenkins knows when the analysis is finished.
4. Turned on `RUN_SONAR` in the `Jenkinsfile`.

Result: **Quality Gate Passed**, 0 vulnerabilities, 95% coverage, 0% duplication.

### Step 9: Run the pipeline

I merged a pull request into `main`. Jenkins picked it up, ran all 10 stages, pushed the image to Docker Hub and
deployed it to AWS. The `/about` page shows the new commit, which proves the new version is live.

### Step 10: Monitoring

- Grafana dashboard **PropertyHub Ireland (L00205200) - Service Overview**: requests per second, errors, response time,
  searches and valuations, memory and CPU, and live logs from Loki.
- Alerts in Prometheus/Alertmanager: app down, high error rate, slow responses, high CPU, pod restarts.
- To test an alert, I sent lots of requests to a test error page:
  ```bash
  for i in $(seq 1 400); do curl -s -o /dev/null http://34.246.109.154/api/chaos/error; sleep 0.3; done
  ```
  After about 2 minutes the **PropertyHubHighErrorRate** alert starts firing.

---

## 6. Problems I faced and how I fixed them

This was the most useful part of the project for learning. These are the real problems I hit.

| # | Problem | What caused it | How I fixed it |
|---|---|---|---|
| 1 | `aws configure` gave `InvalidClientTokenId` | I pasted my Docker Hub token in the AWS Access Key field by mistake | Created a new AWS access key and entered the right values |
| 2 | I accidentally showed a token and password in a screenshot | Human mistake | Deleted the key and made a new one, changed the password. Lesson: never share keys in screenshots or chat |
| 3 | The CloudFormation script failed on Windows | Git Bash changed the `file://` path to a Windows path | Changed the script to use relative paths |
| 4 | The EC2 server couldn't download anything at startup | The server started before the Elastic IP was attached, so there was no internet yet | Added a "wait for internet" loop at the start of the setup script |
| 5 | The Jenkins stack failed and Jenkins wouldn't start | A setting in the Jenkins configuration file (`excludeClientIPFromCrumb`) doesn't exist in the new Jenkins version. I also edited the wrong file the first time (the CloudFormation `jenkins.yaml` instead of `jenkins/casc/jenkins.yaml`) | Removed the old setting, merged the fix, then ran `git pull` and `docker compose up -d --build` on the server |
| 6 | Too many builds in the Jenkins queue | Dependabot created about 11 branches and Jenkins tried to build all of them | Added a branch filter: `^(main\|develop\|feature/.*\|fix/.*)$` |
| 7 | Gitleaks failed the build | It found `admin:PASSWORD` in an example in the Jenkins README. It wasn't a real password | Removed it from the README and added the finding to `.gitleaksignore` with a comment |
| 8 | Trivy found **16 CRITICAL** vulnerabilities in the Docker image | Old versions of Tomcat and Thymeleaf | Upgraded Tomcat to 10.1.62 and Thymeleaf to 3.1.5. (I first tried Tomcat 10.1.58, but that version doesn't exist.) |
| 9 | 3 CRITICAL vulnerabilities were still there | No fix is available yet in Spring 6 (the fix is only in Spring Boot 4) or in the base image | Added them to `.trivyignore` with a reason and an expiry date (31 Dec 2026), so they come back for review |
| 10 | The image scan still failed after step 9 | The `trivy image` command wasn't using the `.trivyignore` file | Added `--ignorefile` to the command in the `Jenkinsfile` |
| 11 | The smoke test failed with `exit code 23` even though the site was working | `grep -q` stops reading early, so `curl` got a write error, and `set -o pipefail` treated that as a failure | Download the page first, then search it |
| 12 | SonarQube settings pointed to the wrong server | `pom.xml` still had old **SonarCloud** settings | Removed them, so Jenkins uses my own SonarQube server |
| 13 | Gitleaks failed again after turning on SonarQube | It thought the Sonar project key setting in the Jenkinsfile was an API key because of the word "Key" | Checked it was only the project name and added it to `.gitleaksignore` |

**Dependabot pull requests:** I merged the small updates after Jenkins passed. I didn't merge the big ones
(Spring Boot 4 and Java 26), because they are major upgrades that need more testing.

---

## 7. What I learned

- A pipeline is only useful if it **stops bad code**. Trivy, Gitleaks and the tests all stopped my builds at least once, and that's what they're for.
- Not every security finding is real. You have to **read it, decide, and write down why** (in `.trivyignore` and `.gitleaksignore`).
- Build logs are the most important thing for fixing problems. Most of my fixes came from reading the failed stage log carefully.
- Infrastructure as Code is very handy: I can delete everything and create it again with one command.
- Small changes on branches with pull requests make it easy to find what broke.

---

## 8. Limitations (things I would improve)

- The site uses **HTTP**, not HTTPS. Next I would add a domain name and a certificate.
- Everything runs on **one EC2 server**, so there's no high availability.
- SonarQube is an older version (9.9) with its built-in database. That's fine for a demo, but not for real use.
- The app data is in memory, so it resets when the app restarts.
- The Jenkins CloudFormation stack shows `CREATE_FAILED` from the first attempt (problem 5), but the server itself is working.

---

## 9. Clean up (to stop AWS charges)

```bash
bash scripts/cfn-delete.sh propertyhub eu-west-1
aws cloudformation delete-stack --stack-name propertyhub-ci --region eu-west-1
```

The two t3.large servers cost roughly $2–3 a day each, so I delete them after the demo.
