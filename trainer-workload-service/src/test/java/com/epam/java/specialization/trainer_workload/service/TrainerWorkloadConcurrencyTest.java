package com.epam.java.specialization.trainer_workload.service;

import com.epam.java.specialization.trainer_workload.mapper.TrainerWorkloadMapperImpl;
import com.epam.java.specialization.trainer_workload.repository.InMemoryTrainerWorkloadRepository;
import com.epam.java.specialization.trainer_workload.repository.TrainerWorkloadRepository;
import com.epam.java.specialization.trainer_workload.service.implementations.TrainerWorkloadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.epam.java.specialization.common.dto.*;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerWorkloadConcurrencyTest {

    private TrainerWorkloadRepository repository;
    private TrainerWorkloadServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTrainerWorkloadRepository();
        service = new TrainerWorkloadServiceImpl(repository, new TrainerWorkloadMapperImpl());
    }

    @Test
    @DisplayName("Should process concurrent ADD operations without race conditions or exceptions")
    void concurrentWorkloadProcessing_ShouldCalculateExactSum() throws InterruptedException {
        int threadsCount = 20;
        int operationsPerThread = 50;
        int durationPerOp = 10;
        String username = "Concurrent.Trainer";

        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
        CountDownLatch latch = new CountDownLatch(threadsCount);
        AtomicInteger successfulReads = new AtomicInteger(0);

        for (int i = 0; i < threadsCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder()
                                .username(username)
                                .firstName("Concurrent")
                                .lastName("Trainer")
                                .isActive(true)
                                .trainingDate(new Date())
                                .trainingDuration(durationPerOp)
                                .actionType(ActionType.ADD)
                                .build();
                        service.processTrainingWorkload(request);

                        TrainerWorkloadResponseDto dto = service.getTrainerWorkload(username, null, null);
                        if (dto != null) {
                            successfulReads.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        int expectedTotalDuration = threadsCount * operationsPerThread * durationPerOp;
        TrainerWorkloadResponseDto finalWorkload = service.getTrainerWorkload(username, null, null);

        assertThat(finalWorkload).isNotNull();
        int actualTotalDuration = finalWorkload.getYears().stream()
                .flatMap(y -> y.getMonths().stream())
                .mapToInt(m -> m.getTrainingSummaryDuration())
                .sum();

        assertThat(actualTotalDuration).isEqualTo(expectedTotalDuration);
        assertThat(successfulReads.get()).isGreaterThan(0);
    }
}