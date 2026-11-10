package com.easynutri.controller.api;

import com.easynutri.ai.AiOrchestrator;
import com.easynutri.ai.AssistantResponse;
import com.easynutri.ai.LlmClient;
import com.easynutri.dto.AiChatResponseDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assistant")
@Tag(name = "AI Assistant", description = "Verified AI nutrition assistant endpoints")
public class AssistantApiController {

  private final LlmClient llmClient;
  private final AiOrchestrator aiOrchestrator;
  private final UserRepository userRepository;

  public AssistantApiController(
      LlmClient llmClient, AiOrchestrator aiOrchestrator, UserRepository userRepository) {
    this.llmClient = llmClient;
    this.aiOrchestrator = aiOrchestrator;
    this.userRepository = userRepository;
  }

  @PostMapping("/chat")
  @Operation(summary = "Ask a nutrition question verified through pure Java rules")
  public ResponseEntity<AssistantResponse> ask(
      @RequestBody ChatPromptRequest request, Principal principal) {
    String userId = principal != null ? principal.getName() : "guest";
    AssistantResponse response = llmClient.processQuery(request.prompt(), userId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/conversations/chat")
  @Operation(summary = "Interactive multi-turn session with database-grounded tool execution")
  public ResponseEntity<AiChatResponseDto> chat(
      @RequestBody FullChatRequest request, Principal principal) {
    Long userId = resolveUserId(principal);
    AiChatResponseDto response =
        aiOrchestrator.handleUserQuery(userId, request.conversationId(), request.prompt());
    return ResponseEntity.ok(response);
  }

  public record ChatPromptRequest(String prompt) {}

  public record FullChatRequest(Long conversationId, String prompt) {}

  private Long resolveUserId(Principal principal) {
    if (principal == null) {
      return userRepository.findByUsername("demo").map(UserEntity::getId).orElse(1L);
    }
    return userRepository.findByUsername(principal.getName()).map(UserEntity::getId).orElse(1L);
  }
}
