package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dto.ChangeLoginRequestDto;
import com.epam.java.specialization.gym_crm.exception.BadCredentialsException;
import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Should successfully change user password when old password matches")
    void changeLogin_ShouldUpdatePassword_WhenCredentialsAreValid() {
        User user = User.builder()
                .username("John.Doe")
                .password("oldSecretPassword")
                .isActive(true)
                .build();

        ChangeLoginRequestDto request = ChangeLoginRequestDto.builder()
                .username("John.Doe")
                .oldPassword("oldSecretPassword")
                .newPassword("newSuperSecretPassword")
                .build();

        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.changeLogin(request);

        assertThat(user.getPassword()).isEqualTo("newSuperSecretPassword");
        verify(userRepository, times(1)).findByUsername("John.Doe");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user does not exist during password change")
    void changeLogin_ShouldThrowEntityNotFoundException_WhenUserDoesNotExist() {
        ChangeLoginRequestDto request = ChangeLoginRequestDto.builder()
                .username("Missing.User")
                .oldPassword("somePass")
                .newPassword("newPass")
                .build();

        when(userRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.changeLogin(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with username: Missing.User");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when old password is incorrect")
    void changeLogin_ShouldThrowBadCredentialsException_WhenOldPasswordIsIncorrect() {
        User user = User.builder()
                .username("John.Doe")
                .password("actualSecretPassword")
                .build();

        ChangeLoginRequestDto request = ChangeLoginRequestDto.builder()
                .username("John.Doe")
                .oldPassword("wrongOldPassword")
                .newPassword("brandNewPassword")
                .build();

        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.changeLogin(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid old password for user: John.Doe");

        verify(userRepository, never()).save(any(User.class));
    }
}