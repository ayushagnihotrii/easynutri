package com.easynutri.controller.web;

import com.easynutri.ai.AiOrchestrator;
import com.easynutri.dto.AiChatResponseDto;
import com.easynutri.entity.AiConversationEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.ChatService;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/assistant")
public class AssistantWebController {

  private final ChatService chatService;
  private final AiOrchestrator aiOrchestrator;
  private final UserRepository userRepository;

  public AssistantWebController(
      ChatService chatService, AiOrchestrator aiOrchestrator, UserRepository userRepository) {
    this.chatService = chatService;
    this.aiOrchestrator = aiOrchestrator;
    this.userRepository = userRepository;
  }

  @GetMapping
  public String assistant(
      @RequestParam(value = "conversationId", required = false) Long conversationId,
      Model model,
      Principal principal) {
    Long userId = resolveUserId(principal);
    List<AiConversationEntity> conversations = chatService.getUserConversations(userId);

    AiConversationEntity activeConversation;
    if (conversationId != null) {
      activeConversation =
          chatService
              .getConversationById(conversationId, userId)
              .orElseGet(() -> chatService.getOrCreateActiveConversation(userId));
    } else if (!conversations.isEmpty()) {
      activeConversation = conversations.get(0);
    } else {
      activeConversation = chatService.getOrCreateActiveConversation(userId);
      conversations = chatService.getUserConversations(userId);
    }

    model.addAttribute("username", principal != null ? principal.getName() : "Demo User");
    model.addAttribute("pageTitle", "AI Nutrition Assistant");
    model.addAttribute("activeNav", "assistant");
    model.addAttribute("conversations", conversations);
    model.addAttribute("activeConversation", activeConversation);
    model.addAttribute("messages", activeConversation.getMessages());
    return "assistant/index";
  }

  @PostMapping("/chat")
  public String sendQuery(
      @RequestParam("query") String query,
      @RequestParam(value = "conversationId", required = false) Long conversationId,
      Principal principal) {
    Long userId = resolveUserId(principal);
    if (query != null && !query.trim().isEmpty()) {
      AiChatResponseDto response =
          aiOrchestrator.handleUserQuery(userId, conversationId, query.trim());
      return "redirect:/assistant?conversationId=" + response.conversationId();
    }
    return "redirect:/assistant"
        + (conversationId != null ? "?conversationId=" + conversationId : "");
  }

  @PostMapping("/conversations/new")
  public String newConversation(Principal principal) {
    Long userId = resolveUserId(principal);
    AiConversationEntity created =
        chatService.createNewConversation(userId, "Nutrition Consultation");
    return "redirect:/assistant?conversationId=" + created.getId();
  }

  @PostMapping("/conversations/{id}/delete")
  public String deleteConversation(@PathVariable("id") Long id, Principal principal) {
    Long userId = resolveUserId(principal);
    chatService.deleteConversation(id, userId);
    return "redirect:/assistant";
  }

  private Long resolveUserId(Principal principal) {
    if (principal == null) {
      return userRepository.findByUsername("demo").map(UserEntity::getId).orElse(1L);
    }
    return userRepository.findByUsername(principal.getName()).map(UserEntity::getId).orElse(1L);
  }
}
