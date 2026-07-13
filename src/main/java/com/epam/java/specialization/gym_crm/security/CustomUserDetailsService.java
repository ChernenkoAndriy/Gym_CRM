package com.epam.java.specialization.gym_crm.security;

import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.repository.TraineeRepository;
import com.epam.java.specialization.gym_crm.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        var traineeOpt = traineeRepository.findByUserUsername(username);
        if (traineeOpt.isPresent()) {
            User user = traineeOpt.get().getUser();
            return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .disabled(!user.getIsActive()) 
                    .roles("TRAINEE")
                    .build();
        }

        
        var trainerOpt = trainerRepository.findByUserUsername(username);
        if (trainerOpt.isPresent()) {
            User user = trainerOpt.get().getUser();
            return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .disabled(!user.getIsActive()) 
                    .roles("TRAINER")
                    .build();
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}