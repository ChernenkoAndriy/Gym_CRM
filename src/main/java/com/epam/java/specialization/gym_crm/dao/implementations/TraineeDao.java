package com.epam.java.specialization.gym_crm.dao.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.model.Trainee;
import org.springframework.stereotype.Repository;

@Repository
public class TraineeDao extends AbstractJpaDao<Trainee> implements ITraineeDao {

    public TraineeDao() {
        super(Trainee.class);
    }
}