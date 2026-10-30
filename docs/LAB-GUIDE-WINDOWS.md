# Lab Guide (Windows): Tasks 4–15, step by step

This guide turns the project files into **your own evidence** (screenshots, logs, reports) for every task.
Everything runs on a Windows 10/11 laptop. Linux-only parts (Ansible) run in **WSL2 Ubuntu**.

> 📸 = take a screenshot for your report. Save them in an `evidence\` folder named `T07-jenkins-build.png` and so on.

**Ports used**

| Port | What |
|---|---|
| 8080 | Tomcat (`http://localhost:8080/homestay`) and `java -jar` local run |
| 9090 | Jenkins |
| 8085 | Container deployed by Jenkins |
| 8090 | Container deployed by Ansible (WSL node) |

---

## 0. One-time installs (about 30 minutes)

Open **PowerShell as Administrator**:

```powershell
winget install EclipseAdoptium.Temurin.17.JDK
winget install Git.Git
winget install GitHub.cli
winget install Docker.DockerDesktop
wsl --install -d Ubuntu-24.04
```

**Maven:** download `apache-maven-3.9.x-bin.zip` from https://maven.apache.org/download.cgi and extract it to `C:\tools\apache-maven-3.9.x`.
**Tomcat:** download the *Tomcat 10.1 "64-bit Windows zip"* from https://tomcat.apache.org/download-10.cgi and extract it to `C:\apache-tomcat-10.1` (rename the folder to exactly this).
**Jenkins:** download the *LTS* `jenkins.war` from https://www.jenkins.io/download/ into `C:\jenkins\`.

Set environment variables (Start → "Edit the system environment variables" → Environment Variables):

| Variable | Value |
|---|---|
| `JAVA_HOME` | `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot` |
| `MAVEN_HOME` | `C:\tools\apache-maven-3.9.x` |
| `Path` (add) | `%MAVEN_HOME%\bin` |

Open a **new** PowerShell and check:

```powershell
java -version ; mvn -v ; git --version ; docker version ; gh --version
```
📸 `T03-tool-versions.png`

---

## Task 3 – Working local setup

```powershell
cd $HOME\Desktop
# unzip the project you received here, then:
cd homestay-host-platform
mvn clean package
java -jar target\homestay.war
```

Open http://localhost:8080 → log in `host / host123`.
📸 `T03-login.png`, `T03-dashboard.png`. Stop with `Ctrl+C`.

> The first `mvn` run downloads dependencies (about 5 minutes). If the build fails on a test, see `docs/TROUBLESHOOTING.md`.

---

## Task 4 – Git and GitHub repository initialization

The project folder already contains a Git history prepared in the order of the tasks:

```powershell
git log --oneline --graph --all
git branch -a
```

| Branch | Contains |
|---|---|
| `main` | Initial skeleton commit (Task 4) |
| `develop` | Skeleton + one README change (used for the merge conflict in Task 6) |
| `feature/booking-management` | Feature 1: entities, login/roles, booking CRUD, search (Task 5) |
| `feature/workflow-dashboard` | Feature 2: status workflow, dashboard, API (Task 6) |
| `feature/cicd-devops` | Selenium, Jenkins, Docker, Ansible, docs (Tasks 7–15) |

1. **Set your identity** (so the commits you make are yours):
   ```powershell
   git config --global user.name "Shruti Tambade"
   git config --global user.email "<your GitHub email>"
   ```
2. On GitHub → **New repository** → name `homestay-host-platform`, **Public**, **do NOT** add a README/.gitignore (the repo must be empty).
3. Push all branches:
   ```powershell
   gh auth login            # or use a Personal Access Token when git asks for a password
   git remote add origin https://github.com/<your-user>/homestay-host-platform.git
   git push -u origin main
   git push origin develop feature/booking-management feature/workflow-dashboard feature/cicd-devops
   ```
4. GitHub → **Settings → General → Default branch** → keep `main`.
5. **Branch protection** (Settings → Branches → Add rule) for `main` and `develop`: ✔ Require a pull request before merging.
6. **Issues:** `powershell -ExecutionPolicy Bypass -File scripts\create-backlog-issues.ps1` creates the 15 backlog issues.
   GitHub → **Projects → New project → Board**, add the issues, and arrange them into To do / In progress / Done.

