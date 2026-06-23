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
import java.util.List;
import java.util.Map;

@Component
public class StorageInitializer implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(StorageInitializer.class);

    @Value("${storage.init.file-path}")
    private Resource initFile;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Storage storage) {
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

        for (Map.Entry<String, List<Object>> entry : dataWrapper.getData().entrySet()) {
            String jsonKey = entry.getKey();
            List<Object> rawList = entry.getValue();

            EntityStorage<?, ?> entityStorage = storage.getStorageByJsonKey(jsonKey);

            if (entityStorage != null) {
                entityStorage.initializeFromRawList(rawList, objectMapper);
                logger.info("Successfully loaded {} entities into storage via key '{}'", rawList.size(), jsonKey);
            } else {
                logger.warn("No storage registered for JSON key '{}'. Skipping data.", jsonKey);
            }
        }
    }
}