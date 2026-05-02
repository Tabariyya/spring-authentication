# syntax=docker/dockerfile:1
# Build context must be the project root:
#   DOCKER_BUILDKIT=1 docker build -f examples/sb2/Dockerfile -t auth-example-sb2 .

# ── Build stage ───────────────────────────────────────────────────────────────
FROM maven:3.8.6-openjdk-8 AS build

# ── Step 1: install the library ───────────────────────────────────────────────
WORKDIR /lib

# Cache library dependency resolution separately from source so remote deps
# are not re-downloaded on every source change.
# NOTE: com.tabariyya:utils:1.0.0 must already be in the host's ~/.m2 cache.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -q

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn install -DskipTests -q

# ── Step 2: build the example application ────────────────────────────────────
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:8-jre

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
