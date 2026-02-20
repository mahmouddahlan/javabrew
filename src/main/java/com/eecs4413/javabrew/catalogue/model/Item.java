package com.eecs4413.javabrew.catalogue.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(length=2000)
    private String description;

    @ElementCollection
    private List<String> keywords = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private AuctionType auctionType = AuctionType.FORWARD;

    @Enumerated(EnumType.STRING)
    private AuctionStatus status = AuctionStatus.ACTIVE;

    // Auction fields
    private int startingBid;
    private int currentBid;
    private String highestBidder; // username
    private boolean hasAnyBid;    // true only after a real bid is placed (not just startingBid)
    private OffsetDateTime endsAt;

    // Shipping fields
    private int shippingCost;
    private int expeditedShippingCost;
    private int shippingDays;

    // Seller (not required by spec but helpful)
    private String sellerUsername;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getKeywords() { return keywords; }
    public AuctionType getAuctionType() { return auctionType; }
    public AuctionStatus getStatus() { return status; }
    public int getStartingBid() { return startingBid; }
    public int getCurrentBid() { return currentBid; }
    public String getHighestBidder() { return highestBidder; }
    public boolean isHasAnyBid() { return hasAnyBid; }
    public OffsetDateTime getEndsAt() { return endsAt; }
    public int getShippingCost() { return shippingCost; }
    public int getExpeditedShippingCost() { return expeditedShippingCost; }
    public int getShippingDays() { return shippingDays; }
    public String getSellerUsername() { return sellerUsername; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    public void setAuctionType(AuctionType auctionType) { this.auctionType = auctionType; }
    public void setStatus(AuctionStatus status) { this.status = status; }
    public void setStartingBid(int startingBid) { this.startingBid = startingBid; }
    public void setCurrentBid(int currentBid) { this.currentBid = currentBid; }
    public void setHighestBidder(String highestBidder) { this.highestBidder = highestBidder; }
    public void setHasAnyBid(boolean hasAnyBid) { this.hasAnyBid = hasAnyBid; }
    public void setEndsAt(OffsetDateTime endsAt) { this.endsAt = endsAt; }
    public void setShippingCost(int shippingCost) { this.shippingCost = shippingCost; }
    public void setExpeditedShippingCost(int expeditedShippingCost) { this.expeditedShippingCost = expeditedShippingCost; }
    public void setShippingDays(int shippingDays) { this.shippingDays = shippingDays; }
    public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }
}