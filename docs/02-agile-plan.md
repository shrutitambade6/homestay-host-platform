# Task 2 – Agile Planning and DevOps Workflow

**Project:** Containerized Homestay Host Management Platform · Shruti Tambade (23102A0072)

Method: **Scrum-ban** – 5 one-week sprints, tracked on a **GitHub Projects Kanban board** (To do → In progress → In review → Done). Each backlog item is a GitHub Issue (template: `.github/ISSUE_TEMPLATE/user_story.md`).

---

## 1. User stories with acceptance criteria

| ID | User story | Acceptance criteria | Priority | Points |
|---|---|---|---|---|
| US-01 | As a **user**, I want to log in with my role so that I only see what I am allowed to do. | Valid credentials open the dashboard; wrong password shows "Invalid username or password"; any page redirects to /login when logged out; logout returns to login. | Must | 3 |
| US-02 | As a **host**, I want to add and edit my properties so that bookings can be made against them. | Name, location, rooms (1–100), price > 0 required; property appears in the list; Staff gets 403 on add/edit. | Must | 3 |
| US-03 | As **staff**, I want to create a booking so that walk-in and phone guests are recorded. | Required: name, 10-digit phone, property, check-in, check-out, guests 1–20; check-out after check-in; saved as **Pending** with reference HS-xxxxx; amount = nights × price. | Must | 5 |
| US-04 | As a **user**, I want to view a booking's full details so that I can answer guest questions. | Page shows guest, phone, email, property, dates, nights, guests, total, notes, created by, last updated by, status badge. | Must | 2 |
| US-05 | As a **user**, I want to update a booking so that date or contact changes are captured. | Edit allowed only when Pending/Confirmed; amount recalculated; "updated" message; last-updated-by changes. | Must | 3 |
| US-06 | As a **host**, I want to search bookings by name, phone, reference, status and property so that I find a stay quickly. | Partial, case-insensitive match; filters combine; result count shown; "No bookings match" when empty. | Must | 3 |
| US-07 | As a **host**, I want to confirm or cancel pending bookings so that the calendar is reliable. | Confirm: Pending→Confirmed. Cancel: Pending/Confirmed→Cancelled. Buttons hidden and server-side blocked for Staff. | Must | 5 |
| US-08 | As **staff**, I want to check guests in and out so that the in-house list is accurate. | Check in: Confirmed→Checked in; Check out: Checked in→Checked out; invalid jumps rejected with a message. | Must | 3 |
| US-09 | As a **host**, I want a dashboard so that I see today's business at a glance. | Tiles: total bookings, check-ins today, guests in house, occupancy % tonight, booked revenue, active properties; status counts link to filtered list; next 5 arrivals. | Must | 5 |
| US-10 | As an **operator**, I want health and version endpoints so that pipelines and Ansible can verify deployments. | `/actuator/health` → `{"status":"UP"}` without login; `/api/version` returns version and environment. | Must | 1 |
| US-11 | As a **developer**, I want every commit built and unit-tested by Jenkins so that broken code is caught early. | Jenkins polls GitHub; build + tests + archived WAR; JUnit report visible. | Must | 3 |
| US-12 | As a **developer**, I want Selenium tests to block deployment when they fail so that users never get a broken release. | ≥ 3 critical journeys automated; failure → pipeline red, deploy stages skipped, screenshot archived. | Must | 5 |
| US-13 | As an **operator**, I want the app shipped as a versioned Docker image so that every environment runs the same build. | Image tagged `1.0.<build>` and `latest`; pushed to Docker Hub; fresh container deployed and smoke-tested. | Must | 5 |
| US-14 | As an **operator**, I want Ansible to provision a node so that a new server is ready in one command. | Packages, user, folders, env file, Docker, container; second run `changed=0`. | Must | 5 |
| US-15 | As an **operator**, I want automatic rollback so that a bad release doesn't cause downtime. | Failed health check restores the previous version; `rollback.yml` switches to the previous release on demand. | Should | 3 |

**Total:** 54 story points.

## 2. Product backlog (ordered)

