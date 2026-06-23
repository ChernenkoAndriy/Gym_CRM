package com.epam.java.specialization.gym_crm.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

@Component
public class StorageInitializer implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(StorageInitializer.class);

    @Value("${storage.init.file-path}")
    private Resource initFile;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Storage) {
            Storage storage = (Storage) bean;
            logger.info("BeanPostProcessor: Start of loading initial data from JSON file... {}", initFile.getFilename());
            try {
                loadData(storage);
            } catch (Exception e) {
                logger.error("BeanPostProcessor: Error during parsing JSON initial data", e);
                throw new IllegalStateException("Couldn`t initialize Storage data from JSON", e);
            }
        }
        return bean;
    }

    private void loadData(Storage storage) throws Exception {
        if (!initFile.exists()) {
            logger.warn("Configuration file {} was not found", initFile.getFilename());
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        StorageDataWrapper dataWrapper = objectMapper.readValue(
                initFile.getInputStream(),
                StorageDataWrapper.class
        );

        if (dataWrapper.getTrainees() != null) {
            for (var trainee : dataWrapper.getTrainees()) {
                if (trainee.getUsername() == null) {
                    trainee.setUsername(trainee.getFirstName() + "." + trainee.getLastName());
                }
                if (trainee.getPassword() == null) {
                    trainee.setPassword("defaultPass");
                }
                storage.getTrainees().put(trainee.getId(), trainee);
                logger.debug("Trainee initialized from JSON: {} {}", trainee.getFirstName(), trainee.getLastName());
            }
        }

        if (dataWrapper.getTrainers() != null) {
            for (var trainer : dataWrapper.getTrainers()) {
                if (trainer.getUsername() == null) {
                    trainer.setUsername(trainer.getFirstName() + "." + trainer.getLastName());
                }
                if (trainer.getPassword() == null) {
                    trainer.setPassword("defaultPass");
                }
                storage.getTrainers().put(trainer.getId(), trainer);
                logger.debug("Trainer initialized from JSON: {} {}", trainer.getFirstName(), trainer.getLastName());
            }
        }

        logger.info("Storage has been successfully initialized from JSON. \n   Trainee count: {} \n   Trainers count: {}",
                storage.getTrainees().size(), storage.getTrainers().size());
    }
}