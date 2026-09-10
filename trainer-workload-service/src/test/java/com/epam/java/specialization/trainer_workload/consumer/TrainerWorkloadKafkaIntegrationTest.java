package com.epam.java.specialization.trainer_workload.consumer;

import com.epam.java.specialization.common.dto.ActionType;
import com.epam.java.specialization.common.dto.TrainerWorkloadRequestDto;
import com.epam.java.specialization.trainer_workload.repository.TrainerWorkloadRepository;
import com.epam.java.specialization.trainer_workload.service.interfaces.TrainerWorkloadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        MongoRepositoriesAutoConfiguration.class
})
@EmbeddedKafka(
        partitions = 1,
        topics = {"${app.kafka.topics.trainer-workload:trainer-workload-topic-test}", "${app.kafka.topics.trainer-workload-dlt:trainer-workload-topic-test.DLT}"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
)
class TrainerWorkloadKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    private TrainerWorkloadRepository repository;

    @SpyBean
    private TrainerWorkloadService workloadService;

    @SpyBean
    private TrainerWorkloadDltConsumer dltConsumer;

    @Value("${app.kafka.topics.trainer-workload:trainer-workload-topic-test}")
    private String workloadTopic;

    @Test
    @DisplayName("Should successfully consume event from Kafka topic and process trainer workload")
    void shouldReceiveKafkaEventAndSaveWorkload() {
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

        doAnswer(invocation -> null).when(workloadService).processTrainingWorkload(any(TrainerWorkloadRequestDto.class));

        kafkaTemplate.send(workloadTopic, "Trainer.Active", requestDto);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(workloadService, atLeast(1)).processTrainingWorkload(any(TrainerWorkloadRequestDto.class));
        });
    }

    @Test
    @DisplayName("Should retry processing on failure and route message to DLT topic after exceeding max-attempts")
    void shouldRouteToDlt_WhenProcessingRepeatedlyFails() {
        TrainerWorkloadRequestDto failingDto = TrainerWorkloadRequestDto.builder()
                .username("Trainer.Fail")
                .firstName("Fail")
                .lastName("Trainer")
                .isActive(true)
                .trainingDate(new Date())
                .trainingDuration(30)
                .actionType(ActionType.ADD)
                .build();

        doThrow(new RuntimeException("Simulated Kafka processing error"))
                .when(workloadService).processTrainingWorkload(any(TrainerWorkloadRequestDto.class));

        kafkaTemplate.send(workloadTopic, "Trainer.Fail", failingDto);

        await().atMost(7, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(workloadService, atLeast(2)).processTrainingWorkload(any(TrainerWorkloadRequestDto.class));
            verify(dltConsumer, atLeast(1)).consumeDeadLetterMessage(any(), any(), any(), any(), any(), any());
        });
    }
}