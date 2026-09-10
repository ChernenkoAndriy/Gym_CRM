package com.epam.java.specialization.trainer_workload.service.implementations;

import com.epam.java.specialization.common.dto.ActionType;
import com.epam.java.specialization.common.dto.TrainerWorkloadRequestDto;
import com.epam.java.specialization.common.dto.TrainerWorkloadResponseDto;
import com.epam.java.specialization.trainer_workload.exception.EntityNotFoundException;
import com.epam.java.specialization.trainer_workload.exception.InvalidWorkloadRequestException;
import com.epam.java.specialization.trainer_workload.mapper.TrainerWorkloadMapper;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private final TrainerWorkloadRepository repository;
    private final TrainerWorkloadMapper mapper;

    @Override
    public void processTrainingWorkload(TrainerWorkloadRequestDto request) {
        log.debug("[OPERATION] Validating incoming workload request for trainer: {}",
                request != null ? request.getUsername() : "null");
        validateRequest(request);

        log.info("Processing training workload action [{}] for trainer: {}",
                request.getActionType(), request.getUsername());

        LocalDate trainingLocalDate = request.getTrainingDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        int year = trainingLocalDate.getYear();
        int month = trainingLocalDate.getMonthValue();
        int duration = request.getTrainingDuration();

        log.debug("[OPERATION] Querying MongoDB for trainer workload by username: '{}'", request.getUsername());
        Optional<TrainerWorkload> optionalWorkload = repository.findByUsername(request.getUsername());

        TrainerWorkload trainerWorkload;
        if (optionalWorkload.isPresent()) {
            trainerWorkload = optionalWorkload.get();
            log.debug("[OPERATION] Existing trainer document found with id: '{}'", trainerWorkload.getId());
        } else {
            log.debug("[OPERATION] Trainer document not found in MongoDB. Initializing new record for: '{}'",
                    request.getUsername());
            trainerWorkload = createNewTrainerWorkload(request);
        }

        trainerWorkload.setFirstName(request.getFirstName());
        trainerWorkload.setLastName(request.getLastName());
        trainerWorkload.setIsActive(request.getIsActive());

        if (trainerWorkload.getYears() == null) {
            trainerWorkload.setYears(new ArrayList<>());
        }

        log.debug("[OPERATION] Resolving workload entry for year: {}", year);
        YearWorkload yearWorkload = trainerWorkload.getYears().stream()
                .filter(y -> y.getYearNumber() == year)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("[OPERATION] Year entry {} not present. Creating new year structure.", year);
                    YearWorkload newYear = YearWorkload.builder()
                            .yearNumber(year)
                            .months(new ArrayList<>())
                            .build();
                    trainerWorkload.getYears().add(newYear);
                    return newYear;
                });

        if (yearWorkload.getMonths() == null) {
            yearWorkload.setMonths(new ArrayList<>());
        }

        log.debug("[OPERATION] Resolving workload entry for month: {}", month);
        MonthWorkload monthWorkload = yearWorkload.getMonths().stream()
                .filter(m -> m.getMonthNumber() == month)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("[OPERATION] Month entry {} not present. Initializing with duration 0.", month);
                    MonthWorkload newMonth = MonthWorkload.builder()
                            .monthNumber(month)
                            .summaryDuration(0)
                            .build();
                    yearWorkload.getMonths().add(newMonth);
                    return newMonth;
                });

        int previousDuration = monthWorkload.getSummaryDuration();
        if (ActionType.ADD.equals(request.getActionType())) {
            monthWorkload.setSummaryDuration(previousDuration + duration);
            log.info("[OPERATION] Added {} minutes for trainer {} (Year: {}, Month: {}). Old: {}, New total: {} minutes",
                    duration, request.getUsername(), year, month, previousDuration, monthWorkload.getSummaryDuration());
        } else if (ActionType.DELETE.equals(request.getActionType())) {
            int updatedDuration = Math.max(0, previousDuration - duration);
            monthWorkload.setSummaryDuration(updatedDuration);
            log.info("[OPERATION] Subtracted {} minutes for trainer {} (Year: {}, Month: {}). Old: {}, New total: {} minutes",
                    duration, request.getUsername(), year, month, previousDuration, updatedDuration);
        }

        log.debug("[OPERATION] Persisting updated document to MongoDB for trainer: '{}'", trainerWorkload.getUsername());
        TrainerWorkload savedWorkload = repository.save(trainerWorkload);
        log.debug("[OPERATION] Document successfully saved with Mongo id: '{}'", savedWorkload.getId());
    }

    @Override
    public TrainerWorkloadResponseDto getTrainerWorkload(String username, Integer year, Integer month) {
        log.debug("[OPERATION] Validating username for workload query: '{}'", username);
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidWorkloadRequestException("Username must not be empty");
        }

        log.info("Fetching workload summary for trainer: {} (Year: {}, Month: {})", username, year, month);
        log.debug("[OPERATION] Executing MongoDB query findByUsername for: '{}'", username);

        TrainerWorkload trainerWorkload = repository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("[OPERATION] Trainer record not found in MongoDB for username: '{}'", username);
                    return new EntityNotFoundException("Trainer workload not found for username: " + username);
                });

        log.debug("[OPERATION] Creating isolated snapshot of document for filtering");
        TrainerWorkload snapshot = createSnapshot(trainerWorkload);

        if (year == null && month == null) {
            log.debug("[OPERATION] No year/month filter specified. Returning full aggregated response");
            return mapper.toResponseDto(snapshot);
        }

        log.debug("[OPERATION] Applying filters - Year: {}, Month: {}", year, month);
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
        log.debug("[OPERATION] Mapping filtered snapshot to response DTO");
        return mapper.toResponseDto(snapshot);
    }

    private void validateRequest(TrainerWorkloadRequestDto request) {
        if (request == null) {
            throw new InvalidWorkloadRequestException("Request body cannot be null");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new InvalidWorkloadRequestException("Username is required");
        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new InvalidWorkloadRequestException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new InvalidWorkloadRequestException("Last name is required");
        }
        if (request.getIsActive() == null) {
            throw new InvalidWorkloadRequestException("Trainer status (isActive) is required");
        }
        if (request.getTrainingDate() == null) {
            throw new InvalidWorkloadRequestException("Training date is required");
        }
        if (request.getTrainingDuration() <= 0) {
            throw new InvalidWorkloadRequestException("Training duration must be a positive number");
        }
        if (request.getActionType() == null) {
            throw new InvalidWorkloadRequestException("Action type (ADD/DELETE) is required");
        }
    }

    private TrainerWorkload createSnapshot(TrainerWorkload source) {
        List<YearWorkload> sourceYears = source.getYears() != null ? source.getYears() : new ArrayList<>();
        List<YearWorkload> clonedYears = sourceYears.stream()
                .map(y -> {
                    List<MonthWorkload> sourceMonths = y.getMonths() != null ? y.getMonths() : new ArrayList<>();
                    return YearWorkload.builder()
                            .yearNumber(y.getYearNumber())
                            .months(sourceMonths.stream()
                                    .map(m -> MonthWorkload.builder()
                                            .monthNumber(m.getMonthNumber())
                                            .summaryDuration(m.getSummaryDuration())
                                            .build())
                                    .collect(Collectors.toCollection(ArrayList::new)))
                            .build();
                })
                .collect(Collectors.toCollection(ArrayList::new));

        return TrainerWorkload.builder()
                .id(source.getId())
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