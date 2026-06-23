package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.model.Training;
import org.springframework.stereotype.Repository;

@Repository
public class TrainingDao extends AbstractDao<Training> {

    @Override
    protected Class<Training> getEntityClass() {
        return Training.class;
    }
}