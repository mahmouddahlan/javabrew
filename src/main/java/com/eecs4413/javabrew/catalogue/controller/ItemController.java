package com.eecs4413.javabrew.catalogue.controller;

import com.eecs4413.javabrew.catalogue.dto.*;
import com.eecs4413.javabrew.catalogue.service.CatalogueService;
import com.eecs4413.javabrew.iam.service.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final CatalogueService catalogue;
    private final CurrentUser currentUser;

    public ItemController(CatalogueService catalogue, CurrentUser currentUser) {
        this.catalogue = catalogue;
        this.currentUser = currentUser;
    }

    // UC7: seller uploads item
    @PostMapping
    public CreateItemResponse create(@Valid @RequestBody CreateItemRequest req, HttpServletRequest httpReq) {
        String username = currentUser.requireUsername(httpReq);
        return catalogue.createItem(req, username);
    }

    // UC2.1: search
    @GetMapping
    public List<ItemSummaryResponse> search(@RequestParam(required = false) String keyword) {
        return catalogue.search(keyword);
    }

    // UC2.2/UC2.3: view item details (then bid)
    @GetMapping("/{itemId}")
    public ItemDetailResponse get(@PathVariable Long itemId) {
        return catalogue.getItem(itemId);
    }
}