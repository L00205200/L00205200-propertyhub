# syntax=docker/dockerfile:1.7
#checkov:skip=CKV_DOCKER_2:Distroless has no shell/curl; liveness/readiness are Kubernetes probes on /actuator/health
# ---------- Stage 1: build with Maven (cached dependencies) ----------
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /src

# Copy the POM first so dependency downloads are cached when only source changes
COPY pom.xml spotbugs-exclude.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests package \
 && java -Djarmode=tools -jar target/propertyhub.jar extract --layers --launcher --destination target/extracted

# ---------- Stage 2: minimal, non-root runtime (no shell, no package manager) ----------
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app

# Spring Boot layers, least- to most-frequently changing, for better layer caching
COPY --from=build /src/target/extracted/dependencies/ ./
COPY --from=build /src/target/extracted/spring-boot-loader/ ./
COPY --from=build /src/target/extracted/snapshot-dependencies/ ./
COPY --from=build /src/target/extracted/application/ ./

ARG APP_VERSION=local
ARG GIT_COMMIT=local
ENV APP_VERSION=${APP_VERSION} \
    GIT_COMMIT=${GIT_COMMIT} \
    LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

LABEL org.opencontainers.image.title="l00205200-propertyhub" \
      org.opencontainers.image.description="PropertyHub Ireland - ATU CI/CD assignment, student L00205200" \
      org.opencontainers.image.licenses="MIT"

EXPOSE 8080
USER nonroot:nonroot
ENTRYPOINT ["/usr/bin/java", "org.springframework.boot.loader.launch.JarLauncher"]
