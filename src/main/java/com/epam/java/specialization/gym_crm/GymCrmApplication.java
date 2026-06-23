package com.epam.java.specialization.gym_crm;

import com.epam.java.specialization.gym_crm.config.ApplicationConfig;
import com.epam.java.specialization.gym_crm.storage.Storage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class GymCrmApplication {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);

        Storage storage = context.getBean(Storage.class);
        System.out.println("Контекст успішно запущено за допомогою YAML конфігурації!");
        System.out.println("Завантажено Trainees: " + storage.getTrainees().size());
        System.out.println("Завантажено Trainers: " + storage.getTrainers().size());

        context.close();
    }
}