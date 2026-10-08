# * Stage 1: Build the application
FROM maven:3.10.0-eclipse-temurin-25-alpine AS builder

WORKDIR /app

# * Resolve dependencies first so this layer is cached until pom.xml changes
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline

COPY src ./src

# * Skip tests in the image build: run them in CI instead
# * (tests needing a DB or env vars would break the build)
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -DskipTests

# * Stage 2: Create the runtime image
FROM eclipse-temurin:25-jre-alpine AS runtime

WORKDIR /app

# * Run as a non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# ! target/*.jar must match exactly one file
COPY --from=builder /app/target/*.jar app.jar

USER spring

# * Documentation only: server.port must be 3000 in application.properties
EXPOSE 3000

# * Optional healthcheck (needs spring-boot-starter-actuator; wget ships with BusyBox)
# ? HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
# ?   CMD wget -qO- http://localhost:3000/actuator/health || exit 1

# * Exec form keeps java as PID 1 so SIGTERM triggers graceful shutdown
# * Secrets are NOT baked in: pass them at runtime (--env-file / compose / secret store)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]