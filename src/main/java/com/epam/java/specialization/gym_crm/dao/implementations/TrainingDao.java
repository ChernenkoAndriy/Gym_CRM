package com.epam.java.specialization.gym_crm.dao.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingDao;
import com.epam.java.specialization.gym_crm.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import java.util.Map;

@Repository
public class TrainingDao extends AbstractMapDao<Training> implements ITrainingDao {

    @Autowired
    @Qualifier("trainingStorage")
    @Override
    public void setStorage(Map<Long, Training> storage) {
        super.setStorage(storage);
    }
}