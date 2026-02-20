package com.eecs4413.javabrew.catalogue.service;

import com.eecs4413.javabrew.auction.service.AuctionService;
import com.eecs4413.javabrew.catalogue.dto.*;
import com.eecs4413.javabrew.catalogue.model.AuctionStatus;
import com.eecs4413.javabrew.catalogue.model.Item;
import com.eecs4413.javabrew.catalogue.repository.ItemRepository;
import com.eecs4413.javabrew.common.exception.ApiException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CatalogueService {

    private final ItemRepository items;
    private final AuctionService auctionService;

    public CatalogueService(ItemRepository items, AuctionService auctionService) {
        this.items = items;
        this.auctionService = auctionService;
    }

    public CreateItemResponse createItem(CreateItemRequest req, String sellerUsername) {
        if (req.keywords == null || req.keywords.isEmpty()) {
            throw ApiException.badRequest("keywords must not be empty");
        }

        Item item = new Item();
        item.setName(req.name);
        item.setDescription(req.description);
        item.setKeywords(req.keywords);

        item.setStartingBid(req.startingBid);
        item.setCurrentBid(req.startingBid);
        item.setEndsAt(OffsetDateTime.now().plusSeconds(req.durationSeconds));

        item.setShippingCost(req.shippingCost);
        item.setExpeditedShippingCost(req.expeditedShippingCost);
        item.setShippingDays(req.shippingDays);

        item.setSellerUsername(sellerUsername);

        items.save(item);
        return new CreateItemResponse(item.getId(), item.getStatus().name());
    }

    public List<ItemSummaryResponse> search(String keyword) {
        // Keep it simple: load ACTIVE items then filter in memory.
        // (Can be optimized with custom queries later.)
        List<Item> active = items.findByStatus(AuctionStatus.ACTIVE);

        for (Item i : active) auctionService.refreshStatus(i);

        // Re-fetch after refresh to ensure we list only active
        active = items.findByStatus(AuctionStatus.ACTIVE);

        if (keyword == null || keyword.isBlank()) {
            return active.stream()
                    .map(i -> new ItemSummaryResponse(i.getId(), i.getName(), i.getCurrentBid(),
                            i.getAuctionType().name(), i.getEndsAt()))
                    .collect(Collectors.toList());
        }

        String k = keyword.toLowerCase(Locale.ROOT);
        return active.stream()
                .filter(i ->
                        i.getName().toLowerCase(Locale.ROOT).contains(k) ||
                        i.getKeywords().stream().anyMatch(w -> w.toLowerCase(Locale.ROOT).contains(k))
                )
                .map(i -> new ItemSummaryResponse(i.getId(), i.getName(), i.getCurrentBid(),
                        i.getAuctionType().name(), i.getEndsAt()))
                .collect(Collectors.toList());
    }

    public ItemDetailResponse getItem(Long itemId) {
        Item item = items.findById(itemId).orElseThrow(() -> ApiException.notFound("Item not found"));
        auctionService.refreshStatus(item);

        ItemDetailResponse r = new ItemDetailResponse();
        r.itemId = item.getId();
        r.name = item.getName();
        r.description = item.getDescription();
        r.keywords = item.getKeywords();

        r.currentBid = item.getCurrentBid();
        r.highestBidder = item.getHighestBidder();
        r.status = item.getStatus().name();
        r.endsAt = item.getEndsAt();

        r.shippingCost = item.getShippingCost();
        r.expeditedShippingCost = item.getExpeditedShippingCost();
        r.shippingDays = item.getShippingDays();
        return r;
    }
}