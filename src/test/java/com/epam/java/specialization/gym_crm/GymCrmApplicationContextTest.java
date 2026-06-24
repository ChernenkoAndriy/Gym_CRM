package com.epam.java.specialization.gym_crm;

import com.epam.java.specialization.gym_crm.config.TestConfig;
import com.epam.java.specialization.gym_crm.facade.GymFacade;
import com.epam.java.specialization.gym_crm.storage.Storage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class GymCrmApplicationContextTest {

    @Autowired
    private GymFacade gymFacade;

    @Autowired
    private Storage storage;

    @Test
    void contextLoads_ShouldInitializeAllBeansAndWiredDependencies() {
        assertNotNull(gymFacade, "GymFacade bean should be successfully injected in context");
        assertNotNull(storage, "Storage bean should be pre-populated and managed by Spring");
    }
}