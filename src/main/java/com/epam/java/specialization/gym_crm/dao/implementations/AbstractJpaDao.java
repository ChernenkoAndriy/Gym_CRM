package com.epam.java.specialization.gym_crm.dao.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.IBaseDao;
import com.epam.java.specialization.gym_crm.model.AbstractEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

public abstract class AbstractJpaDao<T extends AbstractEntity<Long>> implements IBaseDao<T, Long> {

    @PersistenceContext
    protected EntityManager entityManager;

    protected final Class<T> entityClass;

    protected AbstractJpaDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public T create(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Override
    public T update(T entity) {
        return entityManager.merge(entity);
    }

    @Override
    public void delete(Long id) {
        T entity = entityManager.find(entityClass, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public Optional<T> findById(Long id) {
        return Optional.ofNullable(entityManager.find(entityClass, id));
    }

    @Override
    public List<T> findAll() {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        return entityManager.createQuery(jpql, entityClass).getResultList();
    }
}