package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dto.ChangeLoginRequestDto;
import com.epam.java.specialization.gym_crm.exception.BadCredentialsException;
import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.repository.TraineeRepository;
import com.epam.java.specialization.gym_crm.repository.TrainerRepository;
import com.epam.java.specialization.gym_crm.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    @Override
    public void login(String username, String password) {
        
        
    }

    @Override
    @Transactional
    public void changeLogin(ChangeLoginRequestDto request) {
        User user = null;

        
        var traineeOpt = traineeRepository.findByUserUsername(request.getUsername());
        if (traineeOpt.isPresent()) {
            user = traineeOpt.get().getUser();
        } else {
            
            var trainerOpt = trainerRepository.findByUserUsername(request.getUsername());
            if (trainerOpt.isPresent()) {
                user = trainerOpt.get().getUser();
            }
        }

        
        if (user == null) {
            throw new EntityNotFoundException("User not found with username: " + request.getUsername());
        }

        
        if (!user.getPassword().equals(request.getOldPassword())) {
            throw new BadCredentialsException("Invalid old password for user: " + request.getUsername());
        }

        
        user.setPassword(request.getNewPassword());

        
    }
}