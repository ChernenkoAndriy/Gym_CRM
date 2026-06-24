package com.epam.java.specialization.gym_crm.dao.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.IBaseDao;
import com.epam.java.specialization.gym_crm.model.AbstractEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractMapDao<T extends AbstractEntity<Long>> implements IBaseDao<T, Long> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected Map<Long, T> storage;

    public void setStorage(Map<Long, T> storage) {
        this.storage = storage;
    }

    @Override
    public T create(T entity) {
        if (entity.getId() == null) {
            long nextId = storage.keySet().stream().max(Long::compare).orElse(0L) + 1;
            entity.setId(nextId);
        }
        storage.put(entity.getId(), entity);
        logger.info("Successfully created entity in storage with ID: {}", entity.getId());
        return entity;
    }

    @Override
    public T update(T entity) {
        if (entity.getId() != null && storage.containsKey(entity.getId())) {
            storage.put(entity.getId(), entity);
            logger.info("Successfully updated entity in storage with ID: {}", entity.getId());
            return entity;
        }
        logger.error("Failed to update entity. Entity with ID {} not found", entity.getId());
        return null;
    }

    @Override
    public void delete(Long id) {
        if (storage.containsKey(id)) {
            storage.remove(id);
            logger.info("Successfully deleted entity from storage with ID: {}", id);
        } else {
            logger.warn("Attempted to delete non-existing entity with ID: {}", id);
        }
    }

    @Override
    public Optional<T> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }
}