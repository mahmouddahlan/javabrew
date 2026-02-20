package com.eecs4413.javabrew.catalogue.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class ItemDetailResponse {
    public Long itemId;
    public String name;
    public String description;
    public List<String> keywords;

    public int currentBid;
    public String highestBidder;
    public String status;
    public OffsetDateTime endsAt;

    public int shippingCost;
    public int expeditedShippingCost;
    public int shippingDays;
}