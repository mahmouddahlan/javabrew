package com.eecs4413.javabrew.catalogue.dto;

import java.time.OffsetDateTime;

public class ItemSummaryResponse {
    public Long itemId;
    public String name;
    public int currentBid;
    public String auctionType;
    public OffsetDateTime endsAt;

    public ItemSummaryResponse(Long itemId, String name, int currentBid, String auctionType, OffsetDateTime endsAt) {
        this.itemId = itemId;
        this.name = name;
        this.currentBid = currentBid;
        this.auctionType = auctionType;
        this.endsAt = endsAt;
    }
}