plugins {
    java
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.epam.java.specialization"
version = "0.0.1-SNAPSHOT"
description = "integration-tests"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenLocal()
    mavenCentral()
}

val cucumberVersion = "7.20.1"
val restAssuredVersion = "5.5.0"
val awaitilityVersion = "4.2.2"
val testcontainersVersion = "1.20.4"

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
    }
}

dependencies {
    implementation("com.epam.java.specialization:gym-crm-common:2.0.0")

    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Testcontainers
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")

    // Cucumber
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-spring:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")

    // JUnit Platform Suite API & Engine
    testImplementation("org.junit.platform:junit-platform-suite:1.10.3")
    testRuntimeOnly("org.junit.platform:junit-platform-suite-engine:1.10.3")

    // REST клієнт та асинхронні перевірки
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("org.awaitility:awaitility:$awaitilityVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()

    systemProperty("api.version", "1.44")
    systemProperty("docker.api.version", "1.44")
    systemProperty("cucumber.filter.tags", System.getProperty("cucumber.filter.tags", ""))
    systemProperty("cucumber.junit-platform.naming-strategy", "long")

    environment("DOCKER_API_VERSION", "1.44")
    environment("TESTCONTAINERS_RYUK_DISABLED", "true")

    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

val javaHome = System.getProperty("java.home")

tasks.register<Exec>("buildCrmImage") {
    workingDir = file("../gym-crm-main")
    environment("JAVA_HOME", javaHome)
    environment("PATH", "$javaHome/bin;" + (System.getenv("PATH") ?: ""))
    commandLine = if (System.getProperty("os.name").lowercase().contains("windows")) {
        listOf("cmd", "/c", "gradlew.bat bootJar && docker build -t gym-crm-main:latest .")
    } else {
        listOf("sh", "-c", "./gradlew bootJar && docker build -t gym-crm-main:latest .")
    }
}

tasks.register<Exec>("buildWorkloadImage") {
    workingDir = file("../trainer-workload-service")
    environment("JAVA_HOME", javaHome)
    environment("PATH", "$javaHome/bin;" + (System.getenv("PATH") ?: ""))
    commandLine = if (System.getProperty("os.name").lowercase().contains("windows")) {
        listOf("cmd", "/c", "gradlew.bat bootJar && docker build -t trainer-workload-service:latest .")
    } else {
        listOf("sh", "-c", "./gradlew bootJar && docker build -t trainer-workload-service:latest .")
    }
}

tasks.named("test") {
    dependsOn("buildCrmImage", "buildWorkloadImage")
}