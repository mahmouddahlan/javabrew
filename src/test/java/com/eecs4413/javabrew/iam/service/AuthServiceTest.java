package com.eecs4413.javabrew.iam.service;

import com.eecs4413.javabrew.common.exception.ApiException;
import com.eecs4413.javabrew.iam.dto.LoginRequest;
import com.eecs4413.javabrew.iam.dto.LoginResponse;
import com.eecs4413.javabrew.iam.dto.SignupRequest;
import com.eecs4413.javabrew.iam.dto.SignupResponse;
import com.eecs4413.javabrew.iam.model.User;
import com.eecs4413.javabrew.iam.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository users;
    private TokenStore tokenStore;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        tokenStore = mock(TokenStore.class);
        authService = new AuthService(users, tokenStore);
    }

    private SignupRequest validSignupRequest() {
        SignupRequest req = new SignupRequest();
        req.username = "yazan";
        req.password = "StrongPass123!";
        req.firstName = "Yazan";
        req.lastName = "A";

        SignupRequest.ShippingAddressDto addr = new SignupRequest.ShippingAddressDto();
        addr.streetName = "Main St";
        addr.streetNumber = "123";
        addr.city = "Toronto";
        addr.country = "Canada";
        addr.postalCode = "M1M1M1";
        req.shippingAddress = addr;

        return req;
    }

    private LoginRequest validLoginRequest() {
        LoginRequest req = new LoginRequest();
        req.username = "yazan";
        req.password = "StrongPass123!";
        return req;
    }

    @Test
    void signup_success() throws Exception {
        SignupRequest req = validSignupRequest();

        when(users.existsByUsername("yazan")).thenReturn(false);
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 1L);
            return user;
        });

        SignupResponse response = authService.signup(req);

        assertEquals(1L, response.userId);
        assertEquals("yazan", response.username);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("yazan", saved.getUsername());
        assertEquals("Yazan", saved.getFirstName());
        assertEquals("A", saved.getLastName());
        assertNotNull(saved.getShippingAddress());
        assertEquals("Main St", saved.getShippingAddress().streetName);
        assertEquals("123", saved.getShippingAddress().streetNumber);
        assertEquals("Toronto", saved.getShippingAddress().city);
        assertEquals("Canada", saved.getShippingAddress().country);
        assertEquals("M1M1M1", saved.getShippingAddress().postalCode);

        assertNotEquals("StrongPass123!", saved.getPasswordHash());
        assertNotNull(saved.getPasswordHash());
        assertFalse(saved.getPasswordHash().isBlank());
    }

    @Test
    void signup_duplicateUsername_throws() {
        SignupRequest req = validSignupRequest();
        when(users.existsByUsername("yazan")).thenReturn(true);

        assertThrows(ApiException.class, () -> authService.signup(req));

        verify(users, never()).save(any());
    }

    @Test
    void signup_missingShippingAddress_throws() {
        SignupRequest req = validSignupRequest();
        req.shippingAddress = null;

        when(users.existsByUsername("yazan")).thenReturn(false);

        assertThrows(ApiException.class, () -> authService.signup(req));

        verify(users, never()).save(any());
    }

    @Test
    void login_success() {
        LoginRequest req = validLoginRequest();

        User user = new User();
        user.setUsername("yazan");
        user.setPasswordHash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .encode("StrongPass123!"));

        when(users.findByUsername("yazan")).thenReturn(Optional.of(user));
        when(tokenStore.issueToken("yazan")).thenReturn("token-123");

        LoginResponse response = authService.login(req);

        assertEquals("token-123", response.token);
        assertEquals("yazan", response.username);
        verify(tokenStore).issueToken("yazan");
    }

    @Test
    void login_userNotFound_throws() {
        LoginRequest req = validLoginRequest();
        when(users.findByUsername("yazan")).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> authService.login(req));

        verify(tokenStore, never()).issueToken(any());
    }

    @Test
    void login_wrongPassword_throws() {
        LoginRequest req = validLoginRequest();

        User user = new User();
        user.setUsername("yazan");
        user.setPasswordHash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .encode("DifferentPassword123!"));

        when(users.findByUsername("yazan")).thenReturn(Optional.of(user));

        assertThrows(ApiException.class, () -> authService.login(req));

        verify(tokenStore, never()).issueToken(any());
    }
}