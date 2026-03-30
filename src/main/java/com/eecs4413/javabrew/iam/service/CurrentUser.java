package com.eecs4413.javabrew.iam.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CurrentUser {
    public static final String ATTR_USERNAME = "AUTH_USERNAME";

    public String requireUsername(HttpServletRequest req) {
        Object v = req.getAttribute(ATTR_USERNAME);

        if (v == null || v.toString().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        return v.toString();
    }
}
