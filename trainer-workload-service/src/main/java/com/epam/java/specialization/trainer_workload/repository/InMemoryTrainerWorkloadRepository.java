package com.epam.java.specialization.trainer_workload.repository;

import com.epam.java.specialization.trainer_workload.model.TrainerWorkload;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTrainerWorkloadRepository implements TrainerWorkloadRepository {

    private final Map<String, TrainerWorkload> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<TrainerWorkload> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(username.trim()));
    }

    @Override
    public TrainerWorkload save(TrainerWorkload trainerWorkload) {
        if (trainerWorkload != null && trainerWorkload.getUsername() != null) {
            storage.put(trainerWorkload.getUsername().trim(), trainerWorkload);
        }
        return trainerWorkload;
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null) {
            return false;
        }
        return storage.containsKey(username.trim());
    }

    @Override
    public void deleteByUsername(String username) {
        if (username != null) {
            storage.remove(username.trim());
        }
    }

    @Override
    public void clear() {
        storage.clear();
    }
}