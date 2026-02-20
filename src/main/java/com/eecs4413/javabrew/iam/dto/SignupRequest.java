package com.eecs4413.javabrew.iam.dto;

import jakarta.validation.constraints.NotBlank;

public class SignupRequest {
    @NotBlank public String username;
    @NotBlank public String password;
    @NotBlank public String firstName;
    @NotBlank public String lastName;
    public ShippingAddressDto shippingAddress;

    public static class ShippingAddressDto {
        @NotBlank public String streetName;
        @NotBlank public String streetNumber;
        @NotBlank public String city;
        @NotBlank public String country;
        @NotBlank public String postalCode;
    }
}