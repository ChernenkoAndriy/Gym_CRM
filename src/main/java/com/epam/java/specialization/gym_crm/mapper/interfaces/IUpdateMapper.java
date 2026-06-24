package com.epam.java.specialization.gym_crm.mapper.interfaces;

public interface IUpdateMapper<UD, E> {
    E toEntityFromUpdate(UD updateDto);
}