package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dto.ChangeLoginRequestDto;
import com.epam.java.specialization.gym_crm.dto.JwtResponseDto;
import com.epam.java.specialization.gym_crm.dto.LoginRequestDto;
import com.epam.java.specialization.gym_crm.exception.BadCredentialsException;
import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.repository.UserRepository;
import com.epam.java.specialization.gym_crm.security.HttpRequestUtils;
import com.epam.java.specialization.gym_crm.security.JwtService;
import com.epam.java.specialization.gym_crm.security.LoginAttemptService;
import com.epam.java.specialization.gym_crm.security.TokenBlacklistService;
import com.epam.java.specialization.gym_crm.service.interfaces.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public JwtResponseDto login(LoginRequestDto request, HttpServletRequest httpRequest) {
        String username = request.getUsername();
        String clientIp = HttpRequestUtils.getClientIP(httpRequest);
        log.info("Starting authentication attempt for user: {} from IP: {}", username, clientIp);

        loginAttemptService.checkIfBlocked(username, clientIp);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );
            loginAttemptService.loginSucceeded(username, clientIp);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            log.info("User {} successfully authenticated and JWT token generated", username);
            return JwtResponseDto.builder()
                    .token(token)
                    .type("Bearer")
                    .build();
        } catch (org.springframework.security.core.AuthenticationException ex) {
            log.warn("Authentication failed for user: {} from IP: {}. Reason: {}", username, clientIp, ex.getMessage());
            loginAttemptService.loginFailed(username, clientIp);
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public void changeLogin(ChangeLoginRequestDto request) {
        log.info("Starting password change operation for user: {}", request.getUsername());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Password change failed: User not found with username: {}", request.getUsername());
                    return new EntityNotFoundException("User not found with username: " + request.getUsername());
                });

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("Password change failed: Invalid old password provided for user: {}", request.getUsername());
            throw new BadCredentialsException("Invalid old password for user: " + request.getUsername());
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password successfully updated for user: {}", request.getUsername());
    }

    @Override
    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
            log.info("User logged out successfully. Token placed into blacklist");
        } else {
            log.debug("Logout invoked without Bearer token");
        }
        SecurityContextHolder.clearContext();
    }
}