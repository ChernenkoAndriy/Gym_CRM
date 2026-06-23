package com.epam.java.specialization.gym_crm.storage;

import com.epam.java.specialization.gym_crm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public abstract class UserEntityStorage<T extends User> extends EntityStorage<T, Long> {

    private static final Logger logger = LoggerFactory.getLogger(UserEntityStorage.class);

    @Override
    public void initializeData(List<T> entities) {
        if (entities == null) return;

        for (T user : entities) {

            if (user.getUsername() == null) {
                user.setUsername(user.getFirstName() + "." + user.getLastName());
            }

            if (user.getPassword() == null) {
                user.setPassword("defaultPass");
            }

            put(user.getId(), user);
            logger.debug("{} storage: Inline initialized profile for {} {}",
                    user.getClass().getSimpleName(), user.getFirstName(), user.getLastName());
        }
    }
}