# Task 3 – Requirements, Architecture and Technology Setup

**Project:** Containerized Homestay Host Management Platform · Shruti Tambade (23102A0072)

---

## 1. SRS summary

### 1.1 Purpose
A web application for homestay hosts and staff to manage properties and guest bookings through a role-based status workflow, with a summary dashboard. It is delivered through a CI/CD pipeline and runs in Tomcat or Docker.

### 1.2 Functional requirements

| ID | Requirement |
|---|---|
| FR-01 | The system shall authenticate users (form login) and assign one role: ADMIN, HOST or STAFF. |
| FR-02 | Host/Admin shall create and edit properties (name, location, rooms, price per night, active flag). All roles can list properties. |
| FR-03 | Any user shall create a booking: guest name, 10-digit phone, optional email, property, check-in, check-out, guests (1–20), notes. |
| FR-04 | The system shall reject a check-out date that is not after check-in, and show field-level error messages. |
| FR-05 | The system shall generate a booking reference `HS-<5 digits>` and compute total = nights × price per night. |
| FR-06 | Users shall view booking details including audit fields (created by, last updated by). |
| FR-07 | Users shall update a booking only while it is PENDING or CONFIRMED; the amount is recalculated. |
| FR-08 | Users shall search bookings by partial guest name / phone / reference, filtered by status and property. |
| FR-09 | The system shall enforce the status workflow and role permissions (table in §4). |
| FR-10 | The dashboard shall show total bookings, check-ins today, guests in house, occupancy %, booked revenue, active properties, counts per status and the next 5 arrivals. |
| FR-11 | The system shall expose `/actuator/health`, `/api/version` (public) and `/api/bookings`, `/api/dashboard` (authenticated). |

### 1.3 Non-functional requirements

| ID | Category | Requirement |
|---|---|---|
| NFR-01 | Performance | Pages load in < 2 s with 1,000 bookings on a laptop. |
| NFR-02 | Security | Passwords hashed (BCrypt); CSRF protection on all forms; role checks enforced on the server, not only hidden buttons. |
| NFR-03 | Availability | Container restarts automatically (`unless-stopped`); health check every 15 s. |
| NFR-04 | Portability | Same artefact runs on Windows/Linux, Tomcat 10.1 and Docker. |
| NFR-05 | Maintainability | Layered code (controller / service / repository); ≥ 20 automated tests. |
| NFR-06 | Deployability | One-command deploy (Jenkins build or Ansible playbook); rollback < 2 min. |
| NFR-07 | Usability | Responsive layout; clear status colour badges; booking created in < 1 min. |

## 2. Use-case diagram

![Use-case diagram](diagrams/use-case.png)

| Use case | Host | Staff | Admin |
|---|:-:|:-:|:-:|
| Log in / out, view dashboard | ✔ | ✔ | ✔ |
| Create / view / update / search booking | ✔ | ✔ | ✔ |
| Check guest in / out | ✔ | ✔ | ✔ |
| Confirm / cancel booking | ✔ | ✖ | ✔ |
| Add / edit property | ✔ | ✖ | ✔ |

## 3. Architecture

![Architecture](diagrams/architecture.png)

**Style:** layered monolith (MVC). It's the right size for a 15-task MVP: one deployable artefact, so CI/CD and containers stay simple.

| Layer | Package | Responsibility |
|---|---|---|
| Security | `config.SecurityConfig` | Form login, HTTP Basic for API, URL rules, in-memory users |
| Web | `web.*Controller`, `templates/*.html` | Thymeleaf pages, form validation, REST API |
| Service | `service.BookingService`, `PropertyService` | Business rules, workflow, amount calculation, dashboard |
| Domain | `model.*` | Entities `Property`, `Booking`; enums `BookingStatus`, `BookingAction`, `Role` |
| Persistence | `repository.*` | Spring Data JPA queries (search, counts, sums) |
| Ops | Actuator, `/api/version` | Health and version for pipelines/Ansible |

### Status workflow

![Status workflow](diagrams/status-workflow.png)

## 4. Data model

![ER diagram](diagrams/er-diagram.png)

**Status / role rules**

| Action | From | To | Roles |
|---|---|---|---|
| CONFIRM | PENDING | CONFIRMED | HOST, ADMIN |
| CHECK_IN | CONFIRMED | CHECKED_IN | STAFF, HOST, ADMIN |
| CHECK_OUT | CHECKED_IN | CHECKED_OUT | STAFF, HOST, ADMIN |
| CANCEL | PENDING, CONFIRMED | CANCELLED | HOST, ADMIN |

