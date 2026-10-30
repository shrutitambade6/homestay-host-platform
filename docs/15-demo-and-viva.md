# Task 15 – Final demo script and viva notes

**Shruti Tambade · 23102A0072 · BE CMPN A**
Containerized Homestay Host Management Platform · release **v1.1.0**

## 1. Before the demo (5 minutes)

Run these from the `Desktop\homestay-run` folder:

| Start | How | Check |
|---|---|---|
| Docker Desktop | Start menu | whale icon is green |
| Jenkins | `1-start-jenkins.bat` | http://localhost:9090 |
| Tomcat (optional) | `2-start-tomcat.bat` | http://localhost:8080/homestay |
| Container + registry | already running (`--restart unless-stopped`) | http://localhost:8085 , http://localhost:5000/v2/_catalog |
| Ansible node | Ubuntu terminal: `sudo docker start homestay-node` | http://localhost:8090 |

Logins: `host / host123`, `staff / staff123`, `admin / admin123`.

## 2. Demo flow (10–12 minutes)

1. **Problem and app (2 min).** Log in as **staff** and create a booking; it is saved as *Pending*. Point out that staff has no Confirm button. Log out, log in as **host** and confirm it. Then show the dashboard: totals, revenue and occupancy.
2. **Git (1 min).** On GitHub, show the `main` ← `develop` ← `feature/*` branches, the pull requests (#17 had the conflict that was resolved), the releases **v1.0.0** and **v1.1.0**, and the closed issues #1–#15.
3. **Jenkins CI/CD (3 min).** Open the `homestay-cicd` stage view.
   - **#4** is red: the Selenium gate caught a deliberate defect, so every deploy stage was skipped. The failure screenshot is in the archived artifacts.
   - **#5** is green after the fix.
   - **#8 / #10** are the full pipeline: build → 21 unit tests → 9 Selenium tests → Docker build → push to `localhost:5000` → deploy container → smoke test.
4. **Docker (2 min).**
   - `docker ps` shows `homestay-app (healthy)` and `homestay-registry`.
   - Open `http://localhost:5000/v2/homestay-host-platform/tags/list`.
   - Run `docker exec homestay-app whoami` → `homestay`, which proves the container does not run as root.
5. **Ansible (3 min).** In the Ubuntu terminal, `cd ~/homestay-ansible`, then:
   ```bash
   ansible-playbook playbooks/configure.yml -e app_image_repo=homestay-host-platform -e app_container_name=homestay-node -e app_data_volume=homestay-node-data   # changed=0
   ansible-playbook playbooks/healthcheck.yml -e app_container_name=homestay-node
   ```
   Then show `evidence/ansible/ansible-summary.txt`, which records the automatic rollback (`rescued=1`) and the manual `rollback.yml`.

## 3. Likely viva questions

| Question | Short answer |
|---|---|
| What is DevOps? | Dev and Ops share one automated loop: plan → code → build → test → release → deploy → operate → monitor, with fast feedback at every step. |
| CI vs CD? | CI builds and tests every commit (homestay-ci). Continuous delivery or deployment automatically promotes a tested build to an environment (homestay-cicd → Tomcat / container). |
| Why a Jenkinsfile? | Pipeline as code: versioned with the app, reviewed in pull requests, and reproducible. |
| How does Selenium act as a quality gate? | The Selenium stage runs `mvn verify -Pselenium`. Any failure fails the stage, so the deploy stages never run (build #4). |
| Why a multi-stage Dockerfile? | The build tools stay in stage 1; the runtime image has only the JRE and the WAR, so it is smaller and safer. |
| Image vs container? | An image is a read-only template made of layers; a container is a running instance of it, with a writable layer. |
| Why `HEALTHCHECK`? | Docker marks the container healthy or unhealthy, and the pipeline and Ansible wait for the health endpoint before declaring success. |
| Why a local registry? | It is a real registry API (`registry:2`) for push and pull without internet credentials; switching to Docker Hub is one parameter. |
| What is idempotency? | Running the same playbook again changes nothing when the node is already in the desired state (the reruns showed `changed=0`). |
| How does auto-rollback work? | `block/rescue`: if the pull, start or health check fails, the rescue recreates the container from the last good version recorded in `/opt/homestay/release/current`. |
| Ansible vs shell scripts? | Ansible is declarative, idempotent and agentless; its modules check state before changing it, and inventory and variables separate configuration from code. |
| Why WSL for Ansible? | The Ansible control node does not run on native Windows, and WSL2 Ubuntu gives a real Linux node on the same laptop. |
| How is the environment injected? | The `APP_ENV` Jenkins parameter → Maven resource filtering → it is shown in the footer and at `/api/version`. |
| Branch strategy? | Git-flow-lite: `feature/*` → PR → `develop` → release PR → `main` + tag. Branch protection is enabled on `main`. |
| What would you add next? | Webhooks instead of polling, PostgreSQL, Kubernetes, Prometheus/Grafana, and Trivy image scanning. |

## 4. Port map

| Port | Service |
|---|---|
| 8080 | Tomcat (Task 8 WAR deploy) |
| 9090 | Jenkins |
| 8085 | Container deployed by Jenkins |
| 5000 | Local Docker registry |
| 8090 | Container deployed by Ansible on WSL |
