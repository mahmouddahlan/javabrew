package com.eecs4413.javabrew.iam.dto;

public class SignupResponse {
    public Long userId;
    public String username;

    public SignupResponse(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }
}