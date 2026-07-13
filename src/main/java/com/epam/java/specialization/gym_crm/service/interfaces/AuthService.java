package com.epam.java.specialization.gym_crm.service.interfaces;

import com.epam.java.specialization.gym_crm.dto.ChangeLoginRequestDto;

public interface AuthService {

    
    void login(String username, String password);

    
    void changeLogin(ChangeLoginRequestDto request);
}