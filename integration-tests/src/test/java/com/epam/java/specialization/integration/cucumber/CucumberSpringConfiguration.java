package com.epam.java.specialization.integration.cucumber;

import com.epam.java.specialization.integration.IntegrationTestsApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = IntegrationTestsApplication.class)
public class CucumberSpringConfiguration {
}