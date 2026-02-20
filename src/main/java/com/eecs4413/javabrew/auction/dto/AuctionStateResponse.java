package com.eecs4413.javabrew.auction.dto;

import java.time.OffsetDateTime;

public class AuctionStateResponse {
    public Long itemId;
    public String status;
    public int currentBid;
    public String highestBidder;
    public OffsetDateTime endsAt;
}