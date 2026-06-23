package com.epam.java.specialization.gym_crm.facade;

import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.service.TraineeService;
import com.epam.java.specialization.gym_crm.service.TrainerService;
import com.epam.java.specialization.gym_crm.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GymFacade {

    private static final Logger logger = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public Trainee createTrainee(Trainee trainee) {
        logger.info("Facade: Request to create Trainee profile");
        return traineeService.createTrainee(trainee);
    }

    public Trainee updateTrainee(Trainee trainee) {
        logger.info("Facade: Request to update Trainee profile with ID: {}", trainee.getId());
        return traineeService.updateTrainee(trainee);
    }

    public void deleteTrainee(Long id) {
        logger.info("Facade: Request to delete Trainee profile with ID: {}", id);
        traineeService.deleteTrainee(id);
    }

    public Optional<Trainee> getTraineeById(Long id) {
        logger.info("Facade: Request to select Trainee profile by ID: {}", id);
        return traineeService.getTraineeById(id);
    }

    public List<Trainee> getAllTrainees() {
        return traineeService.getAllTrainees();
    }

    public Trainer createTrainer(Trainer trainer) {
        logger.info("Facade: Request to create Trainer profile");
        return trainerService.createTrainer(trainer);
    }

    public Trainer updateTrainer(Trainer trainer) {
        logger.info("Facade: Request to update Trainer profile with ID: {}", trainer.getId());
        return trainerService.updateTrainer(trainer);
    }

    public Optional<Trainer> getTrainerById(Long id) {
        logger.info("Facade: Request to select Trainer profile by ID: {}", id);
        return trainerService.getTrainerById(id);
    }

    public List<Trainer> getAllTrainers() {
        return trainerService.getAllTrainers();
    }

    public Training createTraining(Training training) {
        logger.info("Facade: Request to create Training");
        return trainingService.createTraining(training);
    }

    public Optional<Training> getTrainingById(Long id) {
        logger.info("Facade: Request to select Training by ID: {}", id);
        return trainingService.getTrainingById(id);
    }

    public List<Training> getAllTrainings() {
        return trainingService.getAllTrainings();
    }
}