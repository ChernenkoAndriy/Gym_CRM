plugins {
    java
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
}

group = "com.epam.java.specialization"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Стартери (замінюють ручну конфігурацію Spring Context, ORM, TX)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Database Driver
    implementation("org.postgresql:postgresql:42.7.3")

    // Jackson (JSON parsing) — версії тепер контролюються Spring Boot
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")

    implementation("org.liquibase:liquibase-core")

    // --- TESTING LAYERS ---
    // Базовий стартер для тестування (включає JUnit 5, Mockito, Spring Test)
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Testcontainers
    testImplementation("org.testcontainers:testcontainers:1.20.0")
    testImplementation("org.testcontainers:postgresql:1.20.0")
    testImplementation("org.testcontainers:junit-jupiter:1.20.0")

    // JPA Static Metamodel Generator (зберігаємо для безпечних Criteria запитів)
    annotationProcessor("org.hibernate.orm:hibernate-jpamodelgen:6.5.2.Final")
    testAnnotationProcessor("org.hibernate.orm:hibernate-jpamodelgen:6.5.2.Final")
}

tasks.test {
    useJUnitPlatform()
}