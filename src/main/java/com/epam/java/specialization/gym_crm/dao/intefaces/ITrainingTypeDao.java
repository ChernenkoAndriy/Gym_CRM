package com.epam.java.specialization.gym_crm.dao.intefaces;

import com.epam.java.specialization.gym_crm.model.TrainingType;
import java.util.Optional;

public interface ITrainingTypeDao extends IBaseDao<TrainingType, Long> {
    Optional<TrainingType> findByName(String name);
}