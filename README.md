# 🏡 Homestay Host Management Platform

A containerized web platform for homestay hosts and staff to manage properties and guest bookings, built with a complete DevOps toolchain (Git, Jenkins, Selenium, Docker, Ansible).

> DevOps mini project · Shruti Tambade · Roll No. 23102A0072 · BE Computer Engineering (Div A) · 2026-27

## Status

MVP complete: bookings, search, role-based workflow and dashboard.

## Run locally

```powershell
mvn clean package
java -jar target\\homestay.war
```

Open http://localhost:8080 – demo users: `host/host123`, `staff/staff123`, `admin/admin123`.

## Documentation

- [Task 1 – Problem definition](docs/01-problem-definition.md)
- [Task 2 – Agile plan](docs/02-agile-plan.md)
- [Task 3 – Requirements & architecture](docs/03-requirements-architecture.md)
- [Branch policy](CONTRIBUTING.md)
