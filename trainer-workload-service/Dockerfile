# Build context is the repository root (see docker-compose.yml):
#   docker build -f trainer-workload-service/Dockerfile -t trainer-workload-service .

# Stage 1: build the shared library and the service jar with Gradle
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY gym-crm-common gym-crm-common
RUN cd gym-crm-common && sh ./gradlew publishToMavenLocal --no-daemon
COPY trainer-workload-service trainer-workload-service
RUN cd trainer-workload-service && sh ./gradlew bootJar --no-daemon

# Stage 2: run the jar on a small JRE image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/trainer-workload-service/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
