package com.eecs4413.javabrew.ai.service;

import com.eecs4413.javabrew.auction.model.Bid;
import com.eecs4413.javabrew.auction.repository.BidRepository;
import com.eecs4413.javabrew.catalogue.model.AuctionStatus;
import com.eecs4413.javabrew.catalogue.model.Item;
import com.eecs4413.javabrew.catalogue.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminAiAnalyticsService {

    private final ItemRepository items;
    private final BidRepository bids;

    public AdminAiAnalyticsService(ItemRepository items, BidRepository bids) {
        this.items = items;
        this.bids = bids;
    }

    public Map<String, Object> getAdminStats() {
        List<Item> allItems = items.findAll();
        List<Bid> allBids = bids.findAll();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalItems", allItems.size());
        result.put("activeItems", countItemsByStatus(allItems, AuctionStatus.ACTIVE));
        result.put("endedItems", countItemsByStatus(allItems, AuctionStatus.ENDED));
        result.put("removedNoBidItems", countItemsByStatus(allItems, AuctionStatus.REMOVED_NO_BIDS));
        result.put("totalBids", allBids.size());
        result.put("uniqueBidders", allBids.stream().map(Bid::getBidderUsername).distinct().count());
        result.put("highestCurrentBid", allItems.stream().mapToInt(Item::getCurrentBid).max().orElse(0));
        return result;
    }

    public List<Map<String, Object>> listAuctionsByStatus(String status, int limit) {
        AuctionStatus parsedStatus = AuctionStatus.valueOf(status);
        return items.findAll().stream()
                .filter(item -> item.getStatus() == parsedStatus)
                .sorted(Comparator.comparing(Item::getEndsAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(limit)
                .map(this::toAuctionSummary)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getTopAuctionsByBid(int limit) {
        return items.findAll().stream()
                .sorted(Comparator.comparingInt(Item::getCurrentBid).reversed())
                .limit(limit)
                .map(this::toAuctionSummary)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getItemsWithNoBids(int limit) {
        return items.findAll().stream()
                .filter(item -> !item.isHasAnyBid())
                .sorted(Comparator.comparing(Item::getEndsAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(limit)
                .map(this::toAuctionSummary)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getBidActivitySummary() {
        List<Bid> allBids = bids.findAll();
        List<Item> allItems = items.findAll();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalBids", allBids.size());
        result.put("activeAuctionsWithBids", allItems.stream()
                .filter(item -> item.getStatus() == AuctionStatus.ACTIVE && item.isHasAnyBid())
                .count());
        result.put("endedAuctionsWithBids", allItems.stream()
                .filter(item -> item.getStatus() == AuctionStatus.ENDED && item.isHasAnyBid())
                .count());
        result.put("averageBidAmount", allBids.stream().mapToInt(Bid::getAmount).average().orElse(0.0));
        result.put("highestBidAmount", allBids.stream().mapToInt(Bid::getAmount).max().orElse(0));
        result.put("mostRecentBids", allBids.stream()
                .sorted(Comparator.comparing(Bid::getCreatedAt).reversed())
                .limit(5)
                .map(bid -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("itemId", bid.getItem().getId());
                    row.put("itemName", bid.getItem().getName());
                    row.put("bidderUsername", bid.getBidderUsername());
                    row.put("amount", bid.getAmount());
                    row.put("createdAt", bid.getCreatedAt());
                    return row;
                })
                .collect(Collectors.toList()));
        return result;
    }

    private long countItemsByStatus(List<Item> allItems, AuctionStatus status) {
        return allItems.stream().filter(item -> item.getStatus() == status).count();
    }

    private Map<String, Object> toAuctionSummary(Item item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("itemId", item.getId());
        row.put("name", item.getName());
        row.put("status", item.getStatus().name());
        row.put("currentBid", item.getCurrentBid());
        row.put("highestBidder", item.getHighestBidder());
        row.put("hasAnyBid", item.isHasAnyBid());
        row.put("sellerUsername", item.getSellerUsername());
        row.put("endsAt", item.getEndsAt());
        return row;
    }
}
