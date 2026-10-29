# ---------------------------------------------------------------------------
# Homestay Host Management Platform - container image (Task 11)
# Multi-stage: stage 1 builds the WAR with Maven, stage 2 is a small JRE image.
#
#   docker build -t homestay-host-platform:1.0.0 .
#   docker run -d --name homestay-app -p 8085:8080 -v homestay-data:/app/data homestay-host-platform:1.0.0
# ---------------------------------------------------------------------------

# Base images come from mirror.gcr.io (Google's cache of Docker Hub official images):
# same images, but no auth.docker.io token request, which fails on some ISP IPv6 routes.

# ---------- Stage 1: build ----------
FROM mirror.gcr.io/library/maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
# Copy the POM first so dependency downloads are cached between builds
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
ARG APP_ENV=docker
RUN mvn -B -q package -DskipTests -Dapp.env=${APP_ENV}

# ---------- Stage 2: runtime ----------
FROM mirror.gcr.io/library/eclipse-temurin:17-jre

LABEL org.opencontainers.image.title="homestay-host-platform" \
      org.opencontainers.image.description="Containerized Homestay Host Management Platform" \
      org.opencontainers.image.authors="Shruti Tambade (23102A0072)"

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --system homestay \
 && useradd --system --gid homestay --home-dir /app homestay \
 && mkdir -p /app/data \
 && chown -R homestay:homestay /app

WORKDIR /app
COPY --from=build --chown=homestay:homestay /src/target/homestay.war /app/app.war

ENV HOMESTAY_DATA_DIR=/app/data \
    JAVA_OPTS="-Xms128m -Xmx384m"

USER homestay
EXPOSE 8080
VOLUME ["/app/data"]

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.war"]
