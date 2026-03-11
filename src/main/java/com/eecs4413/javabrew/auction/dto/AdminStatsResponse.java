package com.eecs4413.javabrew.auction.dto;

public class AdminStatsResponse {
    public long totalItems;
    public long activeItems;
    public long endedItems;
    public long removedNoBidItems;

    public AdminStatsResponse(long totalItems, long activeItems, long endedItems, long removedNoBidItems) {
        this.totalItems = totalItems;
        this.activeItems = activeItems;
        this.endedItems = endedItems;
        this.removedNoBidItems = removedNoBidItems;
    }
}