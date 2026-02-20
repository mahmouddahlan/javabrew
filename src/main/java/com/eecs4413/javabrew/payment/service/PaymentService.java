package com.eecs4413.javabrew.payment.service;

import com.eecs4413.javabrew.auction.service.AuctionService;
import com.eecs4413.javabrew.catalogue.model.AuctionStatus;
import com.eecs4413.javabrew.catalogue.model.Item;
import com.eecs4413.javabrew.catalogue.repository.ItemRepository;
import com.eecs4413.javabrew.common.exception.ApiException;
import com.eecs4413.javabrew.payment.dto.PaymentRequest;
import com.eecs4413.javabrew.payment.dto.PaymentResponse;
import com.eecs4413.javabrew.payment.dto.ReceiptResponse;
import com.eecs4413.javabrew.payment.model.Payment;
import com.eecs4413.javabrew.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final ItemRepository items;
    private final PaymentRepository payments;
    private final AuctionService auctionService;

    public PaymentService(ItemRepository items, PaymentRepository payments, AuctionService auctionService) {
        this.items = items;
        this.payments = payments;
        this.auctionService = auctionService;
    }

    @Transactional
    public PaymentResponse pay(Long itemId, String username, PaymentRequest req) {
        Item item = items.findById(itemId).orElseThrow(() -> ApiException.notFound("Item not found"));
        auctionService.refreshStatus(item);

        if (item.getStatus() == AuctionStatus.REMOVED_NO_BIDS) {
            throw ApiException.conflict("Auction ended with no bids; item removed");
        }
        if (item.getStatus() != AuctionStatus.ENDED) {
            throw ApiException.conflict("Auction has not ended yet");
        }
        if (item.getHighestBidder() == null || !item.getHighestBidder().equals(username)) {
            throw ApiException.forbidden("Only the winning bidder can pay");
        }

        int shipping = item.getShippingCost() + (req.expeditedShipping ? item.getExpeditedShippingCost() : 0);
        int total = item.getCurrentBid() + shipping;

        Payment p = new Payment();
        p.setItem(item);
        p.setPaidByUsername(username);
        p.setExpeditedShipping(req.expeditedShipping);
        p.setItemPrice(item.getCurrentBid());
        p.setShippingCost(shipping);
        p.setTotalPaid(total);
        p.setShippingDays(item.getShippingDays());
        payments.save(p);

        PaymentResponse r = new PaymentResponse();
        r.receiptId = p.getId();
        r.itemId = item.getId();
        r.paidBy = username;
        r.itemPrice = p.getItemPrice();
        r.shippingCost = p.getShippingCost();
        r.totalPaid = p.getTotalPaid();
        r.shippingDays = p.getShippingDays();
        return r;
    }

    public ReceiptResponse receipt(Long receiptId) {
        Payment p = payments.findById(receiptId).orElseThrow(() -> ApiException.notFound("Receipt not found"));

        ReceiptResponse r = new ReceiptResponse();
        r.receiptId = p.getId();
        r.totalPaid = p.getTotalPaid();
        r.shippingInfo = "The item will be shipped in " + p.getShippingDays() + " days.";
        return r;
    }
}