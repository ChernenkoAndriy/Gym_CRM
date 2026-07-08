package com.epam.java.specialization.gym_crm.dao.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingDao;
import com.epam.java.specialization.gym_crm.model.Training;
import org.springframework.stereotype.Repository;

@Repository
public class TrainingDao extends AbstractJpaDao<Training> implements ITrainingDao {

    public TrainingDao() {
        super(Training.class);
    }
}