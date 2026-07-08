package com.epam.java.specialization.gym_crm.dao.intefaces;

import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Training;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ITraineeDao extends IBaseDao<Trainee, Long> {
    Optional<Trainee> findByUsername(String username);
    List<Training> findTrainingsByCriteria(String username, Date fromDate, Date toDate, String trainerName, String trainingType);
}