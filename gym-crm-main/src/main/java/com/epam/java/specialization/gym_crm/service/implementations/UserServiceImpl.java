package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.repository.UserRepository;
import com.epam.java.specialization.gym_crm.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public String prepareUserCredentials(User user) {
        log.info("Preparing user credentials for: {} {}", user.getFirstName(), user.getLastName());
        String baseUsername = user.getFirstName() + "." + user.getLastName();
        String finalUsername = generateUniqueUsername(baseUsername);
        String rawPassword = generateRandomPassword();

        user.setUsername(finalUsername);
        user.setPassword(passwordEncoder.encode(rawPassword));
        log.info("Generated unique username: {}", finalUsername);

        return rawPassword;
    }

    @Override
    @Transactional
    public void toggleActivation(String username, boolean isActive) {
        log.info("Attempting to toggle activation status for user: {} to isActive={}", username, isActive);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Toggle activation failed: User not found with username: {}", username);
                    return new EntityNotFoundException("User not found with username: " + username);
                });

        if (user.getIsActive().equals(isActive)) {
            log.warn("Toggle activation rejected: User {} already has isActive={}", username, isActive);
            throw new IllegalStateException("User profile active status is already " + isActive);
        }

        user.setIsActive(isActive);
        userRepository.save(user);
        log.info("User {} activation status successfully updated to isActive={}", username, isActive);
    }

    private String generateUniqueUsername(String baseUsername) {
        String candidate = baseUsername;
        int suffix = 1;

        while (userRepository.existsByUsername(candidate)) {
            log.debug("Username collision detected for candidate: {}. Incrementing suffix.", candidate);
            candidate = baseUsername + suffix;
            suffix++;
        }
        return candidate;
    }

    private String generateRandomPassword() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}