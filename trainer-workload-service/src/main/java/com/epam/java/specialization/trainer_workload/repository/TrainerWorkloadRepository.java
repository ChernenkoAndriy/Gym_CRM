package com.epam.java.specialization.trainer_workload.repository;

import com.epam.java.specialization.trainer_workload.model.TrainerWorkload;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkload, String> {

    Optional<TrainerWorkload> findByUsername(String username);

    List<TrainerWorkload> findByFirstNameAndLastName(String firstName, String lastName);

    void deleteByUsername(String username);

    boolean existsByUsername(String username);
}