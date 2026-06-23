package com.epam.java.specialization.gym_crm.storage;

import com.epam.java.specialization.gym_crm.model.AbstractEntity;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EntityStorage<T extends AbstractEntity<ID>, ID> {
    public abstract void initializeData(List<T> entities);
    public abstract Class<T> getEntityClass();
    public abstract String getJsonStorageKey();

    private final Map<ID, T> map = new ConcurrentHashMap<>();

    public Map<ID, T> asMap() {
        return map;
    }

    public T get(ID id) {
        return map.get(id);
    }

    public void put(ID id, T entity) {
        map.put(id, entity);
    }

    public void remove(ID id) {
        map.remove(id);
    }

    public Collection<T> getAll() {
        return map.values();
    }

    public void initializeFromRawList(List<Object> rawEntities, com.fasterxml.jackson.databind.ObjectMapper mapper) {
        if (rawEntities == null) return;

        List<T> entities = rawEntities.stream()
                .map(raw -> mapper.convertValue(raw, getEntityClass()))
                .toList();

        initializeData(entities);
    }
}