# Stage 1: build the jar with Gradle
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY . .
RUN sh ./gradlew bootJar --no-daemon

# Stage 2: run the jar on a small JRE image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
