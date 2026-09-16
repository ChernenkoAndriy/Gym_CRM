package com.epam.java.specialization.integration.environment;

import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;

public class EnvironmentManager {

    private static final String CRM_SERVICE = "gym-crm-main";
    private static final int CRM_PORT = 8080;

    private static final String WORKLOAD_SERVICE = "trainer-workload-service";
    private static final int WORKLOAD_PORT = 8081;

    private static DockerComposeContainer<?> composeContainer;
    private static String crmBaseUrl;
    private static String workloadBaseUrl;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (composeContainer != null) {
                try {
                    composeContainer.stop();
                } catch (Exception ignored) {
                }
            }
        }));
    }

    public static synchronized void startEnvironment() {
        if (composeContainer != null) {
            return;
        }

        File composeFile = new File("src/test/resources/docker-compose-test.yml");

        composeContainer = new DockerComposeContainer<>(composeFile)
                .withLocalCompose(true)
                .waitingFor(
                        CRM_SERVICE,
                        Wait.forLogMessage(".*Started GymCrmApplication.*\\n", 1)
                                .withStartupTimeout(Duration.ofMinutes(3))
                )
                .waitingFor(
                        WORKLOAD_SERVICE,
                        Wait.forLogMessage(".*Started TrainerWorkloadApplication.*\\n", 1)
                                .withStartupTimeout(Duration.ofMinutes(3))
                );

        composeContainer.start();

        crmBaseUrl = "http://localhost:" + CRM_PORT;
        workloadBaseUrl = "http://localhost:" + WORKLOAD_PORT;
    }

    public static String getCrmBaseUrl() {
        return crmBaseUrl;
    }

    public static String getWorkloadBaseUrl() {
        return workloadBaseUrl;
    }
}