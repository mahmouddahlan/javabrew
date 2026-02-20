package com.eecs4413.javabrew.payment.dto;

import jakarta.validation.constraints.NotBlank;

public class PaymentRequest {
    public boolean expeditedShipping;

    @NotBlank public String cardNumber;
    @NotBlank public String nameOnCard;
    @NotBlank public String expiration;
    @NotBlank public String securityCode;
}