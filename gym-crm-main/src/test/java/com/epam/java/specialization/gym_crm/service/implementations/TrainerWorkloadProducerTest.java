package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.common.dto.ActionType;
import com.epam.java.specialization.common.dto.TrainerWorkloadRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadProducerTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private TrainerWorkloadProducer producer;

    private static final String QUEUE_NAME = "trainer-workload-queue";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(producer, "destinationQueue", QUEUE_NAME);
    }

    @Test
    @DisplayName("Should send workload request DTO to configured ActiveMQ queue")
    void sendWorkloadRequest_ShouldSendToQueue() {
        LocalDate localDate = LocalDate.of(2026, 9, 1);
        Date testDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        TrainerWorkloadRequestDto requestDto = TrainerWorkloadRequestDto.builder()
                .username("Trainer.John")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .trainingDate(testDate)
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        producer.sendWorkloadRequest(requestDto);

        verify(jmsTemplate, times(1)).convertAndSend(eq(QUEUE_NAME), eq(requestDto));
    }
}