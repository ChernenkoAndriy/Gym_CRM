package com.epam.java.specialization.trainer_workload.service.interfaces;

import com.epam.java.specialization.trainer_workload.dto.TrainerWorkloadRequestDto;
import com.epam.java.specialization.trainer_workload.dto.TrainerWorkloadResponseDto;

public interface TrainerWorkloadService {
    void processTrainingWorkload(TrainerWorkloadRequestDto request);
    TrainerWorkloadResponseDto getTrainerWorkload(String username, Integer year, Integer month);
}