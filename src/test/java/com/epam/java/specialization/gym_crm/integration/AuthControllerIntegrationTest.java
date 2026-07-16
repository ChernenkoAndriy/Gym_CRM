package com.epam.java.specialization.gym_crm.integration;

import com.epam.java.specialization.gym_crm.AbstractIntegrationTest;
import com.epam.java.specialization.gym_crm.dto.ChangeLoginRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /auth/login - Успішний вхід з валідними даними (активний користувач)")
    void login_Success() throws Exception {
        mockMvc.perform(get("/auth/login")
                        .param("username", "Trainee.Ten")
                        .param("password", "password10")
                        .with(httpBasic("Trainee.Ten", "password10")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /auth/login - Помилка: невалідні облікові дані (Bad Credentials)")
    void login_Fail_WrongPassword() throws Exception {
        mockMvc.perform(get("/auth/login")
                        .param("username", "Trainee.Ten")
                        .param("password", "wrong_password")
                        .with(httpBasic("Trainee.Ten", "wrong_password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /auth/login - Помилка: користувач заблокований / неактивний")
    void login_Fail_InactiveUser() throws Exception {
        
        mockMvc.perform(get("/auth/login")
                        .param("username", "Trainee.Twelve")
                        .param("password", "password12")
                        .with(httpBasic("Trainee.Twelve", "password12")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /auth/login - Помилка: користувача не існує")
    void login_Fail_UserNotFound() throws Exception {
        mockMvc.perform(get("/auth/login")
                        .param("username", "Non.Existent")
                        .param("password", "anyPass")
                        .with(httpBasic("Non.Existent", "anyPass")))
                .andExpect(status().isUnauthorized());
    }

    
    
    

    @Test
    @DisplayName("PUT /auth/password - Успішна зміна паролю")
    void changeLogin_Success() throws Exception {
        
        ChangeLoginRequestDto request = ChangeLoginRequestDto.builder()
                .username("Trainer.Ten")
                .oldPassword("password20")
                .newPassword("newPassword20")
                .build();

        mockMvc.perform(put("/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(httpBasic("Trainer.Ten", "password20")))
                .andExpect(status().isOk());

        
        mockMvc.perform(get("/auth/login")
                        .param("username", "Trainer.Ten")
                        .param("password", "newPassword20")
                        .with(httpBasic("Trainer.Ten", "newPassword20")))
                .andExpect(status().isOk());

        
        ChangeLoginRequestDto revertRequest = ChangeLoginRequestDto.builder()
                .username("Trainer.Ten")
                .oldPassword("newPassword20")
                .newPassword("password20")
                .build();
        mockMvc.perform(put("/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revertRequest))
                        .with(httpBasic("Trainer.Ten", "newPassword20")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /auth/password - Помилка: старий пароль неправильний")
    void changeLogin_Fail_WrongOldPassword() throws Exception {
        ChangeLoginRequestDto request = ChangeLoginRequestDto.builder()
                .username("Trainee.Eleven")
                .oldPassword("incorrect_old_pass")
                .newPassword("brandNewPass")
                .build();

        
        mockMvc.perform(put("/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(httpBasic("Trainee.Eleven", "password11")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid old password for user: Trainee.Eleven"));
    }

    @Test
    @DisplayName("PUT /auth/password - Помилка: спроба змінити пароль несумісного користувача (EntityNotFoundException)")
    void changeLogin_Fail_UserNotFound() throws Exception {
        ChangeLoginRequestDto request = ChangeLoginRequestDto.builder()
                .username("Ghost.User")
                .oldPassword("password10")
                .newPassword("newPassword10")
                .build();

        mockMvc.perform(put("/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(httpBasic("Trainee.Ten", "password10")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with username: Ghost.User"));
    }

    @Test
    @DisplayName("PUT /auth/password - Помилка: валідація DTO (пустий запит)")
    void changeLogin_Fail_ValidationFailed() throws Exception {
        ChangeLoginRequestDto invalidRequest = ChangeLoginRequestDto.builder()
                .username("")
                .oldPassword(" ")
                .newPassword("")
                .build();

        mockMvc.perform(put("/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(httpBasic("Trainee.Ten", "password10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}