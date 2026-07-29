package com.epam.java.specialization.gym_crm.health;

import com.epam.java.specialization.gym_crm.repository.TrainingTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final TrainingTypeRepository trainingTypeRepository;

    @Override
    public Health health() {
        try {
            long count = trainingTypeRepository.count();
            if (count > 0) {
                return Health.up()
                        .withDetail("database", "PostgreSQL is reachable")
                        .withDetail("trainingTypesCount", count)
                        .withDetail("message", "Database contains initial static data")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "PostgreSQL is reachable")
                        .withDetail("trainingTypesCount", 0)
                        .withDetail("warning", "Training types database table is empty")
                        .build();
            }
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("database", "PostgreSQL connection failed")
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }
}