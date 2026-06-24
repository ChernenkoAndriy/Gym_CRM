package com.epam.java.specialization.gym_crm.service.interfaces;

import java.util.Optional;

public interface ICRService<T, ID> {
    T create(T entity);
    Optional<T> getById(ID id);
}
