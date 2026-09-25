plugins {
    java
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.epam.java.specialization"
version = "2.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenLocal()
    mavenCentral()
}

extra["testcontainersVersion"] = "1.21.3"

dependencies {

    // Shared Library
    implementation("com.epam.java.specialization:gym-crm-common:2.0.0")

    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    implementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring30x:4.11.0")

    // Distributed Tracing & Observability
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp") // Виправлено синтаксис (додано закриваючу дужку)

    // Structured JSON Logging
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Metrics & Monitoring
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // Swagger / OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // JWT (JJWT)
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.5.5.Final")

    // Lombok & Annotation Processors
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.awaitility:awaitility:4.2.0")

    // Spring Boot Testcontainers integration
    testImplementation("org.springframework.boot:spring-boot-testcontainers")

    // Testcontainers
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:kafka")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Test Lombok / MapStruct
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    testImplementation(platform("io.cucumber:cucumber-bom:7.18.1"))
    testImplementation("io.cucumber:cucumber-java")
    testImplementation("io.cucumber:cucumber-spring")
    testImplementation("io.cucumber:cucumber-junit-platform-engine")
    testImplementation("org.junit.platform:junit-platform-suite")
}

dependencyManagement {
    imports {
        mavenBom(
            "org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}"
        )
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"

    options.compilerArgs.addAll(
        listOf(
            "-parameters",
            "-Amapstruct.defaultComponentModel=spring",
            "-Amapstruct.unmappedTargetPolicy=IGNORE"
        )
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("api.version", "1.44")
    systemProperty("docker.api.version", "1.44")
    environment("DOCKER_API_VERSION", "1.44")
    environment("TESTCONTAINERS_RYUK_DISABLED", "true")
}

tasks.register<Test>("cucumberTest") {
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform()
    filter {
        includeTestsMatching("*CucumberTestRunner*")
    }
    systemProperty("api.version", "1.44")
    systemProperty("docker.api.version", "1.44")
    systemProperty("cucumber.filter.tags", System.getProperty("cucumber.filter.tags", ""))
    environment("DOCKER_API_VERSION", "1.44")
    environment("TESTCONTAINERS_RYUK_DISABLED", "true")
}