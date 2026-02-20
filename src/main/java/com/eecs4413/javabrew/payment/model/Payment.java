package com.eecs4413.javabrew.payment.model;

import com.eecs4413.javabrew.catalogue.model.Item;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    private Item item;

    @Column(nullable=false)
    private String paidByUsername;

    private boolean expeditedShipping;

    private int itemPrice;
    private int shippingCost;
    private int totalPaid;
    private int shippingDays;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Long getId() { return id; }
    public Item getItem() { return item; }
    public String getPaidByUsername() { return paidByUsername; }
    public boolean isExpeditedShipping() { return expeditedShipping; }
    public int getItemPrice() { return itemPrice; }
    public int getShippingCost() { return shippingCost; }
    public int getTotalPaid() { return totalPaid; }
    public int getShippingDays() { return shippingDays; }

    public void setItem(Item item) { this.item = item; }
    public void setPaidByUsername(String paidByUsername) { this.paidByUsername = paidByUsername; }
    public void setExpeditedShipping(boolean expeditedShipping) { this.expeditedShipping = expeditedShipping; }
    public void setItemPrice(int itemPrice) { this.itemPrice = itemPrice; }
    public void setShippingCost(int shippingCost) { this.shippingCost = shippingCost; }
    public void setTotalPaid(int totalPaid) { this.totalPaid = totalPaid; }
    public void setShippingDays(int shippingDays) { this.shippingDays = shippingDays; }
}