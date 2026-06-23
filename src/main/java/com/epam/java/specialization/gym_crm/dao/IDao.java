package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.model.AbstractEntity;
import java.util.List;
import java.util.Optional;

public interface IDao<T extends AbstractEntity<Long>> {
    T save(T entity);
    Optional<T> findById(Long id);
    List<T> findAll();
}