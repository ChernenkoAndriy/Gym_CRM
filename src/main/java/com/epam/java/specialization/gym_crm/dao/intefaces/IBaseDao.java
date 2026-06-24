package com.epam.java.specialization.gym_crm.dao.intefaces;

import java.util.List;
import java.util.Optional;

public interface IBaseDao<T, ID> {
    T create(T entity);
    T update(T entity);
    void delete(ID id);
    Optional<T> findById(ID id);
    List<T> findAll();
}