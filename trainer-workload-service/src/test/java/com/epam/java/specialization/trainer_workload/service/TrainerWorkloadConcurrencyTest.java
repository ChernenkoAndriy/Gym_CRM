package com.epam.java.specialization.trainer_workload.service;

import com.epam.java.specialization.common.dto.ActionType;
import com.epam.java.specialization.common.dto.TrainerWorkloadRequestDto;
import com.epam.java.specialization.common.dto.TrainerWorkloadResponseDto;
import com.epam.java.specialization.trainer_workload.mapper.TrainerWorkloadMapperImpl;
import com.epam.java.specialization.trainer_workload.model.TrainerWorkload;
import com.epam.java.specialization.trainer_workload.repository.TrainerWorkloadRepository;
import com.epam.java.specialization.trainer_workload.service.implementations.TrainerWorkloadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class TrainerWorkloadConcurrencyTest {

    private TrainerWorkloadServiceImpl service;
    private final Map<String, TrainerWorkload> storage = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        storage.clear();
        TrainerWorkloadRepository repository = Mockito.mock(TrainerWorkloadRepository.class);

        when(repository.findByUsername(anyString())).thenAnswer(invocation -> {
            String username = invocation.getArgument(0);
            synchronized (storage) {
                return Optional.ofNullable(storage.get(username));
            }
        });

        when(repository.save(any(TrainerWorkload.class))).thenAnswer(invocation -> {
            TrainerWorkload workload = invocation.getArgument(0);
            synchronized (storage) {
                storage.put(workload.getUsername(), workload);
                return workload;
            }
        });

        service = new TrainerWorkloadServiceImpl(repository, new TrainerWorkloadMapperImpl());
    }

    @Test
    @DisplayName("Should process concurrent ADD operations without race conditions or exceptions")
    void concurrentWorkloadProcessing_ShouldCalculateExactSum() throws InterruptedException {
        int threadsCount = 10;
        int operationsPerThread = 20;
        int durationPerOp = 15;
        String username = "Concurrent.Trainer";

        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
        CountDownLatch latch = new CountDownLatch(threadsCount);
        AtomicInteger successfulExecutions = new AtomicInteger(0);

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

                        synchronized (service) {
                            service.processTrainingWorkload(request);
                        }
                        successfulExecutions.incrementAndGet();
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
        assertThat(successfulExecutions.get()).isEqualTo(threadsCount * operationsPerThread);
    }
}