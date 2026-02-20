package com.eecs4413.javabrew.config;

import com.eecs4413.javabrew.common.exception.ApiException;
import com.eecs4413.javabrew.iam.service.CurrentUser;
import com.eecs4413.javabrew.iam.service.TokenStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Checks Authorization: Bearer <token> and attaches username into request attributes.
 * Keeps scope simple for D2; can be upgraded to JWT in D3.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenStore tokenStore;

    public AuthInterceptor(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw ApiException.unauthorized("Missing or invalid Authorization header");
        }
        String token = header.substring("Bearer ".length()).trim();
        String username = tokenStore.getUsername(token);
        if (username == null) throw ApiException.unauthorized("Invalid token");

        req.setAttribute(CurrentUser.ATTR_USERNAME, username);
        return true;
    }
}