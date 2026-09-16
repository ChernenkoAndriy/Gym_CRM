package com.epam.java.specialization.gym_crm.cucumber;

import com.epam.java.specialization.gym_crm.repository.UserRepository;
import com.epam.java.specialization.gym_crm.service.implementations.TrainerWorkloadProducer;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

public class CucumberHooks {

    @Autowired
    private TestContext testContext;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TrainerWorkloadProducer workloadProducer;

    @Before
    public void setUp() {
        testContext.reset();
        doNothing().when(workloadProducer).sendWorkloadRequest(any());

        try {
            Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushDb();
        } catch (Exception ignored) {
        }

        userRepository.findByUsername("Trainee.Ten").ifPresent(user -> {
            user.setFirstName("Trainee");
            user.setLastName("Ten");
            user.setPassword(passwordEncoder.encode("staticPass1"));
            user.setIsActive(true);
            userRepository.saveAndFlush(user);
        });

        userRepository.findByUsername("Trainer.Ten").ifPresent(user -> {
            user.setFirstName("Trainer");
            user.setLastName("Ten");
            user.setPassword(passwordEncoder.encode("staticPass1"));
            user.setIsActive(true);
            userRepository.saveAndFlush(user);
        });

        userRepository.findByUsername("Trainer.Eleven").ifPresent(user -> {
            user.setFirstName("Trainer");
            user.setLastName("Eleven");
            user.setPassword(passwordEncoder.encode("staticPass1"));
            user.setIsActive(true);
            userRepository.saveAndFlush(user);
        });

        userRepository.findByUsername("Trainee.Twelve").ifPresent(user -> {
            user.setFirstName("Trainee");
            user.setLastName("Twelve");
            user.setPassword(passwordEncoder.encode("staticPass1"));
            user.setIsActive(false);
            userRepository.saveAndFlush(user);
        });

        userRepository.findByUsername("Trainer.Twelve").ifPresent(user -> {
            user.setFirstName("Trainer");
            user.setLastName("Twelve");
            user.setPassword(passwordEncoder.encode("staticPass1"));
            user.setIsActive(false);
            userRepository.saveAndFlush(user);
        });
    }

    @After
    public void tearDown() {
        testContext.reset();
    }
}