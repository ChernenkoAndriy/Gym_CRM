package com.epam.java.specialization.gym_crm.storage;

import com.epam.java.specialization.gym_crm.model.AbstractEntity;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Storage {

    private final Map<Class<?>, EntityStorage<?, ?>> registry = new ConcurrentHashMap<>();

    @Autowired
    public void setStorages(List<EntityStorage<? extends AbstractEntity<Long>, Long>> storages) {
        for (var storage : storages) {
            registry.put(storage.getEntityClass(), storage);
        }
    }

    public EntityStorage<?, ?> getStorageByJsonKey(String jsonKey) {
        return registry.values().stream()
                .filter(storage -> jsonKey.equalsIgnoreCase(storage.getJsonStorageKey()))
                .findFirst()
                .orElse(null);
    }

    public <T extends AbstractEntity<Long>> EntityStorage<T, Long> getStorage(Class<T> entityClass) {
        EntityStorage<T, Long> storage = (EntityStorage<T, Long>) registry.get(entityClass);
        if (storage == null) {
            throw new IllegalArgumentException("Storage for entity class " + entityClass.getSimpleName() + " is not registered!");
        }
        return storage;
    }

    public Map<Long, Trainee> getTrainees() {
        return getStorage(Trainee.class).asMap();
    }

    public Map<Long, Trainer> getTrainers() {
        return getStorage(Trainer.class).asMap();
    }

    public Map<Long, Training> getTrainings() {
        return getStorage(Training.class).asMap();
    }
}