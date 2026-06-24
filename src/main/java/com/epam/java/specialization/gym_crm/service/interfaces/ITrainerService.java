package com.epam.java.specialization.gym_crm.service.interfaces;

import com.epam.java.specialization.gym_crm.model.Trainer;

public interface ITrainerService extends
        ICRService<Trainer, Long>,
        IUpdateService<Trainer> {
}