package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.model.AbstractEntity;
import com.epam.java.specialization.gym_crm.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractDao<T extends AbstractEntity<Long>> implements IDao<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected Storage storage;

    @Autowired
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    protected abstract Class<T> getEntityClass();

    protected Map<Long, T> getStorageMap() {
        return storage.getStorage(getEntityClass()).asMap();
    }

    @Override
    public T save(T entity) {
        logger.info("DAO: Saving/Updating {} with ID: {}", getEntityClass().getSimpleName(), entity.getId());
        getStorageMap().put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(Long id) {
        logger.info("DAO: Finding {} by ID: {}", getEntityClass().getSimpleName(), id);
        return Optional.ofNullable(getStorageMap().get(id));
    }

    @Override
    public List<T> findAll() {
        logger.info("DAO: Fetching all {}s", getEntityClass().getSimpleName());
        return new ArrayList<>(getStorageMap().values());
    }
}