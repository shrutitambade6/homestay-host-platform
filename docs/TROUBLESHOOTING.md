# Troubleshooting Guide

| Symptom | Likely cause | Fix |
|---|---|---|
| `'mvn' is not recognized` | Maven not on PATH | Add `%MAVEN_HOME%\bin` to Path; open a **new** terminal |
| `release version 17 not supported` / `invalid target release` | Maven uses an older JDK | `mvn -v` must show Java 17+; set `JAVA_HOME` to JDK 17 |
| `Port 8080 was already in use` | Tomcat (or Jenkins) is running | Stop Tomcat (`shutdown.bat`) or run `java -jar target\homestay.war --server.port=8081` |
| Login page loops / CSRF error | Old cookies from another app on localhost | Use a private window or clear cookies for localhost |
| Want fresh demo data | H2 file DB keeps data | Stop the app and delete `%USERPROFILE%\homestay-data` |
| `Database may be already in use` | Two app instances share the DB | URL uses `AUTO_SERVER=TRUE`; otherwise stop the other instance |
| Tomcat shows 404 for /homestay | WAR not deployed yet or failed | Check `C:\apache-tomcat-10.1\logs\catalina.*.log`; needs **Tomcat 10.1** (Jakarta), not 9 |
| Selenium: `SessionNotCreatedException` | Browser/driver mismatch or no browser | Update Chrome, or use `-Dbrowser=edge`; Selenium Manager needs internet the first time |
| Selenium works locally, fails in Jenkins | Jenkins runs as a Windows service (no desktop) | Run Jenkins with `java -jar jenkins.war` as your user; keep `-Dheadless=true` |
| Selenium flaky on slow laptop | Startup takes longer | Close other apps; the waits are 10 s, so increase them in `BaseSeleniumIT` if needed |
| Jenkins: `Tool type "maven" does not have an install of "Maven3"` | Tool names don't match | Manage Jenkins → Tools → names exactly `JDK17` and `Maven3` |
| Jenkins: `No such DSL method 'timestamps'` | Timestamper plugin missing | Install the **Timestamper** plugin (part of the suggested plugins) |
| Jenkins: parameters not visible | First run of a parameterized Jenkinsfile | Run once, then use **Build with Parameters** |
| Jenkins: `copy` fails in Deploy to Tomcat | Wrong `TOMCAT_WEBAPPS` path | Use the real path, e.g. `C:\apache-tomcat-10.1\webapps` |
| Jenkins: `docker: command not found` / pipe error | Docker Desktop not running, or Jenkins not run as your user | Start Docker Desktop; run Jenkins from your own terminal |
| `docker login` fails in pipeline | Wrong credential | Credential ID must be `dockerhub-creds`; use an access token, not your password |
| `denied: requested access to the resource is denied` on push | `DOCKER_REPO` isn't your Docker Hub user | Set `DOCKER_REPO=<your-user>/homestay-host-platform` |
| Container `unhealthy` | App failed to start | `docker logs homestay-app`; check memory in `JAVA_OPTS` |
| Ansible: `Could not import python3-apt` | Running Ansible from a non-system Python | Use `sudo apt install ansible` in WSL (not pip in a venv) |
| Ansible: `System has not been booted with systemd` | systemd disabled in WSL | Add `[boot]\nsystemd=true` to `/etc/wsl.conf`, run `wsl --shutdown`, reopen |
| Ansible: Docker conflicts with Docker Desktop | WSL integration is on for the node distro | Turn it off for that distro, **or** set `docker_install: false` |
| Ansible: `pull access denied` | Image tag isn't on Docker Hub | Check the tag on Docker Hub; or use `app_image_archive` |
| Ansible: `Permission denied` under `/mnt/c` | World-writable Windows folder | Copy `ansible/` into `~/ansible` inside Linux |
| Git: `rejected – non-fast-forward` | Remote has newer commits | `git pull --rebase` then push |
| Git: GitHub asks for password | Passwords are disabled | `gh auth login`, or use a Personal Access Token |

## Known limitations

- Demo users are in memory (passwords can be overridden via `app.users.*-password` properties); there's no user management UI.
- H2 is a single-node embedded database; for multiple app instances use MySQL/PostgreSQL.
- Occupancy assumes one room per booking, and there is no room-level availability blocking, so overbooking is possible.
- No email/SMS notifications or payments.
- Jenkins polls GitHub (no webhook) because a laptop isn't reachable from the internet.
- The rollback restores the application only. Database schema changes are additive (`ddl-auto=update`), and there is no data rollback.

## Future enhancements

1. MySQL/PostgreSQL with Flyway migrations, and Docker Compose with a DB service.
2. Room-level availability calendar with double-booking prevention.
3. Guest self-service booking page, plus email/WhatsApp confirmations.
4. Payments and GST invoices.
5. User management with a database-backed identity store and password reset.
6. GitHub webhooks via a tunnel (ngrok) or move CI to GitHub Actions.
7. Kubernetes (Helm chart) with rolling updates and readiness/liveness probes.
8. Monitoring with Prometheus + Grafana (`/actuator/prometheus`) and centralized logs.
9. Security scanning in the pipeline (OWASP Dependency-Check, Trivy image scan, SonarQube).
10. Blue/green deployments with Ansible and a reverse proxy (Nginx).
