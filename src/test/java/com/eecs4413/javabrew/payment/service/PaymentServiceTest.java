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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private ItemRepository items;

    @Mock
    private PaymentRepository payments;

    @Mock
    private AuctionService auctionService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest normalRequest;
    private PaymentRequest expeditedRequest;

    @BeforeEach
    void setUp() {
        normalRequest = new PaymentRequest();
        normalRequest.expeditedShipping = false;
        normalRequest.cardNumber = "4111111111111111";
        normalRequest.nameOnCard = "Test User";
        normalRequest.expiration = "12/30";
        normalRequest.securityCode = "123";

        expeditedRequest = new PaymentRequest();
        expeditedRequest.expeditedShipping = true;
        expeditedRequest.cardNumber = "4111111111111111";
        expeditedRequest.nameOnCard = "Test User";
        expeditedRequest.expiration = "12/30";
        expeditedRequest.securityCode = "123";
    }

    @Test
    void pay_success_withoutExpeditedShipping() {
        Item item = mock(Item.class);

        when(items.findById(1L)).thenReturn(Optional.of(item));
        when(item.getStatus()).thenReturn(AuctionStatus.ENDED);
        when(item.getHighestBidder()).thenReturn("winner");
        when(item.getShippingCost()).thenReturn(10);
        when(item.getCurrentBid()).thenReturn(100);
        when(item.getShippingDays()).thenReturn(5);
        when(item.getId()).thenReturn(1L);

        when(payments.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            var idField = Payment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(p, 99L);
            return p;
        });

        PaymentResponse response = paymentService.pay(1L, "winner", normalRequest);

        assertEquals(99L, response.receiptId);
        assertEquals(1L, response.itemId);
        assertEquals("winner", response.paidBy);
        assertEquals(100, response.itemPrice);
        assertEquals(10, response.shippingCost);
        assertEquals(110, response.totalPaid);
        assertEquals(5, response.shippingDays);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(payments).save(captor.capture());

        Payment saved = captor.getValue();
        assertEquals("winner", saved.getPaidByUsername());
        assertFalse(saved.isExpeditedShipping());
        assertEquals(100, saved.getItemPrice());
        assertEquals(10, saved.getShippingCost());
        assertEquals(110, saved.getTotalPaid());
        assertEquals(5, saved.getShippingDays());
        assertEquals(item, saved.getItem());
    }

    @Test
    void pay_success_withExpeditedShipping() {
        Item item = mock(Item.class);

        when(items.findById(2L)).thenReturn(Optional.of(item));
        when(item.getStatus()).thenReturn(AuctionStatus.ENDED);
        when(item.getHighestBidder()).thenReturn("winner");
        when(item.getShippingCost()).thenReturn(10);
        when(item.getExpeditedShippingCost()).thenReturn(20);
        when(item.getCurrentBid()).thenReturn(200);
        when(item.getShippingDays()).thenReturn(3);
        when(item.getId()).thenReturn(2L);

        when(payments.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            var idField = Payment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(p, 100L);
            return p;
        });

        PaymentResponse response = paymentService.pay(2L, "winner", expeditedRequest);

        assertEquals(200, response.itemPrice);
        assertEquals(30, response.shippingCost);
        assertEquals(230, response.totalPaid);
        assertEquals(3, response.shippingDays);
    }

    @Test
    void pay_itemNotFound_throws() {
        when(items.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () ->
                paymentService.pay(1L, "winner", normalRequest));

        verify(payments, never()).save(any());
    }

    @Test
    void pay_removedNoBids_throws() {
        Item item = mock(Item.class);

        when(items.findById(1L)).thenReturn(Optional.of(item));
        when(item.getStatus()).thenReturn(AuctionStatus.REMOVED_NO_BIDS);

        assertThrows(ApiException.class, () ->
                paymentService.pay(1L, "winner", normalRequest));

        verify(payments, never()).save(any());
    }

    @Test
    void pay_auctionNotEnded_throws() {
        Item item = mock(Item.class);

        when(items.findById(1L)).thenReturn(Optional.of(item));
        when(item.getStatus()).thenReturn(AuctionStatus.ACTIVE);

        assertThrows(ApiException.class, () ->
                paymentService.pay(1L, "winner", normalRequest));

        verify(payments, never()).save(any());
    }

    @Test
    void pay_onlyWinningBidderCanPay() {
        Item item = mock(Item.class);

        when(items.findById(1L)).thenReturn(Optional.of(item));
        when(item.getStatus()).thenReturn(AuctionStatus.ENDED);
        when(item.getHighestBidder()).thenReturn("actualWinner");

        assertThrows(ApiException.class, () ->
                paymentService.pay(1L, "notWinner", normalRequest));

        verify(payments, never()).save(any());
    }

    @Test
    void pay_nullHighestBidder_rejected() {
        Item item = mock(Item.class);

        when(items.findById(1L)).thenReturn(Optional.of(item));
        when(item.getStatus()).thenReturn(AuctionStatus.ENDED);
        when(item.getHighestBidder()).thenReturn(null);

        assertThrows(ApiException.class, () ->
                paymentService.pay(1L, "winner", normalRequest));

        verify(payments, never()).save(any());
    }

    @Test
    void pay_callsRefreshStatus() {
        Item item = mock(Item.class);

        when(items.findById(1L)).thenReturn(Optional.of(item));
        when(item.getStatus()).thenReturn(AuctionStatus.ENDED);
        when(item.getHighestBidder()).thenReturn("winner");
        when(item.getShippingCost()).thenReturn(5);
        when(item.getCurrentBid()).thenReturn(50);
        when(item.getShippingDays()).thenReturn(7);
        when(item.getId()).thenReturn(1L);

        when(payments.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            var idField = Payment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(p, 1L);
            return p;
        });

        paymentService.pay(1L, "winner", normalRequest);

        verify(auctionService).refreshStatus(item);
    }

    @Test
    void receipt_success() {
        Payment payment = new Payment();
        payment.setPaidByUsername("winner");
        payment.setItemPrice(100);
        payment.setShippingCost(10);
        payment.setTotalPaid(110);
        payment.setShippingDays(4);

        try {
            var idField = Payment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(payment, 55L);
        } catch (Exception e) {
            fail(e);
        }

        when(payments.findById(55L)).thenReturn(Optional.of(payment));

        ReceiptResponse response = paymentService.receipt(55L);

        assertEquals(55L, response.receiptId);
        assertEquals(110, response.totalPaid);
        assertEquals("The item will be shipped in 4 days.", response.shippingInfo);
    }

    @Test
    void receipt_notFound_throws() {
        when(payments.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> paymentService.receipt(999L));
    }
}