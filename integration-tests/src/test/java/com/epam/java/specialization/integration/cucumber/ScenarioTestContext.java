package com.epam.java.specialization.integration.cucumber;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ScenarioScope
public class ScenarioTestContext {

    private String jwtToken;
    private String trainerUsername;
    private String traineeUsername;
    private Response lastResponse;
    private final Map<String, Object> sessionData = new HashMap<>();

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

    public String getTraineeUsername() {
        return traineeUsername;
    }

    public void setTraineeUsername(String traineeUsername) {
        this.traineeUsername = traineeUsername;
    }

    public Response getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(Response lastResponse) {
        this.lastResponse = lastResponse;
    }

    public void put(String key, Object value) {
        sessionData.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) sessionData.get(key);
    }
}