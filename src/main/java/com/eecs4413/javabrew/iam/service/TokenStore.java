package com.eecs4413.javabrew.iam.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deliverable-2 friendly auth:
 * - Issue random tokens on login
 * - Store token -> username in memory
 * Not production security, but perfect for course milestone.
 */
@Component
public class TokenStore {
    private final Map<String, String> tokenToUser = new ConcurrentHashMap<>();

    public String issueToken(String username) {
        String token = UUID.randomUUID().toString();
        tokenToUser.put(token, username);
        return token;
    }

    public String getUsername(String token) {
        return tokenToUser.get(token);
    }
}