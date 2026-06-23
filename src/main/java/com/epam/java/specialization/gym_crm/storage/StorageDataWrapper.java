package com.epam.java.specialization.gym_crm.storage;

import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import lombok.Data;
import java.util.List;

@Data
public class StorageDataWrapper {
    private List<Trainee> trainees;
    private List<Trainer> trainers;
}