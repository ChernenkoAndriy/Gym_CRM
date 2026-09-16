package com.epam.java.specialization.integration.cucumber;

import com.epam.java.specialization.integration.environment.EnvironmentManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;

public class CucumberHooks {

    @Autowired
    private ScenarioTestContext context;

    @Before(order = 0)
    public void setupEnvironment() {
        EnvironmentManager.startEnvironment();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @After
    public void tearDownScenario() {
        context.setJwtToken(null);
        context.setTrainerUsername(null);
        context.setTraineeUsername(null);
        context.setLastResponse(null);
    }
}