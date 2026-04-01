package com.eecs4413.javabrew.ai.service;

import com.eecs4413.javabrew.ai.dto.AdminAiChatResponse;
import com.eecs4413.javabrew.common.exception.ApiException;
import com.eecs4413.javabrew.iam.model.Role;
import com.eecs4413.javabrew.iam.model.User;
import com.eecs4413.javabrew.iam.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminAiService {

    private static final String SYSTEM_PROMPT = """
            You are JavaBrew Admin Copilot, a concise analytics assistant for an auction marketplace.
            You only help with business and admin questions about the marketplace.
            Use the provided tools whenever the user asks about live auction data, bid activity, item counts, or trends.
            Do not invent numbers. If the data is not available in a tool, say so clearly.
            Keep answers short, practical, and suitable for a business admin dashboard.
            """;

    private final UserRepository users;
    private final AdminAiAnalyticsService analytics;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public AdminAiService(
            UserRepository users,
            AdminAiAnalyticsService analytics,
            ObjectMapper objectMapper,
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model:gpt-4.1-mini}") String model,
            @Value("${openai.base-url:https://api.openai.com/v1}") String baseUrl) {
        this.users = users;
        this.analytics = analytics;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public AdminAiChatResponse chat(String username, String userMessage) {
        requireAdmin(username);

        if (apiKey.isBlank()) {
            return new AdminAiChatResponse(
                    "AI assistant is not configured yet. Set OPENAI_API_KEY on the backend to enable admin chat.",
                    false
            );
        }

        try {
            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(systemMessage(SYSTEM_PROMPT));
            messages.add(userMessage(userMessage));

            for (int i = 0; i < 4; i++) {
                JsonNode messageNode = createChatCompletion(messages);
                JsonNode toolCalls = messageNode.path("tool_calls");

                if (toolCalls.isArray() && toolCalls.size() > 0) {
                    messages.add(toolCallAssistantMessage(toolCalls));

                    for (JsonNode toolCall : toolCalls) {
                        String toolName = toolCall.path("function").path("name").asText("");
                        String arguments = toolCall.path("function").path("arguments").asText("{}");
                        String toolCallId = toolCall.path("id").asText("");

                        String toolResult = executeTool(toolName, arguments);
                        messages.add(toolResultMessage(toolCallId, toolResult));
                    }
                    continue;
                }

                String content = messageNode.path("content").asText("").trim();
                if (!content.isEmpty()) {
                    return new AdminAiChatResponse(content, true);
                }
            }

            throw ApiException.badRequest("AI assistant could not produce a response");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ApiException.badRequest("AI request interrupted");
        } catch (IOException e) {
            throw ApiException.badRequest("AI request failed: " + e.getMessage());
        }
    }

    private void requireAdmin(String username) {
        User user = users.findByUsername(username)
                .orElseThrow(() -> ApiException.unauthorized("User not found"));
        if (user.getRole() != Role.ADMIN) {
            throw ApiException.forbidden("Admin access required");
        }
    }

    private JsonNode createChatCompletion(List<Map<String, Object>> messages) throws IOException, InterruptedException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("messages", messages);
        payload.put("tools", toolDefinitions());
        payload.put("tool_choice", "auto");
        payload.put("temperature", 0.2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw ApiException.badRequest("OpenAI API error: " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw ApiException.badRequest("OpenAI API returned no choices");
        }

        return choices.get(0).path("message");
    }

    private List<Map<String, Object>> toolDefinitions() {
        List<Map<String, Object>> tools = new ArrayList<>();
        tools.add(functionTool(
                "get_admin_stats",
                "Get high-level marketplace counts and business metrics for the admin dashboard.",
                Map.of("type", "object", "properties", Map.of(), "additionalProperties", false)
        ));
        tools.add(functionTool(
                "list_auctions_by_status",
                "List auction items for a specific status such as ACTIVE, ENDED, or REMOVED_NO_BIDS.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "status", Map.of(
                                        "type", "string",
                                        "enum", List.of("ACTIVE", "ENDED", "REMOVED_NO_BIDS")
                                ),
                                "limit", Map.of(
                                        "type", "integer"
                                )
                        ),
                        "required", List.of("status"),
                        "additionalProperties", false
                )
        ));
        tools.add(functionTool(
                "get_top_auctions_by_bid",
                "Get the auctions with the highest current bid values.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "limit", Map.of("type", "integer")
                        ),
                        "additionalProperties", false
                )
        ));
        tools.add(functionTool(
                "get_items_with_no_bids",
                "Get items that currently have no bids yet.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "limit", Map.of("type", "integer")
                        ),
                        "additionalProperties", false
                )
        ));
        tools.add(functionTool(
                "get_bid_activity_summary",
                "Get aggregate bid activity metrics and the most recent bids.",
                Map.of("type", "object", "properties", Map.of(), "additionalProperties", false)
        ));
        return tools;
    }

    private Map<String, Object> functionTool(String name, String description, Map<String, Object> parameters) {
        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", name);
        function.put("description", description);
        function.put("parameters", parameters);

        Map<String, Object> tool = new LinkedHashMap<>();
        tool.put("type", "function");
        tool.put("function", function);
        return tool;
    }

    private String executeTool(String toolName, String argumentsJson) throws JsonProcessingException {
        JsonNode args = objectMapper.readTree(argumentsJson == null || argumentsJson.isBlank() ? "{}" : argumentsJson);
        int limit = clampLimit(args.path("limit").asInt(5));

        Object result = switch (toolName) {
            case "get_admin_stats" -> analytics.getAdminStats();
            case "list_auctions_by_status" -> analytics.listAuctionsByStatus(args.path("status").asText("ACTIVE"), limit);
            case "get_top_auctions_by_bid" -> analytics.getTopAuctionsByBid(limit);
            case "get_items_with_no_bids" -> analytics.getItemsWithNoBids(limit);
            case "get_bid_activity_summary" -> analytics.getBidActivitySummary();
            default -> Map.of("error", "Unknown tool: " + toolName);
        };

        return objectMapper.writeValueAsString(result);
    }

    private int clampLimit(int limit) {
        if (limit <= 0) return 5;
        return Math.min(limit, 10);
    }

    private Map<String, Object> systemMessage(String text) {
        return Map.of("role", "system", "content", text);
    }

    private Map<String, Object> userMessage(String text) {
        return Map.of("role", "user", "content", text);
    }

    private Map<String, Object> toolCallAssistantMessage(JsonNode toolCalls) {
        Map<String, Object> assistantMessage = new LinkedHashMap<>();
        assistantMessage.put("role", "assistant");
        assistantMessage.put("content", "");
        assistantMessage.put("tool_calls", objectMapper.convertValue(toolCalls, List.class));
        return assistantMessage;
    }

    private Map<String, Object> toolResultMessage(String toolCallId, String content) {
        Map<String, Object> toolMessage = new LinkedHashMap<>();
        toolMessage.put("role", "tool");
        toolMessage.put("tool_call_id", toolCallId);
        toolMessage.put("content", content);
        return toolMessage;
    }
}
