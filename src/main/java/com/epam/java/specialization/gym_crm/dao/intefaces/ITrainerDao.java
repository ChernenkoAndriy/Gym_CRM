package com.epam.java.specialization.gym_crm.dao.intefaces;

import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ITrainerDao extends IBaseDao<Trainer, Long> {
    Optional<Trainer> findByUsername(String username);
    List<Trainer> findAvailableTrainersNotAssignedToTrainee(String traineeUsername);
    List<Training> findTrainingsByCriteria(String username, Date fromDate, Date toDate, String traineeName);
}