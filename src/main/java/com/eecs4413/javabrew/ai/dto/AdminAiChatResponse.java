package com.eecs4413.javabrew.ai.dto;

public class AdminAiChatResponse {
    public String reply;
    public boolean configured;

    public AdminAiChatResponse(String reply, boolean configured) {
        this.reply = reply;
        this.configured = configured;
    }
}