📸 `T04-repo-home.png` (README visible), `T04-branches.png`, `T04-issues.png`, `T04-project-board.png`, `T04-branch-protection.png`, plus a terminal shot of `git log --oneline --graph --all`.

**Deliverable:** repository URL, README, initial commits, issues, branch policy (`CONTRIBUTING.md`).

---

## Task 5 – Feature development with branching (Feature 1)

Show the Git basics on the feature branch:

```powershell
git checkout feature/booking-management
git log --oneline -5
git status
```

Make one small change yourself (so the evidence includes your own add/commit/push/pull):

```powershell
notepad src\main\resources\templates\login.html     # e.g. change the subtitle text
git add src\main\resources\templates\login.html
git commit -m "feat(ui): update login subtitle"
git push origin feature/booking-management
git pull origin feature/booking-management
git log --oneline --graph -8
```

Open the pull request:

1. GitHub → **Pull requests → New** → base: `develop` ← compare: `feature/booking-management`.
2. Title: `feat: booking management (US-01 to US-06)`, body: `Closes #1 #2 #3 #4 #5 #6`.
3. **Files changed** tab → click `+` on a line → add a review comment (for example "Validate phone length on the server too ✔"), then **Review changes → Comment**.
4. **Merge pull request** (merge commit) → **Delete branch**.

Run Feature 1 locally (`git checkout develop ; git pull ; mvn clean package ; java -jar target\homestay.war`): create a booking, edit it, search for it.

📸 `T05-git-log.png`, `T05-pr-open.png`, `T05-review-comment.png`, `T05-pr-merged.png`, `T05-feature1-running.png`

---

## Task 6 – MVP completion, merge conflict, tag

Feature 2 (`feature/workflow-dashboard`) and `develop` both changed the same lines of `README.md`, so they conflict.

1. Open the PR: base `develop` ← compare `feature/workflow-dashboard`. GitHub shows **"This branch has conflicts"** 📸 `T06-pr-conflict.png`
2. Resolve it locally:
   ```powershell
   git checkout feature/workflow-dashboard
   git pull origin feature/workflow-dashboard
   git merge origin/develop
   # CONFLICT (content): Merge conflict in README.md
   git status
   ```
   📸 `T06-conflict-terminal.png`
   Open `README.md` and find the block between `<<<<<<<` and `>>>>>>>`. **Keep the feature-branch version (the upper part, between `<<<<<<< HEAD` and `=======`)** and delete the markers and the lower part. 📸 `T06-conflict-file.png`
   ```powershell
   git add README.md
   git commit -m "merge: resolve README conflict with develop"
   git push origin feature/workflow-dashboard
   ```
3. The PR now shows "No conflicts" → **Merge** → delete the branch. 📸 `T06-pr-merged.png`
4. Release the MVP to `main`: open a PR `main` ← `develop`, title `release: v1.0.0 MVP`, then merge it.
5. Tag the release:
   ```powershell
   git checkout main ; git pull
   git tag -a v1.0.0 -m "MVP: bookings, search, role-based workflow, dashboard"
   git push origin v1.0.0
   git log --oneline --graph --all --decorate -20
   ```
   GitHub → **Releases → Draft a new release** → choose tag `v1.0.0` → attach `target\homestay.war`.
6. Close issues US-01 … US-10 and move them to **Done** on the board.

Demo the full MVP (run locally): log in as **staff** → create a booking (no Confirm button) → log out → **host** confirms it → **staff** checks the guest in and then out → **dashboard** numbers change.
📸 `T06-workflow-staff.png`, `T06-workflow-host.png`, `T06-dashboard.png`, `T06-tag.png`, `T06-release.png`

---

## Task 7 – Jenkins installation and CI job

Merge the DevOps branch first: PR `develop` ← `feature/cicd-devops` → merge (it merges cleanly if you kept the feature-branch README in Task 6). After this, `git checkout develop ; git pull`. Your working folder now has everything, including this guide.

1. Start Jenkins (keep this window open while you work):
   ```powershell
   cd C:\jenkins
   java -jar jenkins.war --httpPort=9090
   ```
   > Running Jenkins as your own user gives it access to Docker Desktop, Chrome and the Tomcat folder. Avoid the Windows-service installer for this project.
