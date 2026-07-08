package com.epam.java.specialization.gym_crm.storage;

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
import java.util.function.Function;

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

                List<Trainee> savedTrainees = loadAndPersistEntities(
                        rootNode.get("trainees"),
                        new TypeReference<List<Trainee>>() {},
                        trainee -> {
                            trainee.setId(null);
                            if (trainee.getUser() != null) {
                                trainee.getUser().setId(null);
                            }
                            return traineeService.create(trainee);
                        }
                );

                List<Trainer> savedTrainers = loadAndPersistEntities(
                        rootNode.get("trainers"),
                        new TypeReference<List<Trainer>>() {},
                        trainer -> {
                            trainer.setId(null);
                            if (trainer.getUser() != null) {
                                trainer.getUser().setId(null);
                            }
                            if (trainer.getSpecialization() != null) {
                                TrainingType constantType = trainingTypeService.create(
                                        TrainingType.builder()
                                                .trainingTypeName(trainer.getSpecialization().getTrainingTypeName())
                                                .build()
                                );
                                trainer.setSpecialization(constantType);
                            }
                            return trainerService.create(trainer);
                        }
                );

                processAndPersistTrainings(rootNode.get("trainings"), savedTrainees, savedTrainers);

                logger.info("Successfully populated relational database with initial data.");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize database with prepared data from file: {}", filePath, e);
            throw e;
        }
    }

    private <T> List<T> loadAndPersistEntities(JsonNode node, TypeReference<List<T>> typeReference, Function<T, T> persistFunction) {
        List<T> savedEntities = new ArrayList<>();
        if (node != null && node.isArray()) {
            List<T> entities = objectMapper.convertValue(node, typeReference);
            for (T entity : entities) {
                savedEntities.add(persistFunction.apply(entity));
            }
        }
        return savedEntities;
    }

    private void processAndPersistTrainings(JsonNode node, List<Trainee> savedTrainees, List<Trainer> savedTrainers) {
        if (node != null && node.isArray() && !savedTrainees.isEmpty() && !savedTrainers.isEmpty()) {
            List<Training> trainings = objectMapper.convertValue(node, new TypeReference<List<Training>>() {});
            for (int i = 0; i < trainings.size(); i++) {
                Training training = trainings.get(i);
                training.setId(null);
                training.setTrainee(savedTrainees.get(i % savedTrainees.size()));
                Trainer assignedTrainer = savedTrainers.get(i % savedTrainers.size());
                training.setTrainer(assignedTrainer);
                if (assignedTrainer != null && assignedTrainer.getSpecialization() != null) {
                    training.setTrainingType(assignedTrainer.getSpecialization());
                }
                trainingService.create(training);
            }
        }
    }
}