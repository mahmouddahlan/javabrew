package com.eecs4413.javabrew.ai.controller;

import com.eecs4413.javabrew.ai.dto.AdminAiChatRequest;
import com.eecs4413.javabrew.ai.dto.AdminAiChatResponse;
import com.eecs4413.javabrew.ai.service.AdminAiService;
import com.eecs4413.javabrew.iam.service.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ai")
public class AdminAiController {

    private final AdminAiService adminAiService;
    private final CurrentUser currentUser;

    public AdminAiController(AdminAiService adminAiService, CurrentUser currentUser) {
        this.adminAiService = adminAiService;
        this.currentUser = currentUser;
    }

    @PostMapping("/chat")
    public AdminAiChatResponse chat(@Valid @RequestBody AdminAiChatRequest req, HttpServletRequest httpReq) {
        String username = currentUser.requireUsername(httpReq);
        return adminAiService.chat(username, req.message);
    }
}
