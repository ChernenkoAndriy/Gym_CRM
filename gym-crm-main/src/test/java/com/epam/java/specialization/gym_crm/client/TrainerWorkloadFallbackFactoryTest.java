package com.epam.java.specialization.gym_crm.client;

import com.epam.java.specialization.gym_crm.dto.external.ActionType;
import com.epam.java.specialization.gym_crm.dto.external.TrainerWorkloadRequestDto;
import com.epam.java.specialization.gym_crm.exception.ServiceUnavailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrainerWorkloadFallbackFactoryTest {

    private final TrainerWorkloadFallbackFactory fallbackFactory = new TrainerWorkloadFallbackFactory();

    @Test
    @DisplayName("Should throw ServiceUnavailableException when calling processWorkload via Fallback")
    void processWorkload_ShouldThrowServiceUnavailableException() {
        Throwable cause = new RuntimeException("Connection timed out");
        TrainerWorkloadClient fallbackClient = fallbackFactory.create(cause);

        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder()
                .username("Trainer.Ten")
                .firstName("Trainer")
                .lastName("Ten")
                .isActive(true)
                .trainingDate(new Date())
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        assertThatThrownBy(() -> fallbackClient.processWorkload(request))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("Trainer Workload Service is unavailable");
    }

    @Test
    @DisplayName("Should throw ServiceUnavailableException when calling getTrainerWorkload via Fallback")
    void getTrainerWorkload_ShouldThrowServiceUnavailableException() {
        Throwable cause = new RuntimeException("Service down");
        TrainerWorkloadClient fallbackClient = fallbackFactory.create(cause);

        assertThatThrownBy(() -> fallbackClient.getTrainerWorkload("Trainer.Ten", 2026, 8))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("Trainer Workload Service is unavailable");
    }
}