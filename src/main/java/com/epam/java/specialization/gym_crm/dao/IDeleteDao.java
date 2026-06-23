package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.model.AbstractEntity;

public interface IDeleteDao<T extends AbstractEntity<Long>> extends IDao<T> {
    void deleteById(Long id);
}