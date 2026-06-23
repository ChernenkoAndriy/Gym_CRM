package com.epam.java.specialization.gym_crm.storage;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class StorageDataWrapper {

    private Map<String, List<Object>> data = new HashMap<>();

    @JsonAnySetter
    public void anySetter(String key, List<Object> value) {
        data.put(key, value);
    }
}