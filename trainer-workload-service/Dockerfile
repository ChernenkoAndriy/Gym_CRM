FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/trainer-workload-service-*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]