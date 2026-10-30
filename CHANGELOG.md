# Changelog

## v1.1.0 – Final release (Task 15)
- **CI/CD:** the full Jenkins pipeline runs build, unit tests, the Selenium quality gate, Tomcat deploy, Docker build, a push to the local registry (`localhost:5000`), container deploy and a smoke test (`homestay-cicd #8`).
- **Docker:** base images come from `mirror.gcr.io`, and the lifecycle script logs to `evidence/` (#25, #26, #27).
- **Ansible:** a WSL2 runner covers configure, idempotent reruns, a versioned deploy, automatic rollback on a failed release and a manual `rollback.yml` (#28).
- **Pipeline fixes:** the helper was renamed to `runCmd`, `waitUntil` was moved inside `script` blocks, and Selenium click and staleness handling was hardened (#20–#24).
- **Docs:** the project report now has real evidence for Tasks 10–14.

## v1.0.0 – MVP (Task 6)
- Booking CRUD with search, a role-based status workflow (host/staff/admin), property management and a dashboard.
- 21 unit/MockMvc tests and 9 Selenium tests in 5 journeys.
