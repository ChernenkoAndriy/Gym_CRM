package com.epam.java.specialization.gym_crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = "com.epam.java.specialization.gym_crm")
public class TestConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        Properties properties = new Properties();
        properties.setProperty("storage.init.file-path", "classpath:init-data.json");
        configurer.setProperties(properties);
        return configurer;
    }
}