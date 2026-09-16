package com.epam.java.specialization.integration.cucumber.steps;

import com.epam.java.specialization.common.dto.MonthWorkloadDto;
import com.epam.java.specialization.common.dto.TrainerWorkloadResponseDto;
import com.epam.java.specialization.integration.cucumber.ScenarioTestContext;
import com.epam.java.specialization.integration.cucumber.support.TestDataFactory;
import com.epam.java.specialization.integration.environment.EnvironmentManager;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommonIntegrationSteps {

    @Autowired
    private ScenarioTestContext context;

    @Given("a trainer is registered in CRM with first name {string}, last name {string} and specialization {string}")
    public void registerTrainer(String firstName, String lastName, String specialization) {
        Map<String, Object> payload = Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "specializationId", 1L
        );

        Response response = given()
                .baseUri(EnvironmentManager.getCrmBaseUrl())
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/trainers")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String username = response.jsonPath().getString("username");
        String password = response.jsonPath().getString("password");
        context.setTrainerUsername(username);

        String token = response.jsonPath().getString("token");
        if (token != null && !token.isBlank()) {
            context.setJwtToken(token);
        } else {
            authenticateUser(username, password);
        }
    }

    @Given("a trainee is registered in CRM with first name {string} and last name {string}")
    public void registerTrainee(String firstName, String lastName) {
        Map<String, Object> payload = Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "dateOfBirth", "2000-01-01",
                "address", "Kyiv"
        );

        Response response = given()
                .baseUri(EnvironmentManager.getCrmBaseUrl())
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/trainees")
                .then()
                .statusCode(200)
                .extract()
                .response();

        context.setTraineeUsername(response.jsonPath().getString("username"));
    }

    @When("a new training is added in CRM with name {string}, date {string} and duration {int} minutes")
    public void addTraining(String trainingName, String trainingDate, int duration) {
        Map<String, Object> payload = TestDataFactory.createTrainingPayload(
                context.getTraineeUsername(),
                context.getTrainerUsername(),
                trainingName,
                trainingDate,
                duration
        );

        Response response = given()
                .baseUri(EnvironmentManager.getCrmBaseUrl())
                .header("Authorization", "Bearer " + context.getJwtToken())
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/trainings")
                .then()
                .extract()
                .response();

        context.setLastResponse(response);
    }

    @Then("CRM system returns status code {int}")
    public void verifyCrmStatus(int expectedStatusCode) {
        assertEquals(expectedStatusCode, context.getLastResponse().getStatusCode());
    }

    @Then("within {int} seconds trainer workload service contains {int} minutes for the trainer for year {int} and month {int}")
    public void verifyTrainerWorkloadAsync(int timeoutSeconds, int expectedDuration, int year, int month) {
        // Отримуємо значення з ThreadLocal-контексту ДО заходу у потік Awaitility
        final String trainerUsername = context.getTrainerUsername();
        final String jwtToken = context.getJwtToken();

        await()
                .atMost(Duration.ofSeconds(timeoutSeconds))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    Response response = given()
                            .baseUri(EnvironmentManager.getWorkloadBaseUrl())
                            .header("Authorization", "Bearer " + jwtToken)
                            .when()
                            .get("/workloads/" + trainerUsername)
                            .then()
                            .statusCode(200)
                            .extract()
                            .response();

                    TrainerWorkloadResponseDto workload = response.as(TrainerWorkloadResponseDto.class);
                    assertNotNull(workload, "Workload response must not be null");

                    int actualDuration = 0;
                    if (workload.getYears() != null) {
                        actualDuration = workload.getYears().stream()
                                .filter(y -> y.getYear() == year)
                                .flatMap(y -> y.getMonths().stream())
                                .filter(m -> m.getMonth() == month)
                                .mapToInt(MonthWorkloadDto::getTrainingSummaryDuration)
                                .sum();
                    }

                    assertEquals(expectedDuration, actualDuration, "Workload duration mismatch");
                });
    }

    private void authenticateUser(String username, String password) {
        Response response = given()
                .baseUri(EnvironmentManager.getCrmBaseUrl())
                .contentType(ContentType.JSON)
                .body(Map.of("username", username, "password", password))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String token = response.jsonPath().getString("token");
        context.setJwtToken(token);
    }
}