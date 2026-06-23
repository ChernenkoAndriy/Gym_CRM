package com.epam.java.specialization.gym_crm.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public abstract class AbstractEntity<ID> {
    private ID id;
}