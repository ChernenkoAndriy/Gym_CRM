package com.epam.java.specialization.gym_crm;

import com.epam.java.specialization.gym_crm.config.ApplicationConfig;
import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.facade.GymFacade;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingService;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingTypeService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class GymCrmApplication {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);
        GymFacade facade = context.getBean(GymFacade.class);
        ITrainingTypeService typeService = context.getBean(ITrainingTypeService.class);
        ITrainingService trainingService = context.getBean(ITrainingService.class);

        try {
            System.out.println("=== STARTING INTEGRATION SCENARIO ===");

            
            TrainingType yogaType = typeService.create(TrainingType.builder().trainingTypeName("Yoga").build());
            TrainingType fitnessType = typeService.create(TrainingType.builder().trainingTypeName("Fitness").build());
            System.out.println("Initialized Training Types: Yoga ID=" + yogaType.getId() + ", Fitness ID=" + fitnessType.getId());

            
            TraineeCreateDto traineeDto = TraineeCreateDto.builder()
                    .firstName("Andriy")
                    .lastName("Chernencko")
                    .dateOfBirth(new Date())
                    .address("Kyiv")
                    .build();
            TraineeResponseDto traineeRes = facade.createTrainee(traineeDto);
            String traineeUsername = traineeRes.getUsername();

            
            String traineePassword = context.getBean(com.epam.java.specialization.gym_crm.service.interfaces.ITraineeService.class)
                    .getByUsername(traineeUsername)
                    .orElseThrow(() -> new IllegalStateException("Trainee not found"))
                    .getUser()
                    .getPassword();

            System.out.println("CREATED TRAINEE: Username=" + traineeUsername + " | Password=" + traineePassword);

            
            System.out.println("\n--- Testing Authentication ---");
            try {
                facade.getTraineeByUsername(traineeUsername, traineeUsername, "wrong_password");
                System.out.println("ERROR: Auth should have failed!");
            } catch (SecurityException e) {
                System.out.println("SUCCESS: Caught expected SecurityException for invalid password: " + e.getMessage());
            }

            try {
                Optional<TraineeResponseDto> authProfile = facade.getTraineeByUsername(traineeUsername, traineeUsername, traineePassword);
                System.out.println("SUCCESS: Authenticated successfully! Profile First Name: " + authProfile.get().getFirstName());
            } catch (SecurityException e) {
                System.out.println("ERROR: Valid credentials rejected!");
            }

            
            TrainerCreateDto trainerDto = TrainerCreateDto.builder()
                    .firstName("Ivan")
                    .lastName("Prokopchyk")
                    .trainingTypeId(yogaType.getId())
                    .build();
            TrainerResponseDto trainerRes = facade.createTrainer(trainerDto);
            String trainerUsername = trainerRes.getUsername();
            System.out.println("\nCREATED TRAINER: Username=" + trainerUsername + " | Specialization=" + trainerRes.getSpecialization().getTrainingTypeName());

            
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

            
            System.out.println("\n--- Testing Criteria API Search ---");
            List<TrainingResponseDto> filteredTrainings = facade.getTraineeTrainings(
                    traineeUsername, null, null, trainerUsername, "Yoga", traineeUsername, traineePassword
            );
            System.out.println("Found trainings by criteria matching criteria (Trainer, Yoga Type): " + filteredTrainings.size());

            
            System.out.println("\n--- Testing Hard Delete & Cascade Termination ---");
            facade.deleteTrainee(traineeUsername, traineeUsername, traineePassword);
            System.out.println("Trainee record with username '" + traineeUsername + "' deleted.");

            boolean trainingExists = trainingService.getById(trainingId).isPresent();
            if (!trainingExists) {
                System.out.println("SUCCESS: Training record with ID " + trainingId + " was automatically deleted via cascade option.");
            } else {
                System.out.println("ERROR: Training record still exists in database! Cascade delete failed.");
            }

            System.out.println("\n=== INTEGRATION SCENARIO COMPLETE: ALL JPA LAYERS ARE FUNCTIONAL ===");

        } catch (Exception e) {
            System.err.println("CRITICAL: Integration script execution aborted due to unexpected error:");
            e.printStackTrace();
        } finally {
            context.close();
        }
    }
}