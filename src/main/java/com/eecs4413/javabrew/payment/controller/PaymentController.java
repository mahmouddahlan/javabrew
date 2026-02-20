package com.eecs4413.javabrew.payment.controller;

import com.eecs4413.javabrew.iam.service.CurrentUser;
import com.eecs4413.javabrew.payment.dto.PaymentRequest;
import com.eecs4413.javabrew.payment.dto.PaymentResponse;
import com.eecs4413.javabrew.payment.dto.ReceiptResponse;
import com.eecs4413.javabrew.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentController {

    private final PaymentService payments;
    private final CurrentUser currentUser;

    public PaymentController(PaymentService payments, CurrentUser currentUser) {
        this.payments = payments;
        this.currentUser = currentUser;
    }

    // UC5
    @PostMapping("/api/payments/{itemId}")
    public PaymentResponse pay(@PathVariable Long itemId,
                               @Valid @RequestBody PaymentRequest req,
                               HttpServletRequest httpReq) {
        String username = currentUser.requireUsername(httpReq);
        return payments.pay(itemId, username, req);
    }

    // UC6
    @GetMapping("/api/receipts/{receiptId}")
    public ReceiptResponse receipt(@PathVariable Long receiptId) {
        return payments.receipt(receiptId);
    }
}