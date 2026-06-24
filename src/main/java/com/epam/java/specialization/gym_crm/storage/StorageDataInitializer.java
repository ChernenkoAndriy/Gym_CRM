package com.epam.java.specialization.gym_crm.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class StorageDataInitializer implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(StorageDataInitializer.class);

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    @Value("${storage.init.file-path}")
    private String filePath;

    public StorageDataInitializer(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Storage) {
            Storage storage = (Storage) bean;
            logger.info("BeanPostProcessor triggered for Storage. Loading data from path: {}", filePath);

            try {
                Resource resource = resourceLoader.getResource(filePath);
                try (InputStream inputStream = resource.getInputStream()) {
                    StorageDto data = objectMapper.readValue(inputStream, StorageDto.class);

                    populateMap(data.getTrainees(), storage.getTraineeMap());
                    populateMap(data.getTrainers(), storage.getTrainerMap());
                    populateMap(data.getTrainings(), storage.getTrainingMap());

                    logger.info("Successfully pre-populated Storage: {} trainees, {} trainers, {} trainings loaded.",
                            storage.getTraineeMap().size(), storage.getTrainerMap().size(), storage.getTrainingMap().size());
                }
            } catch (Exception e) {
                logger.error("Failed to initialize storage with prepared data from file: {}", filePath, e);
            }
        }
        return bean;
    }

    private <E extends com.epam.java.specialization.gym_crm.model.AbstractEntity<Long>> void populateMap(List<E> list, Map<Long, E> targetMap) {
        if (list != null) {
            list.forEach(entity -> targetMap.put(entity.getId(), entity));
        }
    }
}