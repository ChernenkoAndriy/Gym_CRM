package com.epam.java.specialization.gym_crm.service.interfaces;

import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Training;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ITraineeService extends ICRService<Trainee, Long>, IUpdateService<Trainee>, IDeleteService<Long> {
    Optional<Trainee> getByUsername(String username);
    void deleteByUsername(String username);
    void toggleActivation(String username, boolean isActive);
    List<Training> getTrainingsByCriteria(String username, Date fromDate, Date toDate, String trainerName, String trainingType);
    void updateTrainersList(String traineeUsername, List<String> trainerUsernames);
    boolean authenticate(String username, String password);
    void changePassword(String username, String newPassword);
}