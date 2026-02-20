package com.eecs4413.javabrew.catalogue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateItemRequest {
    @NotBlank public String name;
    @NotBlank public String description;

    @NotNull public List<String> keywords;

    @Min(1) public int startingBid;
    @Min(5) public int durationSeconds;

    @Min(0) public int shippingCost;
    @Min(0) public int expeditedShippingCost;
    @Min(1) public int shippingDays;
}