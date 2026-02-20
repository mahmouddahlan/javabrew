package com.eecs4413.javabrew.iam.service;

import com.eecs4413.javabrew.common.exception.ApiException;
import com.eecs4413.javabrew.iam.dto.LoginRequest;
import com.eecs4413.javabrew.iam.dto.LoginResponse;
import com.eecs4413.javabrew.iam.dto.SignupRequest;
import com.eecs4413.javabrew.iam.dto.SignupResponse;
import com.eecs4413.javabrew.iam.model.Address;
import com.eecs4413.javabrew.iam.model.User;
import com.eecs4413.javabrew.iam.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final TokenStore tokenStore;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository users, TokenStore tokenStore) {
        this.users = users;
        this.tokenStore = tokenStore;
    }

    public SignupResponse signup(SignupRequest req) {
        if (users.existsByUsername(req.username)) {
            throw ApiException.conflict("Username already exists");
        }

        User u = new User();
        u.setUsername(req.username);
        u.setPasswordHash(encoder.encode(req.password));
        u.setFirstName(req.firstName);
        u.setLastName(req.lastName);

        if (req.shippingAddress == null) throw ApiException.badRequest("shippingAddress is required");
        Address a = new Address();
        a.streetName = req.shippingAddress.streetName;
        a.streetNumber = req.shippingAddress.streetNumber;
        a.city = req.shippingAddress.city;
        a.country = req.shippingAddress.country;
        a.postalCode = req.shippingAddress.postalCode;
        u.setShippingAddress(a);

        users.save(u);
        return new SignupResponse(u.getId(), u.getUsername());
    }

    public LoginResponse login(LoginRequest req) {
        User u = users.findByUsername(req.username)
                .orElseThrow(() -> ApiException.unauthorized("Invalid credentials"));

        if (!encoder.matches(req.password, u.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid credentials");
        }

        String token = tokenStore.issueToken(u.getUsername());
        return new LoginResponse(token, u.getUsername());
    }
}