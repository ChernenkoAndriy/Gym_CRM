package com.epam.java.specialization.gym_crm.service.interfaces;

import com.epam.java.specialization.gym_crm.model.Trainee;

public interface ITraineeService extends
        ICRService<Trainee, Long>,
        IUpdateService<Trainee>,
        IDeleteService<Long> {
}