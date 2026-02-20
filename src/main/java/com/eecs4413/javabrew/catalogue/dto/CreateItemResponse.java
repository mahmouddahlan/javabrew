package com.eecs4413.javabrew.catalogue.dto;

public class CreateItemResponse {
    public Long itemId;
    public String status;

    public CreateItemResponse(Long itemId, String status) {
        this.itemId = itemId;
        this.status = status;
    }
}