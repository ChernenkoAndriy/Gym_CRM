plugins {
    `java-library`
    `maven-publish`
}

group = "com.epam.java.specialization"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    api("jakarta.validation:jakarta.validation-api:3.0.2")
    api("com.fasterxml.jackson.core:jackson-annotations:2.15.3")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}