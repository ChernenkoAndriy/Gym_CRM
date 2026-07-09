plugins {
    java
}

group = "com.epam.java.specialization"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Spring Core & ORM
    implementation("org.springframework:spring-context:6.1.10")
    implementation("org.springframework:spring-orm:6.1.10")
    implementation("org.springframework:spring-tx:6.1.10")

    // Hibernate & Validation
    implementation("org.hibernate.orm:hibernate-core:6.5.2.Final")
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    implementation("org.glassfish:jakarta.el:4.0.2")

    // Database Driver
    implementation("org.postgresql:postgresql:42.7.3")

    // Jackson (JSON parsing)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.6")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")

    // Configuration YAML Support
    implementation("org.yaml:snakeyaml:2.2")

    // --- TESTING LAYERS ---
    // Базовий агрегатор JUnit 5 (включає api та params)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")

    // Рушій виконання та ЛАУНЧЕР для Gradle Executor (виправляє твою помилку)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")

    // Spring Test & Mockito
    testImplementation("org.springframework:spring-test:6.1.10")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")

    testImplementation("org.testcontainers:testcontainers:1.19.8")
    testImplementation("org.testcontainers:postgresql:1.21.4")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
}

tasks.test {
    useJUnitPlatform()
}