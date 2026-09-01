package com.epam.java.specialization.trainer_workload.consumer;

import com.epam.java.specialization.common.dto.ActionType;
import com.epam.java.specialization.common.dto.TrainerWorkloadRequestDto;
import com.epam.java.specialization.common.dto.TrainerWorkloadResponseDto;
import com.epam.java.specialization.trainer_workload.repository.TrainerWorkloadRepository;
import com.epam.java.specialization.trainer_workload.service.interfaces.TrainerWorkloadService;
import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Import(TrainerWorkloadJmsIntegrationTest.EmbeddedBrokerConfig.class)
class TrainerWorkloadJmsIntegrationTest {

    @TestConfiguration
    static class EmbeddedBrokerConfig {
        @Bean(initMethod = "start", destroyMethod = "stop")
        public BrokerService brokerService() throws Exception {
            BrokerService broker = new BrokerService();
            broker.setPersistent(false);
            broker.setUseJmx(false);
            broker.addConnector("vm://localhost");
            return broker;
        }
    }

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TrainerWorkloadRepository repository;

    @SpyBean
    private TrainerWorkloadService workloadService;

    @SpyBean
    private TrainerWorkloadDlqConsumer dlqConsumer;

    @Value("${app.jms.queue.workload:trainer-workload-queue-test}")
    private String workloadQueue;

    @BeforeEach
    void setUp() {
        repository.clear();
    }

    @Test
    @DisplayName("Should successfully consume message from queue and update trainer workload in storage")
    void shouldReceiveMessageAndSaveWorkload() {
        // Задаємо середину місяця для уникнення зсуву таймзони
        LocalDate localDate = LocalDate.of(2026, 9, 15);
        Date trainingDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        TrainerWorkloadRequestDto requestDto = TrainerWorkloadRequestDto.builder()
                .username("Trainer.Active")
                .firstName("Active")
                .lastName("Trainer")
                .isActive(true)
                .trainingDate(trainingDate)
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        jmsTemplate.convertAndSend(workloadQueue, requestDto);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            TrainerWorkloadResponseDto result = workloadService.getTrainerWorkload("Trainer.Active", 2026, 9);
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("Trainer.Active");
            assertThat(result.getYears()).isNotEmpty();
            assertThat(result.getYears().get(0).getMonths()).isNotEmpty();
            assertThat(result.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(60);
        });
    }

    @Test
    @DisplayName("Should retry processing on failure and route message to DLQ after exceeding max-deliveries")
    void shouldRouteToDlq_WhenProcessingRepeatedlyFails() {
        TrainerWorkloadRequestDto failingDto = TrainerWorkloadRequestDto.builder()
                .username("Trainer.Fail")
                .firstName("Fail")
                .lastName("Trainer")
                .isActive(true)
                .trainingDate(new Date())
                .trainingDuration(30)
                .actionType(ActionType.ADD)
                .build();

        doThrow(new RuntimeException("Simulated processing error"))
                .when(workloadService).processTrainingWorkload(any(TrainerWorkloadRequestDto.class));

        jmsTemplate.convertAndSend(workloadQueue, failingDto);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(workloadService, atLeast(2)).processTrainingWorkload(any(TrainerWorkloadRequestDto.class));
            verify(dlqConsumer, atLeast(1)).processDlqMessage(any());
        });
    }
}