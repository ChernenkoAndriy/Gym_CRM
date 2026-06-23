package com.epam.java.specialization.gym_crm.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Trainee extends User {
    private Date dateOfBirth;
    private String address;

    public Trainee(Long id, String firstName, String lastName, String username, String password, boolean isActive, Date dateOfBirth, String address) {
        super(id, firstName, lastName, username, password, isActive);
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }
}