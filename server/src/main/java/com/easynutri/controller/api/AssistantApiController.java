package com.easynutri.controller.api;

import com.easynutri.ai.AssistantResponse;
import com.easynutri.ai.LlmClient;
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

  public AssistantApiController(LlmClient llmClient) {
    this.llmClient = llmClient;
  }

  @PostMapping("/chat")
  @Operation(summary = "Ask a nutrition question verified through pure Java rules")
  public ResponseEntity<AssistantResponse> ask(
      @RequestBody ChatPromptRequest request, Principal principal) {
    String userId = principal != null ? principal.getName() : "guest";
    AssistantResponse response = llmClient.processQuery(request.prompt(), userId);
    return ResponseEntity.ok(response);
  }

  public record ChatPromptRequest(String prompt) {}
}
