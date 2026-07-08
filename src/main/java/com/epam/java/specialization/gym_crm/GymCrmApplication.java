package com.epam.java.specialization.gym_crm;

import com.epam.java.specialization.gym_crm.config.ApplicationConfig;
import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.facade.GymFacade;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainerDao;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingTypeService;
import com.epam.java.specialization.gym_crm.storage.StorageDataInitializer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class GymCrmApplication {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);
        try {
            StorageDataInitializer initializer = context.getBean(StorageDataInitializer.class);
            initializer.run();
        } catch (Exception e) {
            System.err.println("Failed to execute data initializer:");
            e.printStackTrace();
        }
        GymFacade facade = context.getBean(GymFacade.class);
        ITrainingTypeService typeService = context.getBean(ITrainingTypeService.class);
        ITraineeDao traineeDao = context.getBean(ITraineeDao.class);
        ITrainerDao trainerDao = context.getBean(ITrainerDao.class);
        try {
            System.out.println("=== STARTING INTEGRATION SCENARIO ===");
            TrainingType yogaType = typeService.create(TrainingType.builder().trainingTypeName("Yoga").build());
            TrainingType fitnessType = typeService.create(TrainingType.builder().trainingTypeName("Crossfit").build());
            System.out.println("Initialized Training Types: Yoga ID=" + yogaType.getId() + ", Fitness ID=" + fitnessType.getId());

            TraineeCreateDto traineeDto = TraineeCreateDto.builder()
                    .firstName("Andriy")
                    .lastName("Chernencko")
                    .dateOfBirth(new Date())
                    .address("Kyiv")
                    .build();
            TraineeResponseDto traineeRes = facade.createTrainee(traineeDto);
            String traineeUsername = traineeRes.getUsername();

            Trainee rawTrainee = traineeDao.findByUsername(traineeUsername)
                    .orElseThrow(() -> new IllegalStateException("Trainee not found in DB"));
            String traineePassword = rawTrainee.getUser().getPassword();
            System.out.println("CREATED TRAINEE: Username=" + traineeUsername + " | Password=" + traineePassword);

            TrainerCreateDto trainerDto = TrainerCreateDto.builder()
                    .firstName("Ivan")
                    .lastName("Prokopchyk")
                    .trainingTypeId(yogaType.getId())
                    .build();
            TrainerResponseDto trainerRes = facade.createTrainer(trainerDto);
            String trainerUsername = trainerRes.getUsername();

            Trainer rawTrainer = trainerDao.findByUsername(trainerUsername)
                    .orElseThrow(() -> new IllegalStateException("Trainer not found in DB"));
            String trainerPassword = rawTrainer.getUser().getPassword();
            System.out.println("CREATED TRAINER: Username=" + trainerUsername + " | Password=" + trainerPassword);

            System.out.println("\n--- Testing Trainee Authentication ---");
            try {
                Optional<TraineeResponseDto> authProfile = facade.getTraineeByUsername(traineeUsername, traineeUsername, traineePassword);
                System.out.println("SUCCESS: Trainee authenticated successfully! Profile First Name: " + authProfile.get().getFirstName());
            } catch (SecurityException e) {
                System.out.println("ERROR: Valid Trainee credentials rejected!");
            }

            System.out.println("\n--- Testing Trainer Authentication ---");
            try {
                List<TrainerResponseDto> availableTrainers = facade.getAvailableTrainers(traineeUsername, trainerUsername, trainerPassword);
                System.out.println("SUCCESS: Trainer authenticated successfully! Available trainers count: " + availableTrainers.size());
            } catch (SecurityException e) {
                System.out.println("ERROR: Valid Trainer credentials rejected in Facade!");
            }

            System.out.println("\n--- Testing Trainee Update ---");
            TraineeUpdateDto traineeUpdateDto = TraineeUpdateDto.builder()
                    .id(traineeRes.getId())
                    .firstName("Andriy")
                    .lastName("Chernencko")
                    .address("Kyiv Modern")
                    .dateOfBirth(new Date())
                    .isActive(true)
                    .build();
            TraineeResponseDto updatedTrainee = facade.updateTrainee(traineeUpdateDto, traineeUsername, traineePassword);
            System.out.println("SUCCESS: Trainee updated. Address: " + updatedTrainee.getAddress());

            System.out.println("\n--- Testing Trainer Update ---");
            TrainerUpdateDto trainerUpdateDto = TrainerUpdateDto.builder()
                    .id(trainerRes.getId())
                    .firstName("Ivan")
                    .lastName("Prokopchyk")
                    .trainingTypeId(fitnessType.getId())
                    .isActive(true)
                    .build();
            TrainerResponseDto updatedTrainer = facade.updateTrainer(trainerUpdateDto, trainerUsername, trainerPassword);
            System.out.println("SUCCESS: Trainer updated. New Specialization: " + updatedTrainer.getSpecialization().getTrainingTypeName());

            System.out.println("\n--- Testing Toggle Activation ---");
            facade.toggleTrainerActivation(trainerUsername, false, trainerUsername, trainerPassword);
            System.out.println("SUCCESS: Trainer status changed to false.");
            try {
                facade.toggleTrainerActivation(trainerUsername, false, trainerUsername, trainerPassword);
                System.out.println("ERROR: Non-idempotent action should have failed!");
            } catch (IllegalStateException e) {
                System.out.println("SUCCESS: Caught expected IllegalStateException for identical status: " + e.getMessage());
            }
            facade.toggleTrainerActivation(trainerUsername, true, trainerUsername, trainerPassword);
            System.out.println("SUCCESS: Trainer status restored to true.");

            TrainingCreateDto trainingDto = TrainingCreateDto.builder()
                    .traineeId(traineeRes.getId())
                    .trainerId(trainerRes.getId())
                    .trainingName("Power Yoga Morning")
                    .trainingTypeId(yogaType.getId())
                    .trainingDate(new Date())
                    .trainingDuration(90)
                    .build();
            TrainingResponseDto trainingRes = facade.createTraining(trainingDto, traineeUsername, traineePassword);
            Long trainingId = trainingRes.getId();
            System.out.println("\nCREATED TRAINING: ID=" + trainingId + " | Name=" + trainingRes.getTrainingName());

            System.out.println("\n--- Testing Trainee Trainings Retrieval with Pagination ---");
            List<TrainingResponseDto> traineeTrainings = facade.getTraineeTrainings(traineeUsername, null, null, null, null, traineeUsername, traineePassword);
            System.out.println("SUCCESS: Retrieved " + traineeTrainings.size() + " trainings for trainee.");

            System.out.println("\n--- Testing Trainer Trainings Retrieval with Pagination ---");
            List<TrainingResponseDto> trainerTrainings = facade.getTrainerTrainings(trainerUsername, null, null, null, trainerUsername, trainerPassword);
            System.out.println("SUCCESS: Retrieved " + trainerTrainings.size() + " trainings for trainer.");

            System.out.println("\n--- Testing Inactive User Training Constraint ---");
            facade.toggleTraineeActivation(traineeUsername, false, traineeUsername, traineePassword);
            System.out.println("Trainee is now deactivated.");
            try {
                facade.createTraining(trainingDto, trainerUsername, trainerPassword);
                System.out.println("ERROR: Training creation with inactive user should have failed!");
            } catch (IllegalArgumentException e) {
                System.out.println("SUCCESS: Caught expected IllegalArgumentException for inactive profile: " + e.getMessage());
            }
            facade.toggleTraineeActivation(traineeUsername, true, traineeUsername, traineePassword);
            System.out.println("Trainee is restored to active.");

            System.out.println("\n--- Testing Unsupported Training Type Creation ---");
            try {
                typeService.create(TrainingType.builder().trainingTypeName("Unmapped Type").build());
                System.out.println("ERROR: Adding a non-existent training type should have failed!");
            } catch (UnsupportedOperationException e) {
                System.out.println("SUCCESS: Caught expected UnsupportedOperationException for constant violation: " + e.getMessage());
            }

            System.out.println("\n--- Testing Hard Delete & Cascade Termination ---");
            facade.deleteTrainee(traineeUsername, traineeUsername, traineePassword);
            System.out.println("Trainee record with username '" + traineeUsername + "' deleted.");

            boolean trainingExists = context.getBean(com.epam.java.specialization.gym_crm.service.interfaces.ITrainingService.class).getById(trainingId).isPresent();
            if (!trainingExists) {
                System.out.println("SUCCESS: Training record with ID " + trainingId + " was automatically deleted via cascade option.");
            } else {
                System.out.println("ERROR: Training record still exists in database! Cascade delete failed.");
            }
            System.out.println("\n=== INTEGRATION SCENARIO COMPLETE: ALL UPDATED LAYERS ARE FUNCTIONAL ===");
        } catch (Exception e) {
            System.err.println("CRITICAL: Integration script execution aborted due to unexpected error:");
            e.printStackTrace();
        } finally {
            context.close();
        }
    }
}