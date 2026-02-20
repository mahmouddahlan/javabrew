package com.eecs4413.javabrew.auction.repository;

import com.eecs4413.javabrew.auction.model.Bid;
import com.eecs4413.javabrew.catalogue.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByItemOrderByCreatedAtDesc(Item item);
}