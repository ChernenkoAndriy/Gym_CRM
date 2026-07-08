package com.epam.java.specialization.gym_crm.storage;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.service.interfaces.ITraineeService;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainerService;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingService;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingTypeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class StorageDataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(StorageDataInitializer.class);
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final ITraineeService traineeService;
    private final ITrainerService trainerService;
    private final ITrainingService trainingService;
    private final ITrainingTypeService trainingTypeService;

    @Value("${storage.init.file-path}")
    private String filePath;

    public StorageDataInitializer(ResourceLoader resourceLoader,
                                  ITraineeService traineeService,
                                  ITrainerService trainerService,
                                  ITrainingService trainingService,
                                  ITrainingTypeService trainingTypeService) {
        this.resourceLoader = resourceLoader;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public void run() throws Exception {
        logger.info("Loading initial data from path: {}", filePath);
        try {
            Resource resource = resourceLoader.getResource(filePath);
            try (InputStream inputStream = resource.getInputStream()) {
                JsonNode rootNode = objectMapper.readTree(inputStream);
                List<TraineeResponseDto> savedTrainees = new ArrayList<>();
                JsonNode traineesNode = rootNode.get("trainees");
                if (traineesNode != null && traineesNode.isArray()) {
                    List<Trainee> trainees = objectMapper.convertValue(traineesNode, new TypeReference<List<Trainee>>() {});
                    for (Trainee trainee : trainees) {
                        TraineeCreateDto createDto = TraineeCreateDto.builder()
                                .firstName(trainee.getUser().getFirstName())
                                .lastName(trainee.getUser().getLastName())
                                .dateOfBirth(trainee.getDateOfBirth())
                                .address(trainee.getAddress())
                                .build();
                        TraineeResponseDto saved = traineeService.create(createDto);
                        savedTrainees.add(saved);
                    }
                }
                List<TrainerResponseDto> savedTrainers = new ArrayList<>();
                JsonNode trainersNode = rootNode.get("trainers");
                if (trainersNode != null && trainersNode.isArray()) {
                    List<Trainer> trainers = objectMapper.convertValue(trainersNode, new TypeReference<List<Trainer>>() {});
                    for (Trainer trainer : trainers) {
                        TrainingType constantType = trainingTypeService.create(
                                TrainingType.builder()
                                        .trainingTypeName(trainer.getSpecialization().getTrainingTypeName())
                                        .build()
                        );
                        TrainerCreateDto createDto = TrainerCreateDto.builder()
                                .firstName(trainer.getUser().getFirstName())
                                .lastName(trainer.getUser().getLastName())
                                .trainingTypeId(constantType.getId())
                                .build();
                        TrainerResponseDto saved = trainerService.create(createDto);
                        savedTrainers.add(saved);
                    }
                }
                JsonNode trainingsNode = rootNode.get("trainings");
                if (trainingsNode != null && trainingsNode.isArray() && !savedTrainees.isEmpty() && !savedTrainers.isEmpty()) {
                    List<Training> trainings = objectMapper.convertValue(trainingsNode, new TypeReference<List<Training>>() {});
                    for (int i = 0; i < trainings.size(); i++) {
                        Training training = trainings.get(i);
                        TraineeResponseDto assignedTrainee = savedTrainees.get(i % savedTrainees.size());
                        TrainerResponseDto assignedTrainer = savedTrainers.get(i % savedTrainers.size());
                        TrainingCreateDto createDto = TrainingCreateDto.builder()
                                .traineeId(assignedTrainee.getId())
                                .trainerId(assignedTrainer.getId())
                                .trainingName(training.getTrainingName())
                                .trainingTypeId(assignedTrainer.getSpecialization().getId())
                                .trainingDate(training.getTrainingDate())
                                .trainingDuration(training.getTrainingDuration())
                                .build();
                        trainingService.create(createDto);
                    }
                }
                logger.info("Successfully populated relational database with initial data.");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize database with prepared data from file: {}", filePath, e);
            throw e;
        }
    }
}