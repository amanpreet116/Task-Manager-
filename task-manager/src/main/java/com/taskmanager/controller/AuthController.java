package com.taskmanager.controller;

import com.taskmanager.dto.JwtResponseDto;
import com.taskmanager.dto.LoginRequestDto;
import com.taskmanager.dto.RegisterRequestDto;
import com.taskmanager.dto.UserResponseDto;
import com.taskmanager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody RegisterRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public JwtResponseDto login(
            @Valid @RequestBody LoginRequestDto request) {
        return authService.login(request);
    }
}