| Rank | Item | Type | Story | Sprint | Status |
|---|---|---|---|---|---|
| 1 | Problem definition & scope | Doc | – | S1 | Done |
| 2 | Backlog, board, DoD, workflow diagram | Doc | – | S1 | Done |
| 3 | SRS, architecture, data model, local setup | Doc | – | S1 | Done |
| 4 | GitHub repo, README, templates, branch policy | Setup | – | S1 | Done |
| 5 | Login & roles | Feature | US-01 | S2 | Done |
| 6 | Property management | Feature | US-02 | S2 | Done |
| 7 | Create / view / update booking | Feature | US-03/04/05 | S2 | Done |
| 8 | Search bookings | Feature | US-06 | S2 | Done |
| 9 | Status workflow + dashboard + API | Feature | US-07/08/09/10 | S3 | Done |
| 10 | Jenkins CI job | DevOps | US-11 | S3 | Done |
| 11 | Jenkinsfile + Tomcat deploy | DevOps | US-11 | S3 | Done |
| 12 | Selenium suite + Jenkins quality gate | Test | US-12 | S4 | Done |
| 13 | Dockerfile + Jenkins-Docker CD | DevOps | US-13 | S4 | Done |
| 14 | Ansible configure, deploy, rollback | DevOps | US-14/15 | S5 | Done |
| 15 | Final release, report, viva | Release | – | S5 | Done |

## 3. Kanban task board (15 tasks)

| To do | In progress | In review | Done |
|---|---|---|---|
| *(empty at release)* | | | T1 Problem definition · T2 Agile plan · T3 Architecture · T4 Repo init · T5 Feature 1 · T6 MVP complete · T7 Jenkins CI · T8 Jenkinsfile · T9 Selenium · T10 CI testing · T11 Docker · T12 Docker CD · T13 Ansible config · T14 Provisioning & rollback · T15 Final release |

WIP limit for **In progress** = 2. Labels: `feature`, `devops`, `test`, `docs`, `bug`. Each card links to its issue, branch and pull request.

## 4. Sprint plan

| Sprint (1 week each) | Goal | Tasks | Stories | Demo / output |
|---|---|---|---|---|
| **S1 – Plan & setup** | Scope frozen, repo ready | T1–T4 | – | Docs, diagrams, GitHub repo |
| **S2 – Core booking** | Bookings can be created, edited and searched | T5, start T6 | US-01–06 | Feature 1 PR merged |
| **S3 – Workflow & CI** | Full MVP + automated build | T6–T8 | US-07–11 | Tag v1.0.0; Jenkins deploys to Tomcat |
| **S4 – Quality & containers** | Tests gate releases; app in Docker | T9–T12 | US-12–13 | Red/green pipeline, image on Docker Hub |
| **S5 – Operate & release** | Automated provisioning, rollback, final docs | T13–T15 | US-14–15 | End-to-end demo, report, viva |

Ceremonies (solo-adapted): sprint planning (Mon, 20 min), daily self-standup note in the issue, review and retrospective (Sun, 20 min, notes in `docs/`).

## 5. Definition of Done

A backlog item is **Done** only when:

1. Code is on a `feature/*` branch with meaningful commit messages (`feat:`, `fix:`, `docs:`, `ci:`, `test:`).
2. A pull request into `develop` is reviewed (self-review checklist in the PR template) and merged.
3. The Maven build passes and **all unit tests are green**.
4. New behaviour has a unit test, and user-facing flows have a Selenium test.
5. The Jenkins pipeline is green, including the Selenium quality gate.
6. The app is deployable to Tomcat **and** as a Docker container, and `/actuator/health` is `UP`.
7. README / docs are updated, and the issue is closed with a link to the PR.
8. Acceptance criteria are demonstrated to the "host" (guide) or recorded as a screenshot.

## 6. DevOps lifecycle (development → operations)

![DevOps lifecycle](diagrams/devops-lifecycle.png)

| Phase | Tool(s) used in this project | Output |
|---|---|---|
| Plan | GitHub Issues, Projects (Kanban) | Backlog, sprint board |
| Code | Git, GitHub, IntelliJ/VS Code | Feature branches, PRs, tags |
| Build | Maven, Jenkins | `homestay.war` artefact |
| Test | JUnit 5, MockMvc, Selenium WebDriver | Surefire/Failsafe reports |
| Release | Git tags, Docker image tags | `v1.0.0`, `repo:1.0.<build>` |
| Deploy | Jenkins → Tomcat 10.1, Docker | Running app on :8080/:8085 |
| Operate | Ansible playbooks | Provisioned node, rollback |
| Monitor | Spring Boot Actuator, Docker logs, smoke tests | Health status, logs |

Pipeline detail:

![CI/CD pipeline](diagrams/cicd-pipeline.png)

---
*Deliverable checklist:* ✅ Backlog ✅ Task board ✅ Sprint plan ✅ Definition of Done ✅ DevOps workflow diagram