2. Open http://localhost:9090. Unlock it with the password in `%USERPROFILE%\.jenkins\secrets\initialAdminPassword`, choose **Install suggested plugins**, then create the admin user. 📸 `T07-jenkins-home.png`
3. **Manage Jenkins → Tools**:
   - JDK installations → Add JDK → Name **`JDK17`**, uncheck "Install automatically", JAVA_HOME = your JDK path.
   - Maven installations → Add Maven → Name **`Maven3`**, uncheck "Install automatically", MAVEN_HOME = `C:\tools\apache-maven-3.9.x`.
   📸 `T07-tools.png`
4. **New Item** → `homestay-ci` → **Freestyle project**:
   - Source Code Management → Git → `https://github.com/<you>/homestay-host-platform.git`, branch `*/develop`
   - Build Triggers → ✔ **Poll SCM** → `H/5 * * * *` (and ✔ Build periodically `H 2 * * *` for the nightly build)
   - Build Steps → **Invoke top-level Maven targets** → Maven Version `Maven3`, Goals `clean package`
   - Post-build Actions → **Archive the artifacts** `target/homestay.war` → **Publish JUnit test result report** `target/surefire-reports/*.xml`
5. **Build Now** → open the build → **Console Output** ends with `BUILD SUCCESS` and `Finished: SUCCESS`. 📸 `T07-console-success.png`, `T07-artifact.png`, `T07-test-result.png`
6. Trigger evidence: make a small commit to `develop` that adds a **new** file (for example `docs/sprint-notes.md` with your sprint review notes; don't edit README.md here or it will conflict with the DevOps branch), push it, and wait up to 5 minutes. A new build starts and **Polling Log** shows "Changes found". 📸 `T07-poll-trigger.png`

---

## Task 8 – Pipeline as code and Tomcat deployment

1. Start Tomcat: `C:\apache-tomcat-10.1\bin\startup.bat` → http://localhost:8080 shows the Tomcat page.
2. Jenkins → **New Item** → `homestay-tomcat-pipeline` → **Pipeline**:
   - Definition **Pipeline script from SCM** → Git → repo URL → branch `*/develop`
   - **Script Path:** `jenkins/Jenkinsfile.tomcat`
3. **Build Now**. The first run only loads the parameters, so run **Build with Parameters** again: `APP_ENV = staging`, `TOMCAT_WEBAPPS = C:\apache-tomcat-10.1\webapps`.
4. Stage view: Checkout → Build → Package → Deploy to Tomcat, all green. 📸 `T08-stage-view.png`
5. Open http://localhost:8080/homestay. The footer shows **env: staging** (the parameterized setting). Also open http://localhost:8080/homestay/api/version. 📸 `T08-deployed-app.png`, `T08-version-staging.png`
6. Re-run with `APP_ENV = prod` → the footer changes to `prod`. 📸 `T08-parameter-prod.png`

---

## Task 9 – Selenium test design and local execution

Test plan: `docs/09-selenium-test-plan.md`. The suite (5 journeys, 9 tests) lives in `src/test/java/com/homestay/platform/selenium/`.

```powershell
mvn verify -Pselenium                                  # headless Chrome
mvn verify -Pselenium -Dheadless=false                 # watch Chrome do it
mvn verify -Pselenium -Dbrowser=edge -Dheadless=false  # use Edge instead
```

Result: `Tests run: 9, Failures: 0` and `BUILD SUCCESS`. Reports are in `target\failsafe-reports\`. 📸 `T09-local-run.png`, `T09-browser-running.png`

**Failure screenshot mechanism:** temporarily break an assertion, for example in `LoginJourneyIT` change `"Dashboard"` to `"Dashboard!"`, then run `mvn verify -Pselenium -Dit.test=LoginJourneyIT`. A PNG appears in `target\selenium-screenshots\`. 📸 `T09-failure-screenshot.png`. Undo the change afterwards.

Optional HTML report: `mvn surefire-report:failsafe-report-only` → `target\site\failsafe-report.html`.

---

## Task 10 – Continuous testing in Jenkins (quality gate)

1. Jenkins → **New Item** → `homestay-cicd` → **Pipeline** → Pipeline script from SCM → branch `*/develop` → Script Path **`Jenkinsfile`**.
2. Build once to load the parameters, then **Build with Parameters**: ✔ RUN_SELENIUM, ✔ DEPLOY_TOMCAT, ☐ DOCKER_DEPLOY (Docker comes in Task 12).
3. Green run → open **Test Result** (unit + Selenium). 📸 `T10-test-report.png`
4. **Introduce a deliberate defect** (the search ignores the status filter, which only the UI tests catch):
   ```powershell
   git checkout develop ; git pull
   # in src\main\resources\templates\bookings\list.html change  name="status"  to  name="state"
   git commit -am "feat(ui): rename status filter field"
   git push
   ```
   Build → the **Selenium UI Tests** stage is red, and **Deploy to Tomcat** shows as *skipped*. The failed test and its screenshot artefact are visible. 📸 `T10-failed-pipeline.png`, `T10-failed-test.png`
5. **Fix it:**
   ```powershell
   # change name="state" back to name="status"
   git commit -am "fix(ui): restore status filter parameter name"
   git push
   ```
   Rerun → all stages green. 📸 `T10-successful-rerun.png`, and from GitHub a screenshot of the fix commit.

---

## Task 11 – Docker image and container lifecycle

Start **Docker Desktop** and wait until it shows "Engine running".

```powershell
powershell -ExecutionPolicy Bypass -File scripts\docker-lifecycle.ps1
```

The script builds and tags the image, runs the container with port mapping `8085:8080`, checks health, shows logs, inspects it, runs `exec`, then stops, starts, restarts, and writes everything to `evidence\docker-lifecycle-log.txt`. Open http://localhost:8085.
📸 `T11-image-list.png`, `T11-running-container.png`, `T11-logs.png`, `T11-docker-desktop.png`. The command reference is in `docs/11-docker-lifecycle.md`.

Clean up afterwards: `docker rm -f homestay-app` (the Jenkins pipeline creates its own container in Task 12).

---

## Task 12 – Jenkins → Docker continuous deployment

1. Create a Docker Hub account → **Account settings → Personal access tokens → Generate** (Read & Write).
2. Jenkins → **Manage Jenkins → Credentials → System → Global → Add Credentials** → *Username with password* → username = Docker Hub user, password = token, **ID = `dockerhub-creds`**.
3. Edit `Jenkinsfile`: set the default of `DOCKER_REPO` to `<your-dockerhub-user>/homestay-host-platform`, then commit and push.
4. **Build with Parameters** (job `homestay-cicd`): ✔ DOCKER_DEPLOY, ✔ PUSH_IMAGE.
5. Stages: … Selenium → Deploy to Tomcat → **Docker Build → Docker Push → Deploy Container → Smoke Test**. 📸 `T12-full-pipeline.png`
6. Evidence:
   - Docker Hub → repository → **Tags** shows `1.0.<build#>` and `latest` 📸 `T12-dockerhub-tags.png`
   - `docker ps` shows `homestay-app` running image `…:1.0.<build#>` 📸 `T12-docker-ps.png`
   - http://localhost:8085 is up
7. **End to end:** change the login subtitle, commit, push → wait for the poll → a new image `1.0.<n+1>` is built and a fresh container replaces the old one. 📸 `T12-commit-to-container.png`

---

## Task 13 – Configuration management with Ansible

The node is a **clean WSL2 Ubuntu**. Spec: `docs/13-configuration-spec.md`.

1. Docker Desktop → **Settings → Resources → WSL integration** → turn **off** integration for `Ubuntu-24.04` (the node gets its own Docker from Ansible).
2. Open **Ubuntu** from the Start menu and install Ansible:
   ```bash
   sudo apt update && sudo apt install -y ansible python3-docker
   cat /etc/wsl.conf          # should contain: [boot] systemd=true  (if not, add it, then run "wsl --shutdown" in PowerShell and reopen Ubuntu)
   ```
3. Copy the Ansible folder into Linux (it runs faster and avoids Windows permission warnings):
   ```bash
   cp -r /mnt/c/Users/<you>/Desktop/homestay-host-platform/ansible ~/ansible && cd ~/ansible
   ansible-galaxy collection install -r requirements.yml
   ```
4. Edit `inventory/group_vars/homestay_nodes.yml` → `app_image_repo: <your-dockerhub-user>/homestay-host-platform`.
5. Check and run:
   ```bash
   ansible-inventory --graph
   ansible all -m ping
   ansible-playbook playbooks/configure.yml --syntax-check
   ansible-playbook playbooks/configure.yml -K        # -K asks for your Ubuntu sudo password
   ```
   📸 `T13-ping.png`, `T13-first-run.png` (PLAY RECAP with changed > 0)
6. Verify: `id homestay ; ls -la /opt/homestay ; cat /opt/homestay/NODE-INFO.txt ; systemctl status docker --no-pager` 📸 `T13-verify.png`
   The log is saved as `~/ansible/ansible-run.log`.

---

## Task 14 – Automated provisioning and reliability validation

Use two image tags that exist on Docker Hub (for example builds `1.0.7` and `1.0.8` from Task 12; check the Tags page).

```bash
cd ~/ansible
# 1) Provision + deploy on the node
ansible-playbook playbooks/site.yml -K -e app_version=1.0.7
curl -s localhost:8090/actuator/health ; echo
# 2) Idempotency: run the exact same command again → PLAY RECAP shows changed=0
ansible-playbook playbooks/site.yml -K -e app_version=1.0.7
# 3) Upgrade to the next release
ansible-playbook playbooks/deploy.yml -K -e app_version=1.0.8
ansible-playbook playbooks/healthcheck.yml -K
# 4) Failed release → automatic recovery (this tag does not exist)
ansible-playbook playbooks/deploy.yml -K -e app_version=9.9.9
#    → "Release 9.9.9 failed. Rolling back to 1.0.8" ... "Recovered: 1.0.8 is running again."
# 5) Manual rollback to the previous stable release
ansible-playbook playbooks/rollback.yml -K
ansible-playbook playbooks/healthcheck.yml -K       # Image …:1.0.7, Health UP
cat /opt/homestay/release/current /opt/homestay/release/previous
```

To simulate a release that starts but is unhealthy, use: `-e app_version=1.0.8 -e app_health_path=/not-a-real-endpoint -e app_health_retries=3`.

📸 `T14-provisioned.png`, `T14-idempotent-changed0.png`, `T14-health.png`, `T14-auto-rollback.png`, `T14-manual-rollback.png`. Open http://localhost:8090 in the Windows browser (WSL forwards localhost).

> **No Docker Hub?** Export the image on Windows with `docker save -o C:\Users\<you>\homestay-1.0.0.tar homestay-host-platform:1.0.0`, then add `-e app_image_repo=homestay-host-platform -e app_version=1.0.0 -e app_image_archive=/mnt/c/Users/<you>/homestay-1.0.0.tar`.

---

## Task 15 – Final end-to-end release, documentation and viva

**Live demo script (about 8 minutes):**

1. Show the GitHub board (all Done), branches, PRs, tags.
2. Change something visible (for example the dashboard heading) on a `feature/final-demo` branch → PR → merge to `develop`.
3. Jenkins `homestay-cicd` starts by polling → Build → Unit Tests → Package → **Selenium gate** → Tomcat → Docker Build/Push → Deploy Container → Smoke Test.
4. Open http://localhost:8085 and show the change. Show Docker Hub's new tag.
5. In Ubuntu: `ansible-playbook playbooks/deploy.yml -K -e app_version=<new tag>` → http://localhost:8090 shows the change. Then run `rollback.yml` to show recovery.
6. Merge `develop → main`, then tag `v1.1.0` and create the GitHub release.

Record the screen with **Win + Alt + R** (Xbox Game Bar) or OBS for the video evidence.

Final documents: `docs/PROJECT-REPORT.md` (complete report), `docs/TROUBLESHOOTING.md`, the architecture docs, and the `evidence\` folder.

---

## Evidence checklist

| Task | Must-have evidence |
|---|---|
| 1–3 | Docs 01–03, diagrams, tool versions, app running locally |
| 4 | Repo URL, README, branches, issues, board, protection rule |
| 5 | Feature branch log, PR with review comment, merge |
| 6 | Conflict in terminal + file, resolved PR, tag `v1.0.0`, release, MVP demo |
| 7 | Jenkins tools, freestyle config, `BUILD SUCCESS`, archived WAR, poll trigger |
| 8 | Jenkinsfile, stage view, app on Tomcat, `env: staging` |
| 9 | Test plan, local `Tests run: 9`, failure screenshot PNG |
| 10 | Jenkins test report, red build with skipped deploy, fix commit, green rerun |
| 11 | Dockerfile, `docker-lifecycle-log.txt`, running container |
| 12 | Versioned tags on Docker Hub, full pipeline, new container after a commit |
| 13 | Config spec, playbook, first-run recap, verification |
| 14 | Idempotency `changed=0`, health UP, auto-rollback, manual rollback |
| 15 | Final repo, demo video, report, presentation |
