# syntax=docker/dockerfile:1
# Run from examples/sb4/:
#   DOCKER_BUILDKIT=1 docker build \
#     --build-context library=../.. \
#     --build-context m2local=$HOME/.m2 \
#     -t auth-example-sb4 .

# ── Build stage ───────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build

# ── Seed the Maven cache with private artifacts ───────────────────────────────
RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=bind,from=m2local,source=repository/com/tabariyya,target=/tmp/tabariyya \
    mkdir -p /root/.m2/repository/com/tabariyya && \
    cp -rf /tmp/tabariyya/. /root/.m2/repository/com/tabariyya/

# ── Step 1: build and install the library ────────────────────────────────────
WORKDIR /lib

COPY --from=library pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -q

COPY --from=library src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn install -DskipTests -q

# ── Step 2: build the example application ────────────────────────────────────
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
