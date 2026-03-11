package com.eecs4413.javabrew.iam.dto;

public class CurrentUserResponse {
    public Long userId;
    public String username;
    public String firstName;
    public String lastName;
    public String role;

    public CurrentUserResponse(Long userId, String username, String firstName, String lastName, String role) {
        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
}