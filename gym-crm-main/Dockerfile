# Build context is the repository root (see docker-compose.yml):
#   docker build -f gym-crm-main/Dockerfile -t gym-crm-main .

# Stage 1: build the shared library and the service jar with Gradle
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY gym-crm-common gym-crm-common
RUN cd gym-crm-common && sh ./gradlew publishToMavenLocal --no-daemon
COPY gym-crm-main gym-crm-main
RUN cd gym-crm-main && sh ./gradlew bootJar --no-daemon

# Stage 2: run the jar on a small JRE image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/gym-crm-main/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
