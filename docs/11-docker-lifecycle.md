# Task 11 – Docker Image and Container Lifecycle

## 1. Dockerfile design

| Choice | Reason |
|---|---|
| **Multi-stage**: `maven:3.9-eclipse-temurin-17` → `eclipse-temurin:17-jre` | Build tools stay out of the runtime image (smaller, safer) |
| `COPY pom.xml` + `dependency:go-offline` before `COPY src` | Dependency layer is cached, so code changes rebuild in seconds |
| `ARG APP_ENV` | Environment label baked in (shown at `/api/version`) |
| Non-root user `homestay` | Container doesn't run as root |
| `VOLUME /app/data` + `HOMESTAY_DATA_DIR` | H2 data survives container removal |
| `EXPOSE 8080` | Documents the container port |
| `HEALTHCHECK` on `/actuator/health` | `docker ps` shows `healthy`; used by the CD smoke test |
| `JAVA_OPTS` env | Memory limits can be tuned without a rebuild |
| `.dockerignore` | Keeps `target/`, `.git/`, docs and Ansible out of the build context |

## 2. Lifecycle commands

```powershell
# Build and tag
docker build -t homestay-host-platform:1.0.0 -t homestay-host-platform:latest .
docker images homestay-host-platform
docker history homestay-host-platform:1.0.0

# Run (detached, port mapping host 8085 → container 8080, named volume)
docker run -d --name homestay-app -p 8085:8080 -v homestay-data:/app/data homestay-host-platform:1.0.0
docker ps
docker port homestay-app
curl.exe http://localhost:8085/actuator/health

# Observe
docker logs -f homestay-app            # Ctrl+C to stop following
docker inspect homestay-app
docker stats --no-stream homestay-app
docker exec -it homestay-app sh        # look inside, then type exit

# Stop / restart / remove
docker stop homestay-app
docker ps -a
docker start homestay-app
docker restart homestay-app
docker rm -f homestay-app
docker rmi homestay-host-platform:1.0.0

# Compose alternative
docker compose up -d --build
docker compose logs -f
docker compose down
```

## 3. Container states

```
 docker create/run        docker stop           docker start
 ───────────────▶ RUNNING ───────────▶ EXITED ───────────▶ RUNNING
                    │  ▲                  │
       docker pause │  │ docker unpause   │ docker rm
                    ▼  │                  ▼
                  PAUSED               REMOVED  (volume homestay-data is kept)
```

## 4. Evidence

Run `scripts\docker-lifecycle.ps1`. It executes every step above (except removal) and writes the full command log to `evidence\docker-lifecycle-log.txt`.
