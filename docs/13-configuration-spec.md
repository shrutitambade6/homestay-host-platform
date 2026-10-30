# Task 13 – Configuration Specification (Ansible)

Target node: Ubuntu 22.04/24.04 (WSL2 distro or VM). Tool: **Ansible** (agentless, push over SSH or `local` connection).

## 1. Server prerequisites

| Category | Item | Value | Ansible task |
|---|---|---|---|
| Packages | Utilities | `curl`, `ca-certificates`, `tree`, `jq`, `python3-pip` | `common` → `apt` |
| Packages | Container runtime | `docker.io`, `python3-docker` | `docker` → `apt` |
| Users | Service group | `homestay` (system) | `common` → `group` |
| Users | Service user | `homestay`, no login shell, home `/opt/homestay`, member of `docker` | `common` / `docker` → `user` |
| Folders | Base | `/opt/homestay` (owner homestay, 0755) | `common` → `file` |
| Folders | Config | `/opt/homestay/config` | 〃 |
| Folders | Release history | `/opt/homestay/release` (`current`, `previous`) | 〃 |
| Folders | Backups | `/opt/homestay/backups` | 〃 |
| Folders | Logs | `/var/log/homestay` | 〃 |
| Files | Environment file | `/opt/homestay/config/homestay.env` (0640) | `common` → `template` |
| Files | Node info | `/opt/homestay/NODE-INFO.txt` | `common` → `template` |
| Services | Docker | started + enabled at boot | `docker` → `service` |
| Ports | Application | host **8090** → container 8080/tcp | `homestay_app` → `docker_container` |
| Ports | Firewall | `ufw allow 8090/tcp` (when `firewall_enabled: true`) | `common` → `ufw` |
| Data | Persistent volume | Docker volume `homestay-data` → `/app/data` | `homestay_app` |
| Health | Endpoint | `GET /actuator/health` = 200 / `UP` | `homestay_app` → `uri` |

## 2. Layout

```
ansible/
├── ansible.cfg                         inventory, roles path, log file
├── requirements.yml                    community.docker collection
├── inventory/
│   ├── hosts.ini                       [homestay_nodes]
│   └── group_vars/homestay_nodes.yml   all variables above
├── roles/
│   ├── common/        packages, user, folders, env file, firewall
│   ├── docker/        engine, service, docker group
│   └── homestay_app/  pull image, run container, health check, release record, auto-rollback
└── playbooks/
    ├── configure.yml   Task 13: common + docker
    ├── site.yml        Task 14: full provisioning + deploy
    ├── deploy.yml      deploy a version (with auto-rollback)
    ├── rollback.yml    switch back to the previous release
    └── healthcheck.yml status report
```

## 3. Idempotency design

- Only declarative modules are used (`apt state=present`, `file state=directory`, `template`, `docker_container state=started`), so a second run changes nothing.
- The release record is only rewritten when the version changes.
- Read-only checks use `changed_when: false`.

## 4. Rollback design

1. Before deploying, the role reads `release/current` (last good version).
2. The new container is started and `/actuator/health` is polled (24 × 5 s).
3. **Success:** `previous ← current`, `current ← new`.
4. **Failure** (image missing or unhealthy): the `rescue:` block recreates the container from `current`, re-checks health, and fails the play with a "Recovered" message.
5. `rollback.yml` redeploys `previous` on demand, and the record swaps.

## 5. Verified behaviour

The playbooks passed syntax checks and `ansible-lint` (production profile). They were then run against a Docker host with stand-in images:

| Run | Result |
|---|---|
| `site.yml -e app_version=1.0.1` (clean node) | changed > 0, container healthy |
| same command again | **changed=0** (idempotent) |
| `deploy.yml -e app_version=1.0.2` | upgraded; current=1.0.2, previous=1.0.1 |
| `deploy.yml -e app_version=1.0.3` (unhealthy image) | "Rolling back to 1.0.2" → "Recovered: 1.0.2 is running again" |
| `rollback.yml` | container back on 1.0.1; current=1.0.1, previous=1.0.2 |
| `healthcheck.yml` | Health UP, image, state running, release |
