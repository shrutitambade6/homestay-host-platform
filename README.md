# 🏡 Homestay Host Management Platform

A containerized web platform where homestay **hosts** and **staff** manage properties and guest bookings through a role-based status workflow, with a summary dashboard. The app is built, tested and shipped with a complete DevOps toolchain: **Git → Jenkins → Selenium → Docker → Ansible**.

> DevOps mini project · Shruti Tambade · Roll No. 23102A0072 · BE Computer Engineering (Div A) · 2026-27

## Features (MVP)

- Login with three roles: **Host**, **Staff**, **Admin**
- Properties: add, edit, list (Host/Admin)
- Bookings: create, view, update and **search** (name / phone / reference, status, property)
- **Role-based status workflow**: Pending → Confirmed → Checked in → Checked out, or Cancelled
- **Summary dashboard**: bookings, check-ins today, guests in house, occupancy, revenue, upcoming arrivals
- Health (`/actuator/health`) and version (`/api/version`) endpoints, plus a small JSON API

## Run locally

Prerequisites: JDK 17, Maven 3.9, Git.

```powershell
mvn clean package
java -jar target\homestay.war
```

Open http://localhost:8080 and sign in:

| User | Password | Role |
|---|---|---|
| host | host123 | Host – confirm/cancel, manage properties |
| staff | staff123 | Staff – create bookings, check in/out |
| admin | admin123 | Admin – everything |

## Tests

```powershell
mvn test                          # unit + MockMvc tests (21)
mvn verify -Pselenium             # + Selenium UI suite (headless Chrome)
mvn verify -Pselenium -Dheadless=false -Dbrowser=edge   # watch it run in Edge
```

## Docker

```powershell
docker build -t homestay-host-platform:1.0.0 .
docker run -d --name homestay-app -p 8085:8080 -v homestay-data:/app/data homestay-host-platform:1.0.0
# http://localhost:8085
```

## CI/CD

- `Jenkinsfile`: Checkout → Build → Unit tests → Package → **Selenium gate** → Tomcat deploy → Docker build/push → container deploy → smoke test
- `jenkins/Jenkinsfile.tomcat`: the simpler Task 8 pipeline (build + deploy to Tomcat)
- `ansible/`: configure a node, deploy a release with health check and automatic rollback

## Documentation

| Doc | Task |
|---|---|
| [docs/01-problem-definition.md](docs/01-problem-definition.md) | 1 |
| [docs/02-agile-plan.md](docs/02-agile-plan.md) | 2 |
| [docs/03-requirements-architecture.md](docs/03-requirements-architecture.md) | 3 |
| [CONTRIBUTING.md](CONTRIBUTING.md) – branch policy & commit rules | 4 |
| [docs/LAB-GUIDE-WINDOWS.md](docs/LAB-GUIDE-WINDOWS.md) – step-by-step for Tasks 4–15 | 4–15 |
| [docs/09-selenium-test-plan.md](docs/09-selenium-test-plan.md) | 9 |
| [docs/11-docker-lifecycle.md](docs/11-docker-lifecycle.md) | 11 |
| [docs/13-configuration-spec.md](docs/13-configuration-spec.md) | 13 |
| [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) | 15 |

## Project structure

```
src/main/java/com/homestay/platform   config · model · repository · service · web
src/main/resources/templates          Thymeleaf pages
src/test/java/.../selenium            Selenium WebDriver suite
Jenkinsfile, jenkins/                 Pipelines as code
Dockerfile, docker-compose.yml        Container build and run
ansible/                              Inventory, roles, playbooks
docs/                                 Task documents and diagrams
```