## 5. Endpoint / API list

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET | `/login` | public | Login page |
| POST | `/login`, `/logout` | public / user | Sign in / out |
| GET | `/` | user | Dashboard |
| GET | `/bookings?q=&status=&propertyId=` | user | List + search |
| GET | `/bookings/new` | user | New booking form |
| POST | `/bookings` | user | Create booking |
| GET | `/bookings/{id}` | user | Booking details + allowed actions |
| GET | `/bookings/{id}/edit` | user | Edit form (Pending/Confirmed only) |
| POST | `/bookings/{id}` | user | Update booking |
| POST | `/bookings/{id}/status?action=CONFIRM\|CHECK_IN\|CHECK_OUT\|CANCEL` | role-based | Workflow transition |
| GET | `/properties` | user | Property list |
| GET/POST | `/properties/new`, `/properties` | HOST, ADMIN | Add property |
| GET/POST | `/properties/{id}/edit`, `/properties/{id}` | HOST, ADMIN | Edit property |
| GET | `/api/version` | public | `{"application","version","environment"}` |
| GET | `/actuator/health` | public | `{"status":"UP"}` |
| GET | `/api/bookings?q=&status=&propertyId=` | Basic auth | Bookings as JSON |
| GET | `/api/bookings/{id}` | Basic auth | One booking as JSON |
| GET | `/api/dashboard` | Basic auth | Dashboard numbers as JSON |

Example: `curl -u host:host123 http://localhost:8080/api/bookings?status=PENDING`

## 6. Technology stack

| Concern | Choice | Why |
|---|---|---|
| Language / runtime | Java 17 (LTS) | Required by Spring Boot 3; supported by Jenkins, Tomcat 10.1 |
| Framework | Spring Boot 3.3 (Web, Security, Data JPA, Validation, Actuator) | Fast MVP, health checks built in |
| UI | Thymeleaf + plain CSS | Server-rendered, no extra build tool, easy for Selenium |
| Database | H2 (file mode; in-memory for tests) | Zero install; Docker volume for persistence |
| Build tool | **Maven** 3.9 | Required; Surefire (unit) + Failsafe (Selenium) |
| Packaging | WAR (`homestay.war`) | Deploys to **Tomcat 10.1** *and* runs with `java -jar` |
| Web server | **Apache Tomcat 10.1** | Deployment target for Task 8 |
| Tests | JUnit 5, AssertJ, Spring MockMvc, **Selenium 4** | Unit, web and UI levels |
| VCS | Git + GitHub | Branches, PRs, tags, issues |
| CI/CD | **Jenkins** (declarative Jenkinsfile) | Pipeline as code |
| Containers | **Docker** (multi-stage), Docker Hub | Versioned, portable releases |
| Config mgmt | **Ansible** (roles, `community.docker`) | Agentless, YAML, idempotent |

## 7. Local development setup (Windows)

| Tool | Version | Check command |
|---|---|---|
| JDK (Temurin) | 17 | `java -version` |
| Maven | 3.9.x | `mvn -v` |
| Git | 2.4x | `git --version` |
| Chrome or Edge | latest | (for Selenium) |
| IDE | IntelliJ IDEA Community / VS Code | – |

```powershell
git clone https://github.com/<your-user>/homestay-host-platform.git
cd homestay-host-platform
mvn clean package          # builds target\homestay.war and runs unit tests
java -jar target\homestay.war
# open http://localhost:8080  → log in as host / host123
```

Demo accounts: `host/host123`, `staff/staff123`, `admin/admin123`.
Data is stored in `%USERPROFILE%\homestay-data\` (delete that folder to reset the demo data).

**Project structure**

```
homestay-host-platform/
├── pom.xml                      Maven build (WAR, selenium profile)
├── Jenkinsfile                  Final CI/CD pipeline
├── jenkins/Jenkinsfile.tomcat   Task 8 pipeline
├── Dockerfile, docker-compose.yml
├── ansible/                     Inventory, roles, playbooks
├── src/main/java/com/homestay/platform/
│   ├── config/  model/  repository/  service/  web/
├── src/main/resources/templates/  static/css/
├── src/test/java/.../            Unit + MockMvc tests
│   └── selenium/                Selenium WebDriver suite (*IT)
└── docs/                        Task documents, diagrams, lab guide
```

---
*Deliverable checklist:* ✅ SRS summary ✅ Use-case diagram ✅ Architecture diagram ✅ Data model / API list ✅ Working local setup
