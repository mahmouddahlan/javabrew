package com.eecs4413.javabrew.auction.controller;

import com.eecs4413.javabrew.auction.dto.AuctionStateResponse;
import com.eecs4413.javabrew.auction.dto.BidRequest;
import com.eecs4413.javabrew.auction.service.AuctionService;
import com.eecs4413.javabrew.iam.service.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auction;
    private final CurrentUser currentUser;

    public AuctionController(AuctionService auction, CurrentUser currentUser) {
        this.auction = auction;
        this.currentUser = currentUser;
    }

    @GetMapping("/{itemId}")
    public AuctionStateResponse state(@PathVariable Long itemId) {
        return auction.getAuctionState(itemId);
    }

    // UC3: bidding
    @PostMapping("/{itemId}/bids")
    public AuctionStateResponse bid(@PathVariable Long itemId,
                                    @Valid @RequestBody BidRequest req,
                                    HttpServletRequest httpReq) {
        String username = currentUser.requireUsername(httpReq);
        return auction.placeBid(itemId, username, req);
    }
}