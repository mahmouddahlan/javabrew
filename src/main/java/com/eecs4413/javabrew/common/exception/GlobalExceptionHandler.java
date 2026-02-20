package com.eecs4413.javabrew.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handle(ApiException ex, HttpServletRequest req) {
        String errorLabel = switch (ex.getStatus()) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            default -> "Error";
        };
        return ResponseEntity.status(ex.getStatus())
                .body(new ApiError(ex.getStatus(), errorLabel, ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().isEmpty()
                ? "Invalid input"
                : ex.getBindingResult().getFieldErrors().get(0).getField() + ": " +
                  ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        return ResponseEntity.status(400)
                .body(new ApiError(400, "Bad Request", msg, req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(500)
                .body(new ApiError(500, "Internal Server Error", "Unexpected error", req.getRequestURI()));
    }
}