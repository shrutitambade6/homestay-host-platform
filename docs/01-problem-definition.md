# Task 1 – Problem Definition and Scope

**Project:** Containerized Homestay Host Management Platform
**Student:** Shruti Tambade · Roll No. 23102A0072 · BE Computer Engineering (Div A)
**Academic Year:** 2026-27, ODD Semester · Subject: DevOps

---

## 1. Real-time need

Small homestays (1–5 rooms, run by a family) are booming in places like Mahabaleshwar, Lonavala, Alibaug and Coorg. Most hosts still manage bookings with:

- WhatsApp chats and phone calls,
- a paper register or an Excel sheet,
- memory ("did we confirm the Sharma family for Saturday?").

That causes **double bookings**, **missed check-ins**, **no clear view of revenue**, and confusion between the owner (host) and the caretaker (staff) about who confirmed what. Hosts don't need a big hotel PMS (Property Management System) – they need a **small, reliable, always-up web app** that the host and staff can both use.

The project also needs the app to be **delivered the DevOps way**, with version control, automated build and tests, containers, and automated server setup, so updates reach the host safely and quickly.

## 2. Problem statement

> Homestay hosts lack a simple, shared system to record guest bookings, move each booking through a clear status workflow (pending → confirmed → checked-in → checked-out / cancelled) with role-based permissions, search past and upcoming stays, and see occupancy and revenue at a glance. The solution must be built and released through an automated DevOps pipeline (Git → Jenkins → Selenium → Docker → Ansible) so that every change is tested and can be deployed or rolled back reliably.

## 3. Target users

| User | Description | Main needs |
|---|---|---|
| **Host** (owner) | Owns 1–5 properties, takes enquiries | Confirm/cancel bookings, manage properties, see revenue |
| **Staff** (caretaker / front desk) | On-site person who receives guests | Create walk-in bookings, check guests in and out |
| **Admin** | Technical owner / the developer | Full access, support, demo |
| *(Indirect)* Guest | Person staying | Correct booking, no double booking |

## 4. Existing pain points

1. Bookings scattered across WhatsApp, calls and paper.
2. No status tracking, so a booking can't be told apart as enquired, confirmed or cancelled.
3. Anyone can change anything; there's no accountability for who confirmed or checked in a guest.
4. Finding a guest's past booking means scrolling through chats.
5. No quick view of today's arrivals, guests in house, occupancy or revenue.
6. Updates to any software are manual and risky; there's no testing and no rollback.

## 5. Stakeholders

| Stakeholder | Interest / role |
|---|---|
| Homestay host | Primary user and "customer"; accepts the MVP |
| Homestay staff | Daily user at the property |
| Guests | Benefit from correct, quick service |
| Developer / DevOps engineer (student) | Builds, tests, deploys and operates the platform |
| Subject faculty / project guide | Reviews scope, tasks and the final viva |
| Hosting / infrastructure (laptop, Docker Hub, GitHub) | Runs code, images and pipelines |

## 6. Objectives

1. Build a web MVP that lets users **create, view, update and search** bookings and properties.
2. Enforce a **role-based status workflow** (Host / Staff / Admin).
3. Provide a **summary dashboard** (total bookings, today's check-ins, guests in house, occupancy, revenue, status counts, upcoming arrivals).
4. Keep all code in **Git/GitHub** with feature branches, pull requests and tagged releases.
5. Automate **build, test and deployment** with **Jenkins** (pipeline as code).
6. Use **Selenium** UI tests as a **quality gate** that blocks bad deployments.
7. Package and run the app as a **Docker container** with versioned images.
8. Configure servers and deploy with **Ansible**, with idempotency, health checks and **rollback**.

## 7. Constraints

| Type | Constraint |
|---|---|
| Time | One semester; 15 tasks |
| Team | Single student |
| Cost | Free tools only (GitHub, Jenkins, Docker Desktop, Docker Hub free tier, Ansible) |
| Hardware | Personal Windows laptop (8 GB+ RAM); Linux parts run in WSL2 Ubuntu |
| Technology | Java 17, Spring Boot 3, Maven, Tomcat 10.1 / Docker, Selenium, Ansible |
| Data | Embedded H2 database (no separate DB server) for the MVP |
| Security | Demo users with in-memory credentials; no payments or personal ID storage |
| Scope | No online guest portal, payments, channel manager or mobile app in the MVP |

## 8. Measurable success criteria

| # | Criterion | Target / how measured |
|---|---|---|
| S1 | Core features work | All 15 MVP backlog items accepted in the demo |
| S2 | Role rules enforced | Staff **cannot** confirm/cancel or add properties (automated test) |
| S3 | Automated tests | ≥ 20 unit/integration tests + 5 Selenium journeys, all green |
| S4 | CI time | Commit → build → tests finishes in **< 10 min** on Jenkins |
| S5 | Quality gate | A deliberately broken commit **stops** deployment (pipeline evidence) |
| S6 | Container | App runs from a versioned image; health endpoint returns `UP` within 60 s |
| S7 | Provisioning | Ansible configures a clean node; second run shows `changed=0` |
| S8 | Recovery | A failed release is rolled back to the previous version in **< 2 min** |
| S9 | Usability | Host can create a booking in **< 1 minute** |

## 9. Approved MVP scope (15 tasks, frozen)

**In scope:**

| ID | MVP item |
|---|---|
| M1 | Login / logout with three roles (Host, Staff, Admin) |
| M2 | Add / edit / list properties (Host & Admin only) |
| M3 | Create booking with validation (10-digit phone, check-out after check-in) |
| M4 | Automatic booking reference (HS-00001) and amount = nights × price |
| M5 | View booking details |
| M6 | Update booking (only while Pending / Confirmed) |
| M7 | Search bookings by guest name / phone / reference, status and property |
| M8 | Status workflow: Confirm, Check in, Check out, Cancel with role checks |
| M9 | Audit fields: created by / last updated by |
| M10 | Summary dashboard: tiles, status counts, upcoming arrivals |
| M11 | Health endpoint and version endpoint for monitoring |
| M12 | Read-only JSON API (/api/bookings, /api/dashboard) |
| M13 | Demo data seeding for first run |
| M14 | Automated tests (unit, MVC, Selenium) |
| M15 | Packaging: WAR for Tomcat + Docker image |

**Out of scope (future):** online guest booking, payments, invoices/GST, email/SMS notifications, room-level calendar and availability blocking, multi-host SaaS, MySQL/PostgreSQL, mobile app.

---
*Deliverable checklist:* ✅ Problem statement ✅ Stakeholder list ✅ Objectives ✅ Constraints ✅ Success criteria ✅ Approved MVP scope
