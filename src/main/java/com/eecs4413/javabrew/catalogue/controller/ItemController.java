package com.eecs4413.javabrew.catalogue.controller;

import com.eecs4413.javabrew.catalogue.dto.;
import com.eecs4413.javabrew.catalogue.service.CatalogueService;
import com.eecs4413.javabrew.common.exception.ApiException;
import com.eecs4413.javabrew.iam.service.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.;

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

    @PostMapping
    public ResponseEntity<CreateItemResponse> create(
            @Valid @RequestBody CreateItemRequest req,
            HttpServletRequest httpReq) {
        String username = currentUser.requireUsername(httpReq);
        CreateItemResponse response = catalogue.createItem(req, username);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public List<ItemSummaryResponse> search(@RequestParam(required = false) String keyword) {
        return catalogue.search(keyword);
    }

    @GetMapping("/{itemId}")
    public ItemDetailResponse get(@PathVariable Long itemId) {
        return catalogue.getItem(itemId);
    }

    @GetMapping("/won")
    public List<ItemSummaryResponse> wonItems(HttpServletRequest httpReq) {
        String username = currentUser.requireUsername(httpReq);
        if (username == null) throw ApiException.unauthorized("Login required");
        return catalogue.getWonItems(username);
    }
}
