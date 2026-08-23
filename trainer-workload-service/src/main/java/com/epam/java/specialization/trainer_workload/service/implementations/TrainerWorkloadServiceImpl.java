package com.epam.java.specialization.trainer_workload.service.implementations;

import com.epam.java.specialization.trainer_workload.dto.TrainerWorkloadRequestDto;
import com.epam.java.specialization.trainer_workload.dto.TrainerWorkloadResponseDto;
import com.epam.java.specialization.trainer_workload.exception.EntityNotFoundException;
import com.epam.java.specialization.trainer_workload.mapper.TrainerWorkloadMapper;
import com.epam.java.specialization.trainer_workload.model.ActionType;
import com.epam.java.specialization.trainer_workload.model.MonthWorkload;
import com.epam.java.specialization.trainer_workload.model.TrainerWorkload;
import com.epam.java.specialization.trainer_workload.model.YearWorkload;
import com.epam.java.specialization.trainer_workload.repository.TrainerWorkloadRepository;
import com.epam.java.specialization.trainer_workload.service.interfaces.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private final TrainerWorkloadRepository repository;
    private final TrainerWorkloadMapper mapper;

    @Override
    public synchronized void processTrainingWorkload(TrainerWorkloadRequestDto request) {
        log.info("Processing training workload action [{}] for trainer: {}", request.getActionType(), request.getUsername());

        LocalDate trainingLocalDate = request.getTrainingDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        int year = trainingLocalDate.getYear();
        int month = trainingLocalDate.getMonthValue();
        int duration = request.getTrainingDuration();

        TrainerWorkload trainerWorkload = repository.findByUsername(request.getUsername())
                .orElseGet(() -> createNewTrainerWorkload(request));

        trainerWorkload.setFirstName(request.getFirstName());
        trainerWorkload.setLastName(request.getLastName());
        trainerWorkload.setIsActive(request.getIsActive());

        YearWorkload yearWorkload = trainerWorkload.getYears().stream()
                .filter(y -> y.getYearNumber() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearWorkload newYear = YearWorkload.builder()
                            .yearNumber(year)
                            .months(new ArrayList<>())
                            .build();
                    trainerWorkload.getYears().add(newYear);
                    return newYear;
                });

        MonthWorkload monthWorkload = yearWorkload.getMonths().stream()
                .filter(m -> m.getMonthNumber() == month)
                .findFirst()
                .orElseGet(() -> {
                    MonthWorkload newMonth = MonthWorkload.builder()
                            .monthNumber(month)
                            .summaryDuration(0)
                            .build();
                    yearWorkload.getMonths().add(newMonth);
                    return newMonth;
                });

        if (ActionType.ADD.equals(request.getActionType())) {
            monthWorkload.setSummaryDuration(monthWorkload.getSummaryDuration() + duration);
            log.info("Added {} minutes for trainer {} (Year: {}, Month: {}). New total: {} minutes",
                    duration, request.getUsername(), year, month, monthWorkload.getSummaryDuration());
        } else if (ActionType.DELETE.equals(request.getActionType())) {
            int updatedDuration = Math.max(0, monthWorkload.getSummaryDuration() - duration);
            monthWorkload.setSummaryDuration(updatedDuration);
            log.info("Subtracted {} minutes for trainer {} (Year: {}, Month: {}). New total: {} minutes",
                    duration, request.getUsername(), year, month, updatedDuration);
        }

        repository.save(trainerWorkload);
    }

    @Override
    public synchronized TrainerWorkloadResponseDto getTrainerWorkload(String username, Integer year, Integer month) {
        log.info("Fetching workload summary for trainer: {} (Year: {}, Month: {})", username, year, month);

        TrainerWorkload trainerWorkload = repository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Workload summary retrieval failed: Trainer not found for username: {}", username);
                    return new EntityNotFoundException("Trainer workload not found for username: " + username);
                });

        TrainerWorkload snapshot = createSnapshot(trainerWorkload);

        if (year == null && month == null) {
            return mapper.toResponseDto(snapshot);
        }

        List<YearWorkload> matchedYears = snapshot.getYears().stream()
                .filter(y -> year == null || y.getYearNumber() == year)
                .map(y -> {
                    List<MonthWorkload> matchedMonths = y.getMonths().stream()
                            .filter(m -> month == null || m.getMonthNumber() == month)
                            .collect(Collectors.toList());

                    if (month != null && matchedMonths.isEmpty()) {
                        matchedMonths.add(MonthWorkload.builder().monthNumber(month).summaryDuration(0).build());
                    }

                    return YearWorkload.builder()
                            .yearNumber(y.getYearNumber())
                            .months(matchedMonths)
                            .build();
                })
                .collect(Collectors.toList());

        if (year != null && matchedYears.isEmpty()) {
            List<MonthWorkload> monthsList = new ArrayList<>();
            if (month != null) {
                monthsList.add(MonthWorkload.builder().monthNumber(month).summaryDuration(0).build());
            }
            matchedYears.add(YearWorkload.builder().yearNumber(year).months(monthsList).build());
        }

        snapshot.setYears(matchedYears);
        return mapper.toResponseDto(snapshot);
    }

    private TrainerWorkload createSnapshot(TrainerWorkload source) {
        List<YearWorkload> clonedYears = source.getYears().stream()
                .map(y -> YearWorkload.builder()
                        .yearNumber(y.getYearNumber())
                        .months(y.getMonths().stream()
                                .map(m -> MonthWorkload.builder()
                                        .monthNumber(m.getMonthNumber())
                                        .summaryDuration(m.getSummaryDuration())
                                        .build())
                                .collect(Collectors.toCollection(ArrayList::new)))
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));

        return TrainerWorkload.builder()
                .username(source.getUsername())
                .firstName(source.getFirstName())
                .lastName(source.getLastName())
                .isActive(source.getIsActive())
                .years(clonedYears)
                .build();
    }

    private TrainerWorkload createNewTrainerWorkload(TrainerWorkloadRequestDto request) {
        return TrainerWorkload.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(request.getIsActive())
                .years(new ArrayList<>())
                .build();
    }
}