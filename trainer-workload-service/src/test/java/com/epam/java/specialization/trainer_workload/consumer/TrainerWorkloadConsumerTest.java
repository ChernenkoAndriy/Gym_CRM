package com.epam.java.specialization.trainer_workload.consumer;

import com.epam.java.specialization.common.dto.ActionType;
import com.epam.java.specialization.common.dto.TrainerWorkloadRequestDto;
import com.epam.java.specialization.trainer_workload.service.interfaces.TrainerWorkloadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadConsumerTest {

    @Mock
    private TrainerWorkloadService workloadService;

    @InjectMocks
    private TrainerWorkloadConsumer consumer;

    @Test
    @DisplayName("Should receive Kafka event and delegate processing to TrainerWorkloadService")
    void consumeWorkloadEvent_Success() {
        TrainerWorkloadRequestDto requestDto = TrainerWorkloadRequestDto.builder()
                .username("Trainer.Ten")
                .firstName("Trainer")
                .lastName("Ten")
                .isActive(true)
                .trainingDate(new Date())
                .trainingDuration(45)
                .actionType(ActionType.ADD)
                .build();

        consumer.consumeWorkloadEvent(requestDto, "Trainer.Ten", 0, 100L);

        verify(workloadService, times(1)).processTrainingWorkload(requestDto);
    }

    @Test
    @DisplayName("Should rethrow exception on processing failure to trigger Kafka retry and DLT routing")
    void consumeWorkloadEvent_ExceptionRethrown() {
        TrainerWorkloadRequestDto requestDto = TrainerWorkloadRequestDto.builder()
                .username("Trainer.Ten")
                .build();

        doThrow(new RuntimeException("Database error")).when(workloadService).processTrainingWorkload(any());

        assertThatThrownBy(() -> consumer.consumeWorkloadEvent(requestDto, "Trainer.Ten", 0, 100L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(workloadService, times(1)).processTrainingWorkload(requestDto);
    }
}