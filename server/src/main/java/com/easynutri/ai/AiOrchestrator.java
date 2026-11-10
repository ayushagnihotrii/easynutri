package com.easynutri.ai;

import com.easynutri.dto.AiChatResponseDto;
import com.easynutri.entity.AiConversationEntity;
import com.easynutri.entity.AiMessageEntity;
import com.easynutri.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiOrchestrator {

  private final ChatService chatService;
  private final AiContextBuilder contextBuilder;
  private final AiResponseValidator responseValidator;
  private final AiToolExecutor toolExecutor;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public AiOrchestrator(
      ChatService chatService,
      AiContextBuilder contextBuilder,
      AiResponseValidator responseValidator,
      AiToolExecutor toolExecutor) {
    this.chatService = chatService;
    this.contextBuilder = contextBuilder;
    this.responseValidator = responseValidator;
    this.toolExecutor = toolExecutor;
  }

  @Transactional
  public AiChatResponseDto handleUserQuery(Long userId, Long conversationId, String query) {
    AiConversationEntity conversation =
        conversationId != null
            ? chatService
                .getConversationById(conversationId, userId)
                .orElseGet(() -> chatService.getOrCreateActiveConversation(userId))
            : chatService.getOrCreateActiveConversation(userId);

    // Save user message
    chatService.saveUserMessage(conversation, query);

    // Context & Emergency Check
    AiContextBuilder.UserAiContext context = contextBuilder.buildContext(userId);
    AiResponseValidator.ValidationResult validation =
        responseValidator.validate(query, "", context);

    if (validation.isEmergency()) {
      AiMessageEntity emergencyMsg =
          chatService.saveAssistantMessage(
              conversation,
              validation.sanitizedContent(),
              "[]",
              "BLOCK",
              "Medical emergency detected. Immediate clinical intervention advised.",
              true);

      return new AiChatResponseDto(
          conversation.getId(),
          emergencyMsg.getId(),
          validation.sanitizedContent(),
          List.of(),
          "BLOCK",
          "Medical emergency detected.",
          true,
          true);
    }

    // Create assistant message placeholder for tool linking
    AiMessageEntity assistantMsg =
        chatService.saveAssistantMessage(
            conversation,
            "Analyzing...",
            "[]",
            "ALLOW",
            "Pure Java rules verification pending.",
            true);

    List<AiChatResponseDto.ToolExecutionSummaryDto> toolSummaries = new ArrayList<>();
    String rawAnswer = planAndExecuteTools(userId, query, context, assistantMsg, toolSummaries);

    // Final response validation and sensitive mode sanitization
    AiResponseValidator.ValidationResult finalValidation =
        responseValidator.validate(query, rawAnswer, context);

    assistantMsg.setContent(finalValidation.sanitizedContent());
    assistantMsg.setSafetyVerdict(finalValidation.safetyStatus());

    List<String> executedToolNames =
        toolSummaries.stream().map(AiChatResponseDto.ToolExecutionSummaryDto::toolName).toList();
    try {
      assistantMsg.setExecutedTools(objectMapper.writeValueAsString(executedToolNames));
    } catch (Exception ignored) {
      assistantMsg.setExecutedTools("[]");
    }

    return new AiChatResponseDto(
        conversation.getId(),
        assistantMsg.getId(),
        finalValidation.sanitizedContent(),
        toolSummaries,
        finalValidation.safetyStatus(),
        "Verified by EasyNutri Pure Java Rules Engine & Safety Gate.",
        false,
        true);
  }

  private String planAndExecuteTools(
      Long userId,
      String query,
      AiContextBuilder.UserAiContext context,
      AiMessageEntity assistantMsg,
      List<AiChatResponseDto.ToolExecutionSummaryDto> toolSummaries) {
    String lower = query != null ? query.toLowerCase(Locale.ROOT) : "";

    StringBuilder sb = new StringBuilder();

    // 1. Pantry & Expiry intent
    if (lower.contains("pantry") || lower.contains("expir") || lower.contains("spoil")) {
      String toolResult =
          executeAndRecord(
              userId, assistantMsg, "get_expiring_items", Map.of("daysAhead", 4), toolSummaries);
      sb.append("### 🥫 Pantry & Expiry Overview\n\n");
      sb.append(toolResult).append("\n\n");
      sb.append(
          "💡 *Tip: You can use our [Ingredient Recipe Finder](/ingredients) to transform these expiring ingredients into healthy meals before they spoil.*");
      return sb.toString();
    }

    // 2. Hydration intent
    if (lower.contains("water") || lower.contains("hydrat") || lower.contains("drink")) {
      String toolResult =
          executeAndRecord(userId, assistantMsg, "get_hydration_summary", Map.of(), toolSummaries);
      sb.append("### 💧 Today's Hydration Status\n\n");
      sb.append(toolResult).append("\n\n");
      sb.append(
          "Keep sipping steadily through the afternoon to support focus and metabolic health.");
      return sb.toString();
    }

    // 3. Activity / Step / Burn intent
    if (lower.contains("step")
        || lower.contains("walk")
        || lower.contains("burn")
        || lower.contains("workout")
        || lower.contains("activ")) {
      String toolResult =
          executeAndRecord(userId, assistantMsg, "get_activity_summary", Map.of(), toolSummaries);
      sb.append("### 🏃 Activity & Daily Movement\n\n");
      sb.append(toolResult).append("\n\n");
      sb.append("Consistent daily movement aids glycemic control and boosts cardiac wellness.");
      return sb.toString();
    }

    // 4. Diary / What I ate / Nutrition summary intent
    if (lower.contains("diary")
        || lower.contains("ate")
        || lower.contains("eat")
        || lower.contains("calorie")
        || lower.contains("macro")) {
      String toolResult =
          executeAndRecord(userId, assistantMsg, "get_food_diary_summary", Map.of(), toolSummaries);
      sb.append("### 🥗 Food Diary & Macronutrient Breakdown\n\n");
      sb.append(toolResult);
      return sb.toString();
    }

    // 5. Nutrient rich foods intent (protein, iron, calcium, fiber)
    if (lower.contains("protein")
        || lower.contains("iron")
        || lower.contains("calcium")
        || lower.contains("fiber")
        || lower.contains("potassium")) {
      String nutrient = "protein";
      if (lower.contains("iron")) nutrient = "iron";
      else if (lower.contains("calcium")) nutrient = "calcium";
      else if (lower.contains("fiber") || lower.contains("fibre")) nutrient = "fiber";
      else if (lower.contains("potassium")) nutrient = "potassium";

      String toolResult =
          executeAndRecord(
              userId,
              assistantMsg,
              "find_nutrient_food_sources",
              Map.of("nutrient", nutrient, "limit", 5),
              toolSummaries);
      sb.append(
          String.format(
              "### 🌱 Top %s-Rich Foods (Verified for Your Profile)\n\n", capitalize(nutrient)));
      sb.append(
          String.format(
              "Based on your %s diet and safety parameters, here are optimal whole food sources:\n\n",
              context.diet()));
      sb.append(toolResult).append("\n\n");
      sb.append("*All selections are verified against your registered allergens.*");
      return sb.toString();
    }

    // 6. Food safety check intent
    if (lower.contains("can i eat")
        || lower.contains("is it safe")
        || lower.contains("safe to eat")
        || lower.contains("allowed")) {
      String searchWord = extractFoodName(query);
      String toolResult =
          executeAndRecord(
              userId,
              assistantMsg,
              "check_food_safety",
              Map.of("foodName", searchWord),
              toolSummaries);
      sb.append("### 🛡️ Safety Verification\n\n");
      sb.append(toolResult);
      return sb.toString();
    }

    // 7. General search fallback
    String searchResult =
        executeAndRecord(
            userId,
            assistantMsg,
            "find_recipes",
            Map.of("query", query, "limit", 3),
            toolSummaries);

    sb.append("### 🍳 EasyNutri Nutrition Assistant\n\n");
    sb.append(searchResult).append("\n\n");
    sb.append(
        "All recommendations are verified directly against our canonical nutrition database and Java safety rules.\n\n");
    sb.append("You can ask me:\n");
    sb.append("• *\"What foods are rich in protein or iron?\"*\n");
    sb.append("• *\"What's expiring in my pantry?\"*\n");
    sb.append("• *\"How is my hydration today?\"*\n");
    sb.append("• *\"Can I eat peanuts with my allergies?\"*\n");
    sb.append("• *\"What can I make with lentils and spinach?\"*\n");

    return sb.toString();
  }

  private String executeAndRecord(
      Long userId,
      AiMessageEntity message,
      String toolName,
      Map<String, Object> arguments,
      List<AiChatResponseDto.ToolExecutionSummaryDto> summaries) {
    long start = System.currentTimeMillis();
    String result = toolExecutor.executeTool(userId, message, toolName, arguments);
    long duration = System.currentTimeMillis() - start;

    String argsJson;
    try {
      argsJson = objectMapper.writeValueAsString(arguments);
    } catch (Exception e) {
      argsJson = "{}";
    }

    summaries.add(
        new AiChatResponseDto.ToolExecutionSummaryDto(toolName, argsJson, result, duration));
    return result;
  }

  private String capitalize(String str) {
    if (str == null || str.isEmpty()) return "";
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }

  private String extractFoodName(String query) {
    String clean =
        query.replaceAll("(?i)(can i eat|is it safe to eat|is|safe|for me|\\?)", "").trim();
    return clean.isEmpty() ? query : clean;
  }
}
