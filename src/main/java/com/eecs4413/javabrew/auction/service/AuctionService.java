package com.eecs4413.javabrew.auction.service;

import com.eecs4413.javabrew.auction.dto.AuctionStateResponse;
import com.eecs4413.javabrew.auction.dto.BidRequest;
import com.eecs4413.javabrew.auction.model.Bid;
import com.eecs4413.javabrew.auction.repository.BidRepository;
import com.eecs4413.javabrew.catalogue.model.AuctionStatus;
import com.eecs4413.javabrew.catalogue.model.Item;
import com.eecs4413.javabrew.catalogue.repository.ItemRepository;
import com.eecs4413.javabrew.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class AuctionService {

    private final ItemRepository items;
    private final BidRepository bids;

    public AuctionService(ItemRepository items, BidRepository bids) {
        this.items = items;
        this.bids = bids;
    }

    /**
     * Refresh status based on time. If ended and no bids happened, mark removed.
     */
    public void refreshStatus(Item item) {
        if (item.getStatus() != AuctionStatus.ACTIVE) return;

        if (OffsetDateTime.now().isAfter(item.getEndsAt())) {
            if (!item.isHasAnyBid()) {
                item.setStatus(AuctionStatus.REMOVED_NO_BIDS);
            } else {
                item.setStatus(AuctionStatus.ENDED);
            }
            items.save(item);
        }
    }

    @Transactional
    public AuctionStateResponse getAuctionState(Long itemId) {
        Item item = items.findById(itemId).orElseThrow(() -> ApiException.notFound("Item not found"));
        refreshStatus(item);

        AuctionStateResponse r = new AuctionStateResponse();
        r.itemId = item.getId();
        r.status = item.getStatus().name();
        r.currentBid = item.getCurrentBid();
        r.highestBidder = item.getHighestBidder();
        r.endsAt = item.getEndsAt();
        return r;
    }

    /**
     * Core UC3: bid must be strictly increasing integer.
     * Transactional so two near-simultaneous bids can't both win.
     */
    @Transactional
    public AuctionStateResponse placeBid(Long itemId, String bidderUsername, BidRequest req) {
        Item item = items.findById(itemId).orElseThrow(() -> ApiException.notFound("Item not found"));
        refreshStatus(item);

        if (item.getStatus() != AuctionStatus.ACTIVE) {
            throw ApiException.conflict("Auction ended");
        }

        int newBid = req.bidAmount;
        if (newBid <= item.getCurrentBid()) {
            throw ApiException.badRequest("Bid must be strictly greater than current bid");
        }

        // Save bid history
        Bid b = new Bid();
        b.setItem(item);
        b.setBidderUsername(bidderUsername);
        b.setAmount(newBid);
        bids.save(b);

        // Update current highest
        item.setCurrentBid(newBid);
        item.setHighestBidder(bidderUsername);
        item.setHasAnyBid(true);
        items.save(item);

        return getAuctionState(itemId);
    }
}