# Multi-stage Java 21 build for optional Compose profile `full`.
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
    && mkdir -p /app/uploads \
    && chown -R app:app /app

COPY --from=build --chown=app:app /workspace/app.jar /app/app.jar

USER app
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=10 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health >/dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
