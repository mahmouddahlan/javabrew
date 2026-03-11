package com.eecs4413.javabrew.auction.controller;

import com.eecs4413.javabrew.auction.dto.AdminStatsResponse;
import com.eecs4413.javabrew.auction.service.AdminService;
import com.eecs4413.javabrew.catalogue.model.Item;
import com.eecs4413.javabrew.iam.service.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final CurrentUser currentUser;

    public AdminController(AdminService adminService, CurrentUser currentUser) {
        this.adminService = adminService;
        this.currentUser = currentUser;
    }

    @GetMapping("/items")
    public List<Item> getAllItems(HttpServletRequest req) {
        String username = currentUser.requireUsername(req);
        return adminService.getAllItems(username);
    }

    @DeleteMapping("/items/{itemId}")
    public void deleteItem(@PathVariable Long itemId, HttpServletRequest req) {
        String username = currentUser.requireUsername(req);
        adminService.deleteItem(username, itemId);
    }

    @PostMapping("/auctions/{itemId}/end")
    public void forceEndAuction(@PathVariable Long itemId, HttpServletRequest req) {
        String username = currentUser.requireUsername(req);
        adminService.forceEnd(username, itemId);
    }

    @GetMapping("/stats")
    public AdminStatsResponse stats(HttpServletRequest req) {
        String username = currentUser.requireUsername(req);
        return adminService.getStats(username);
    }
}