package com.eecs4413.javabrew.payment.dto;

public class PaymentResponse {
    public Long receiptId;
    public Long itemId;
    public String paidBy;

    public int itemPrice;
    public int shippingCost;
    public int totalPaid;
    public int shippingDays;
}