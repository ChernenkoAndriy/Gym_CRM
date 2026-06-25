package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractUserService<T extends User> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    protected void prepareUserProfile(T user, List<? extends User> existingUsers) {
        String baseUsername = user.getFirstName() + "." + user.getLastName();

        Set<String> takenUsernames = existingUsers.stream()
                .map(User::getUsername)
                .filter(username -> username != null)
                .collect(Collectors.toSet());

        String finalUsername = baseUsername;

        if (takenUsernames.contains(baseUsername)) {
            int suffix = 1;
            while (takenUsernames.contains(baseUsername + suffix)) {
                suffix++;
            }
            finalUsername = baseUsername + suffix;
        }

        user.setUsername(finalUsername);
        user.setPassword(generateRandomPassword());
        logger.info("Generated profile for user: username={}, password=[PROTECTED]", finalUsername);
    }

    private String generateRandomPassword() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}