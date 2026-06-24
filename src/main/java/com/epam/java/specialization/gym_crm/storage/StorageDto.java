package com.epam.java.specialization.gym_crm.storage;

import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import lombok.Data;
import java.util.List;

@Data
public class StorageDto {
    private List<Trainee> trainees;
    private List<Trainer> trainers;
    private List<Training> trainings;
}