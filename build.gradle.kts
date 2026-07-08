plugins {
    java
}

group = "com.epam.java.specialization"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Spring Core & ORM (Spring 6.x)
    implementation("org.springframework:spring-context:6.1.10")
    implementation("org.springframework:spring-orm:6.1.10")
    implementation("org.springframework:spring-tx:6.1.10")

    // Hibernate Core (Jakarta Persistence 3.1 сумісний)
    implementation("org.hibernate.orm:hibernate-core:6.5.2.Final")

    // Валідація (Hibernate Validator + Jakarta Validation API)
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    implementation("org.glassfish:jakarta.el:4.0.2")

    // База даних
    implementation("org.postgresql:postgresql:42.7.3")

    // Jackson для роботи з JSON (init-data)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")

    // Логування
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.6")

    // Lombok для генерації коду
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    //SnakeYAML для підтримки парсингу .yaml конфігурацій
    implementation("org.yaml:snakeyaml:2.2")

    // Тестування
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.springframework:spring-test:6.1.10")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
}

tasks.test {
    useJUnitPlatform()
}