package com.eecs4413.javabrew.iam.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public static final String ATTR_USERNAME = "AUTH_USERNAME";

    public String requireUsername(HttpServletRequest req) {
        Object v = req.getAttribute(ATTR_USERNAME);
        return v == null ? null : v.toString();
    }
}