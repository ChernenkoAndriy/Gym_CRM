package com.epam.java.specialization.gym_crm.storage;

import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Storage {

    private Map<Long, Trainee> trainees;
    private Map<Long, Trainer> trainers;
    private Map<Long, Training> trainings;

    @Autowired
    @Qualifier("traineeStorage")
    public void setTrainees(Map<Long, Trainee> trainees) {
        this.trainees = trainees;
    }

    @Autowired
    @Qualifier("trainerStorage")
    public void setTrainers(Map<Long, Trainer> trainers) {
        this.trainers = trainers;
    }

    @Autowired
    @Qualifier("trainingStorage")
    public void setTrainings(Map<Long, Training> trainings) {
        this.trainings = trainings;
    }

    public Map<Long, Trainee> getTrainees() {
        return trainees;
    }

    public Map<Long, Trainer> getTrainers() {
        return trainers;
    }

    public Map<Long, Training> getTrainings() {
        return trainings;
    }
}