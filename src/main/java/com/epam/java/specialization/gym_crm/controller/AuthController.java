package com.epam.java.specialization.gym_crm.controller;

import com.epam.java.specialization.gym_crm.dto.ChangeLoginRequestDto;
import com.epam.java.specialization.gym_crm.service.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API")
public class AuthController {

    private final AuthService authService;


    @GetMapping("/login")
    @Operation(summary = "Login")
    public ResponseEntity<Void> login(
            @RequestParam String username,
            @RequestParam String password) {
        authService.login(username, password);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/password")
    @Operation(summary = "Change Login")
    public ResponseEntity<Void> changeLogin(@Valid @RequestBody ChangeLoginRequestDto request) {
        authService.changeLogin(request);
        return ResponseEntity.ok().build();
    }
}