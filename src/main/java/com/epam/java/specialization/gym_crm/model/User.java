package com.epam.java.specialization.gym_crm.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public abstract class User extends AbstractEntity<Long> {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean isActive;
}