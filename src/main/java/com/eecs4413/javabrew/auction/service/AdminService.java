package com.eecs4413.javabrew.auction.service;

import com.eecs4413.javabrew.auction.dto.AdminStatsResponse;
import com.eecs4413.javabrew.catalogue.model.AuctionStatus;
import com.eecs4413.javabrew.catalogue.model.Item;
import com.eecs4413.javabrew.catalogue.repository.ItemRepository;
import com.eecs4413.javabrew.common.exception.ApiException;
import com.eecs4413.javabrew.iam.model.Role;
import com.eecs4413.javabrew.iam.model.User;
import com.eecs4413.javabrew.iam.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository users;
    private final ItemRepository items;
    private final AuctionService auctionService;

    public AdminService(UserRepository users, ItemRepository items, AuctionService auctionService) {
        this.users = users;
        this.items = items;
        this.auctionService = auctionService;
    }

    private void requireAdmin(String username) {
        User user = users.findByUsername(username)
                .orElseThrow(() -> ApiException.unauthorized("User not found"));

        if (user.getRole() != Role.ADMIN) {
            throw ApiException.forbidden("Admin access required");
        }
    }

    public List<Item> getAllItems(String username) {
        requireAdmin(username);
        return items.findAll();
    }

    public void deleteItem(String username, Long itemId) {
        requireAdmin(username);
        Item item = items.findById(itemId).orElseThrow(() -> ApiException.notFound("Item not found"));
        items.delete(item);
    }

    public AdminStatsResponse getStats(String username) {
        requireAdmin(username);

        long total = items.count();
        long active = items.countByStatus(AuctionStatus.ACTIVE);
        long ended = items.countByStatus(AuctionStatus.ENDED);
        long removed = items.countByStatus(AuctionStatus.REMOVED_NO_BIDS);

        return new AdminStatsResponse(total, active, ended, removed);
    }

    public void forceEnd(String username, Long itemId) {
        requireAdmin(username);
        auctionService.forceEndAuction(itemId);
    }
}