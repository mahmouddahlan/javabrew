package com.eecs4413.javabrew.auction.dto;

import jakarta.validation.constraints.Min;

public class BidRequest {
    @Min(1) public int bidAmount;
}