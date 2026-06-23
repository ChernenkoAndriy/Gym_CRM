package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.model.Trainer;
import org.springframework.stereotype.Repository;

@Repository
public class TrainerDao extends AbstractDao<Trainer> {

    @Override
    protected Class<Trainer> getEntityClass() {
        return Trainer.class;
    }
}