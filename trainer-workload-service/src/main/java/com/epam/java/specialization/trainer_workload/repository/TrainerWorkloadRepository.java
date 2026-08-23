package com.epam.java.specialization.trainer_workload.repository;

import com.epam.java.specialization.trainer_workload.model.TrainerWorkload;

import java.util.Optional;

public interface TrainerWorkloadRepository {
    Optional<TrainerWorkload> findByUsername(String username);
    TrainerWorkload save(TrainerWorkload trainerWorkload);
    boolean existsByUsername(String username);
    void deleteByUsername(String username);
    void clear();
}