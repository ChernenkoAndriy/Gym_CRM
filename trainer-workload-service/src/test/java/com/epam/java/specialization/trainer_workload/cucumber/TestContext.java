package com.epam.java.specialization.trainer_workload.cucumber;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

@Component
@ScenarioScope
@Getter
@Setter
public class TestContext {

    private ResultActions latestResponse;
    private String token;
    private String username;
    private String password;
    private final Map<String, Object> payload = new HashMap<>();

    public void put(String key, Object value) {
        payload.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) payload.get(key);
    }

    public void reset() {
        latestResponse = null;
        token = null;
        username = null;
        password = null;
        payload.clear();
    }
}