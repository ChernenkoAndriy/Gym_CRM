package com.epam.java.specialization.gym_crm.client;

import com.epam.java.specialization.gym_crm.dto.external.TrainerWorkloadRequestDto;
import com.epam.java.specialization.gym_crm.dto.external.TrainerWorkloadResponseDto;
import com.epam.java.specialization.gym_crm.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrainerWorkloadFallbackFactory implements FallbackFactory<TrainerWorkloadClient> {

    @Override
    public TrainerWorkloadClient create(Throwable cause) {
        return new TrainerWorkloadClient() {
            @Override
            public ResponseEntity<Void> processWorkload(TrainerWorkloadRequestDto request) {
                log.error("Circuit Breaker fallback triggered when calling POST /workloads. Reason: {}",
                        cause.getMessage());
                throw new ServiceUnavailableException("Trainer Workload Service is unavailable: " + cause.getMessage());
            }

            @Override
            public ResponseEntity<TrainerWorkloadResponseDto> getTrainerWorkload(
                    String username, Integer year, Integer month) {
                log.error("Circuit Breaker fallback triggered when calling GET /workloads/{}. Reason: {}",
                        username, cause.getMessage());
                throw new ServiceUnavailableException("Trainer Workload Service is unavailable: " + cause.getMessage());
            }
        };
    }
}