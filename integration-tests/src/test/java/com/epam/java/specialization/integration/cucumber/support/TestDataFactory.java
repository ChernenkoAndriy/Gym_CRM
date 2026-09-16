package com.epam.java.specialization.integration.cucumber.support;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class TestDataFactory {

    public static Map<String, Object> createTrainingPayload(
            String traineeUsername,
            String trainerUsername,
            String trainingName,
            String date,
            int duration
    ) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("traineeUsername", traineeUsername);
        payload.put("trainerUsername", trainerUsername);
        payload.put("trainingName", trainingName);
        payload.put("trainingDate", LocalDate.parse(date).toString());
        payload.put("trainingDuration", duration);
        return payload;
    }
}