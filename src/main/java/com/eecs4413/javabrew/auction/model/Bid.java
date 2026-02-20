package com.eecs4413.javabrew.auction.model;

import com.eecs4413.javabrew.catalogue.model.Item;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
public class Bid {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    private Item item;

    @Column(nullable=false)
    private String bidderUsername;

    @Column(nullable=false)
    private int amount;

    @Column(nullable=false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Long getId() { return id; }
    public Item getItem() { return item; }
    public String getBidderUsername() { return bidderUsername; }
    public int getAmount() { return amount; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setItem(Item item) { this.item = item; }
    public void setBidderUsername(String bidderUsername) { this.bidderUsername = bidderUsername; }
    public void setAmount(int amount) { this.amount = amount; }
}