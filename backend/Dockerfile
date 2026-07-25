# Multi-stage Java 21 build.
# Preferred (repo root, matches parent compose):
#   docker build -f backend/Dockerfile .
# Backend-local (from this directory):
#   docker build -f Dockerfile ..
# Integration tests (Testcontainers) run on the host via `mvnw clean test`, not in-image.

FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY backend/pom.xml backend/mvnw backend/
COPY backend/.mvn backend/.mvn
RUN chmod +x backend/mvnw \
    && ./backend/mvnw -f backend/pom.xml -B -q dependency:go-offline

COPY backend/src backend/src
RUN ./backend/mvnw -f backend/pom.xml -B -DskipTests package \
    && cp backend/target/ExoticStamp-*.jar /workspace/app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app \
    && mkdir -p /tmp/app \
    && chown -R app:app /app /tmp/app

COPY --from=build --chown=app:app /workspace/app.jar /app/app.jar

USER app
EXPOSE 8080

# Prefer readiness over aggregate health (S3 probe is a separate group).
# Production media lives in S3 — do not mount or use /app/uploads in prod.
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=10 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health/readiness >/dev/null || exit 1

STOPSIGNAL SIGTERM

# JAVA_TOOL_OPTIONS is honored by the JVM.
# MaxRAMPercentage=75 targets typical Lightsail 2–4GB plans; override per instance if needed.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
