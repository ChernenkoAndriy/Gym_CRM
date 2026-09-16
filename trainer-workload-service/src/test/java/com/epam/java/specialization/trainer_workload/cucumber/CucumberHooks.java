package com.epam.java.specialization.trainer_workload.cucumber;

import com.epam.java.specialization.trainer_workload.repository.TrainerWorkloadRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

public class CucumberHooks {

    @Autowired
    private TestContext testContext;

    @Autowired
    private TrainerWorkloadRepository repository;

    @Before
    public void setUp() {
        testContext.reset();
        Mockito.reset(repository);
    }

    @After
    public void tearDown() {
        testContext.reset();
    }
}