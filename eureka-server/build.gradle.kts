plugins {
    java
    id("org.springframework.boot") version "3.3.3"
}

group = "com.epam.java.specialization"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // BOMs (Керування версіями)
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.3"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2023.0.3"))

    // Eureka Server & Actuator
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}