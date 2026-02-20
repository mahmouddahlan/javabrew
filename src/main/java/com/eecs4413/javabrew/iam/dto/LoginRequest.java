package com.eecs4413.javabrew.iam.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank public String username;
    @NotBlank public String password;
}