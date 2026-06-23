package com.epam.java.specialization.gym_crm.storage;

import com.epam.java.specialization.gym_crm.model.Trainee;
import org.springframework.stereotype.Component;

@Component("traineeStorage")
public class TraineeStorage extends UserEntityStorage<Trainee> {

    @Override
    public Class<Trainee> getEntityClass() {
        return Trainee.class;
    }

    @Override
    public String getJsonStorageKey() { return "trainees"; }
}