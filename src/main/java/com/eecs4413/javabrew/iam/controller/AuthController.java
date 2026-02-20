package com.eecs4413.javabrew.iam.controller;

import com.eecs4413.javabrew.iam.dto.LoginRequest;
import com.eecs4413.javabrew.iam.dto.LoginResponse;
import com.eecs4413.javabrew.iam.dto.SignupRequest;
import com.eecs4413.javabrew.iam.dto.SignupResponse;
import com.eecs4413.javabrew.iam.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest req) {
        return ResponseEntity.status(201).body(auth.signup(req));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }
}