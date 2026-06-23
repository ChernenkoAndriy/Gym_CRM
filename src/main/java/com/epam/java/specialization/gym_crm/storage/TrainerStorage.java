package com.epam.java.specialization.gym_crm.storage;

import com.epam.java.specialization.gym_crm.model.Trainer;
import org.springframework.stereotype.Component;

@Component("trainerStorage")
public class TrainerStorage extends UserEntityStorage<Trainer> {

    @Override
    public Class<Trainer> getEntityClass() {
        return Trainer.class;
    }

    @Override
    public String getJsonStorageKey() { return "trainers"; }
}