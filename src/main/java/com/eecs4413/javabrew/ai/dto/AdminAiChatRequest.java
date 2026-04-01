package com.eecs4413.javabrew.ai.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminAiChatRequest {
    @NotBlank(message = "message is required")
    public String message;
}
