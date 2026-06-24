package com.epam.java.specialization.gym_crm.mapper.interfaces;

public interface ICreateMapper<CD, E> {
    E toEntityFromCreate(CD createDto);
}
