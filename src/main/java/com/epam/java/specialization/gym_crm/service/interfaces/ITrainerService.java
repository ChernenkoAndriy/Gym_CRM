package com.epam.java.specialization.gym_crm.service.interfaces;

import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import java.util.Date;
import java.util.List;

public interface ITrainerService extends ICRService<Trainer, Long>, IUpdateService<Trainer> {
    List<Training> getTrainingsByCriteria(String username, Date fromDate, Date toDate, String traineeName);
    List<Trainer> getAvailableTrainersNotAssignedToTrainee(String traineeUsername);
}