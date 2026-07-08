package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractUserService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @PersistenceContext
    protected EntityManager entityManager;

    @Transactional
    protected synchronized void prepareUserProfile(User user) {
        String baseUsername = user.getFirstName() + "." + user.getLastName();

        List<String> existingUsernames = entityManager.createQuery(
                        "SELECT u.username FROM User u WHERE u.username = :base OR u.username LIKE :pattern", String.class)
                .setParameter("base", baseUsername)
                .setParameter("pattern", baseUsername + "%")
                .getResultList();

        String finalUsername = baseUsername;
        if (!existingUsernames.isEmpty()) {
            Set<String> usernamesSet = new HashSet<>(existingUsernames);
            if (usernamesSet.contains(baseUsername)) {
                int suffix = 1;
                while (true) {
                    String candidateUsername = baseUsername + suffix;
                    if (!usernamesSet.contains(candidateUsername)) {
                        finalUsername = candidateUsername;
                        break;
                    }
                    suffix++;
                }
            }
        }

        user.setUsername(finalUsername);
        user.setPassword(generateRandomPassword());
        logger.info("Generated profile for user: username={}, password=[PROTECTED]", finalUsername);
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        logger.debug("Attempting authentication for username: {}", username);
        List<Long> count = entityManager.createQuery(
                        "SELECT count(u) FROM User u WHERE u.username = :username AND u.password = :password", Long.class)
                .setParameter("username", username)
                .setParameter("password", password)
                .getResultList();

        boolean success = count != null && !count.isEmpty() && count.get(0) > 0;
        if (success) {
            logger.info("Authentication successful for username: {}", username);
        } else {
            logger.warn("Authentication failed for username: {}", username);
        }
        return success;
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        logger.info("Changing password for username: {}", username);
        int updatedRows = entityManager.createQuery(
                        "UPDATE User u SET u.password = :newPassword WHERE u.username = :username")
                .setParameter("newPassword", newPassword)
                .setParameter("username", username)
                .executeUpdate();

        if (updatedRows == 0) {
            logger.error("Failed to change password. User with username {} not found", username);
            throw new IllegalArgumentException("User not found: " + username);
        }
        logger.info("Password successfully updated for username: {}", username);
    }

    private String generateRandomPassword() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}