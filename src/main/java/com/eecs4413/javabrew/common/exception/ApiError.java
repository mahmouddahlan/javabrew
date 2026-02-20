package com.eecs4413.javabrew.common.exception;

import java.time.OffsetDateTime;

public class ApiError {
    public OffsetDateTime timestamp;
    public int status;
    public String error;
    public String message;
    public String path;

    public ApiError(int status, String error, String message, String path) {
        this.timestamp = OffsetDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}