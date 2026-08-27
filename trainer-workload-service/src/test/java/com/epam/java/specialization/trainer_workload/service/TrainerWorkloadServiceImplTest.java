package com.epam.java.specialization.trainer_workload.service;

import com.epam.java.specialization.trainer_workload.exception.EntityNotFoundException;
import com.epam.java.specialization.trainer_workload.mapper.TrainerWorkloadMapper;
import com.epam.java.specialization.trainer_workload.model.MonthWorkload;
import com.epam.java.specialization.trainer_workload.model.TrainerWorkload;
import com.epam.java.specialization.trainer_workload.model.YearWorkload;
import com.epam.java.specialization.trainer_workload.repository.TrainerWorkloadRepository;
import com.epam.java.specialization.trainer_workload.service.implementations.TrainerWorkloadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.epam.java.specialization.common.dto.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceImplTest {

    @Mock
    private TrainerWorkloadRepository repository;

    @Mock
    private TrainerWorkloadMapper mapper;

    @InjectMocks
    private TrainerWorkloadServiceImpl service;

    private Date trainingDate;

    @BeforeEach
    void setUp() {
        LocalDate localDate = LocalDate.of(2026, 7, 10);
        trainingDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    @DisplayName("Should create new record and add training duration when trainer not exists")
    void processTrainingWorkload_ShouldAddNewRecord_WhenTrainerDoesNotExist() {
        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder()
                .username("Trainer.Ten")
                .firstName("Trainer")
                .lastName("Ten")
                .isActive(true)
                .trainingDate(trainingDate)
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        when(repository.findByUsername("Trainer.Ten")).thenReturn(Optional.empty());

        service.processTrainingWorkload(request);

        verify(repository, times(1)).save(argThat(workload -> {
            assertThat(workload.getUsername()).isEqualTo("Trainer.Ten");
            assertThat(workload.getYears()).hasSize(1);
            YearWorkload year = workload.getYears().get(0);
            assertThat(year.getYearNumber()).isEqualTo(2026);
            assertThat(year.getMonths()).hasSize(1);
            MonthWorkload month = year.getMonths().get(0);
            assertThat(month.getMonthNumber()).isEqualTo(7);
            assertThat(month.getSummaryDuration()).isEqualTo(60);
            return true;
        }));
    }

    @Test
    @DisplayName("Should subtract training duration on DELETE action and not drop below zero")
    void processTrainingWorkload_ShouldSubtractDuration_WhenActionIsDelete() {
        TrainerWorkload existingWorkload = TrainerWorkload.builder()
                .username("Trainer.Ten")
                .firstName("Trainer")
                .lastName("Ten")
                .isActive(true)
                .years(new ArrayList<>())
                .build();

        YearWorkload year = YearWorkload.builder().yearNumber(2026).months(new ArrayList<>()).build();
        MonthWorkload month = MonthWorkload.builder().monthNumber(7).summaryDuration(100).build();
        year.getMonths().add(month);
        existingWorkload.getYears().add(year);

        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder()
                .username("Trainer.Ten")
                .firstName("Trainer")
                .lastName("Ten")
                .isActive(true)
                .trainingDate(trainingDate)
                .trainingDuration(40)
                .actionType(ActionType.DELETE)
                .build();

        when(repository.findByUsername("Trainer.Ten")).thenReturn(Optional.of(existingWorkload));

        service.processTrainingWorkload(request);

        verify(repository, times(1)).save(existingWorkload);
        assertThat(month.getSummaryDuration()).isEqualTo(60);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when getting workload of unknown trainer")
    void getTrainerWorkload_ShouldThrowNotFound_WhenTrainerAbsent() {
        when(repository.findByUsername("Unknown.Trainer")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTrainerWorkload("Unknown.Trainer", null, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer workload not found for username: Unknown.Trainer");
    }

    @Test
    @DisplayName("Should return full workload snapshot when year and month are null")
    void getTrainerWorkload_ShouldReturnAllYears_WhenNoFiltersApplied() {
        TrainerWorkload existing = TrainerWorkload.builder()
                .username("Trainer.Ten")
                .years(new ArrayList<>())
                .build();
        TrainerWorkloadResponseDto responseDto = TrainerWorkloadResponseDto.builder().username("Trainer.Ten").build();

        when(repository.findByUsername("Trainer.Ten")).thenReturn(Optional.of(existing));
        when(mapper.toResponseDto(any(TrainerWorkload.class))).thenReturn(responseDto);

        TrainerWorkloadResponseDto result = service.getTrainerWorkload("Trainer.Ten", null, null);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("Trainer.Ten");
        verify(mapper, times(1)).toResponseDto(any(TrainerWorkload.class));
    }

    @Test
    @DisplayName("Should filter workload by year and month and return zero if month record not found")
    void getTrainerWorkload_ShouldFilterByYearAndMonth_AndReturnZero_WhenMonthRecordAbsent() {
        TrainerWorkload existing = TrainerWorkload.builder()
                .username("Trainer.Ten")
                .years(new ArrayList<>())
                .build();

        when(repository.findByUsername("Trainer.Ten")).thenReturn(Optional.of(existing));
        when(mapper.toResponseDto(any(TrainerWorkload.class))).thenAnswer(invocation -> {
            TrainerWorkload filtered = invocation.getArgument(0);
            return TrainerWorkloadResponseDto.builder()
                    .username(filtered.getUsername())
                    .build();
        });

        TrainerWorkloadResponseDto result = service.getTrainerWorkload("Trainer.Ten", 2026, 8);

        assertThat(result).isNotNull();
        verify(mapper, times(1)).toResponseDto(argThat(filtered -> {
            assertThat(filtered.getYears()).hasSize(1);
            assertThat(filtered.getYears().get(0).getYearNumber()).isEqualTo(2026);
            assertThat(filtered.getYears().get(0).getMonths()).hasSize(1);
            assertThat(filtered.getYears().get(0).getMonths().get(0).getMonthNumber()).isEqualTo(8);
            assertThat(filtered.getYears().get(0).getMonths().get(0).getSummaryDuration()).isEqualTo(0);
            return true;
        }));
    }
}