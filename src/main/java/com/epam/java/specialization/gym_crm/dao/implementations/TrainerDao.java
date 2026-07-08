package com.epam.java.specialization.gym_crm.dao.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainerDao;
import com.epam.java.specialization.gym_crm.model.Trainer;
import org.springframework.stereotype.Repository;

@Repository
public class TrainerDao extends AbstractJpaDao<Trainer> implements ITrainerDao {

    public TrainerDao() {
        super(Trainer.class);
    }
}