package com.epam.java.specialization.gym_crm.dao.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingTypeDao;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import org.springframework.stereotype.Repository;

@Repository
public class TrainingTypeDao extends AbstractJpaDao<TrainingType> implements ITrainingTypeDao {

    public TrainingTypeDao() {
        super(TrainingType.class);
    }
}