package com.eecs4413.javabrew.auction.service;

import com.eecs4413.javabrew.auction.dto.AuctionStateResponse;
import com.eecs4413.javabrew.auction.dto.BidRequest;
import com.eecs4413.javabrew.auction.model.Bid;
import com.eecs4413.javabrew.auction.repository.BidRepository;
import com.eecs4413.javabrew.catalogue.model.AuctionStatus;
import com.eecs4413.javabrew.catalogue.model.Item;
import com.eecs4413.javabrew.catalogue.repository.ItemRepository;
import com.eecs4413.javabrew.common.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuctionServiceTest {

    private ItemRepository items;
    private BidRepository bids;
    private AuctionService auctionService;

    @BeforeEach
    void setUp() {
        items = mock(ItemRepository.class);
        bids = mock(BidRepository.class);
        auctionService = new AuctionService(items, bids);
    }

    private BidRequest bidRequest(int amount) {
        BidRequest req = new BidRequest();
        req.bidAmount = amount;
        return req;
    }

    private void setItemId(Item item, long id) {
        try {
            Field f = Item.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(item, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Item activeItem(long id, int currentBid, String highestBidder, boolean hasAnyBid, OffsetDateTime endsAt) {
        Item item = new Item();
        setItemId(item, id);
        item.setStatus(AuctionStatus.ACTIVE);
        item.setCurrentBid(currentBid);
        item.setHighestBidder(highestBidder);
        item.setHasAnyBid(hasAnyBid);
        item.setEndsAt(endsAt);
        return item;
    }

    @Test
    void getAuctionState_success() {
        Item item = activeItem(1L, 100, "alice", true, OffsetDateTime.now().plusHours(1));
        when(items.findById(1L)).thenReturn(Optional.of(item));

        AuctionStateResponse response = auctionService.getAuctionState(1L);

        assertEquals(1L, response.itemId);
        assertEquals("ACTIVE", response.status);
        assertEquals(100, response.currentBid);
        assertEquals("alice", response.highestBidder);
        assertEquals(item.getEndsAt(), response.endsAt);
    }

    @Test
    void getAuctionState_itemNotFound_throws() {
        when(items.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> auctionService.getAuctionState(1L));
    }

    @Test
    void refreshStatus_pastEnd_noBids_marksRemovedNoBids() {
        Item item = activeItem(1L, 50, null, false, OffsetDateTime.now().minusSeconds(1));

        auctionService.refreshStatus(item);

        assertEquals(AuctionStatus.REMOVED_NO_BIDS, item.getStatus());
        verify(items).save(item);
    }

    @Test
    void refreshStatus_pastEnd_withBids_marksEnded() {
        Item item = activeItem(1L, 80, "winner", true, OffsetDateTime.now().minusSeconds(1));

        auctionService.refreshStatus(item);

        assertEquals(AuctionStatus.ENDED, item.getStatus());
        verify(items).save(item);
    }

    @Test
    void refreshStatus_activeFutureAuction_doesNothing() {
        Item item = activeItem(1L, 80, "winner", true, OffsetDateTime.now().plusMinutes(10));

        auctionService.refreshStatus(item);

        assertEquals(AuctionStatus.ACTIVE, item.getStatus());
        verify(items, never()).save(any());
    }

    @Test
    void placeBid_success_updatesItemAndSavesBid() {
        Item item = activeItem(1L, 100, "alice", true, OffsetDateTime.now().plusHours(1));
        when(items.findById(1L)).thenReturn(Optional.of(item));

        AuctionStateResponse response = auctionService.placeBid(1L, "bob", bidRequest(150));

        assertEquals(1L, response.itemId);
        assertEquals("ACTIVE", response.status);
        assertEquals(150, response.currentBid);
        assertEquals("bob", response.highestBidder);

        assertEquals(150, item.getCurrentBid());
        assertEquals("bob", item.getHighestBidder());
        assertTrue(item.isHasAnyBid());

        ArgumentCaptor<Bid> captor = ArgumentCaptor.forClass(Bid.class);
        verify(bids).save(captor.capture());

        Bid savedBid = captor.getValue();
        assertEquals(item, savedBid.getItem());
        assertEquals("bob", savedBid.getBidderUsername());
        assertEquals(150, savedBid.getAmount());

        verify(items, atLeastOnce()).save(item);
    }

    @Test
    void placeBid_itemNotFound_throws() {
        when(items.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> auctionService.placeBid(1L, "bob", bidRequest(150)));
        verify(bids, never()).save(any());
    }

    @Test
    void placeBid_statusEnded_rejected() {
        Item item = new Item();
        setItemId(item, 1L);
        item.setStatus(AuctionStatus.ENDED);
        item.setCurrentBid(100);
        item.setEndsAt(OffsetDateTime.now().plusHours(1));

        when(items.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ApiException.class, () -> auctionService.placeBid(1L, "bob", bidRequest(150)));
        verify(bids, never()).save(any());
    }

    @Test
    void placeBid_statusRemovedNoBids_rejected() {
        Item item = new Item();
        setItemId(item, 1L);
        item.setStatus(AuctionStatus.REMOVED_NO_BIDS);
        item.setCurrentBid(100);
        item.setEndsAt(OffsetDateTime.now().plusHours(1));

        when(items.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ApiException.class, () -> auctionService.placeBid(1L, "bob", bidRequest(150)));
        verify(bids, never()).save(any());
    }

    @Test
    void placeBid_equalToCurrentBid_rejected() {
        Item item = activeItem(1L, 100, "alice", true, OffsetDateTime.now().plusHours(1));
        when(items.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ApiException.class, () -> auctionService.placeBid(1L, "bob", bidRequest(100)));

        verify(bids, never()).save(any());
        verify(items, never()).save(any());
    }

    @Test
    void placeBid_lowerThanCurrentBid_rejected() {
        Item item = activeItem(1L, 100, "alice", true, OffsetDateTime.now().plusHours(1));
        when(items.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ApiException.class, () -> auctionService.placeBid(1L, "bob", bidRequest(99)));

        verify(bids, never()).save(any());
        verify(items, never()).save(any());
    }

    @Test
    void placeBid_expiredAuctionWithBids_rejectedAndEnded() {
        Item item = activeItem(1L, 100, "alice", true, OffsetDateTime.now().minusSeconds(1));
        when(items.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ApiException.class, () -> auctionService.placeBid(1L, "bob", bidRequest(150)));

        assertEquals(AuctionStatus.ENDED, item.getStatus());
        verify(items).save(item);
        verify(bids, never()).save(any());
    }

    @Test
    void placeBid_expiredAuctionWithoutBids_rejectedAndRemoved() {
        Item item = activeItem(1L, 100, null, false, OffsetDateTime.now().minusSeconds(1));
        when(items.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ApiException.class, () -> auctionService.placeBid(1L, "bob", bidRequest(150)));

        assertEquals(AuctionStatus.REMOVED_NO_BIDS, item.getStatus());
        verify(items).save(item);
        verify(bids, never()).save(any());
    }
}