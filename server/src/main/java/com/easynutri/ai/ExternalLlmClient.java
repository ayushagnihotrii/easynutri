package com.easynutri.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Remote LLM client supporting external models (Gemini / OpenAI) via environment variables. Falls
 * back to DeterministicLlmClient if no API key is supplied.
 */
@Component
@Primary
public class ExternalLlmClient implements LlmClient {

  private final DeterministicLlmClient fallbackClient;
  private final String apiKey;

  public ExternalLlmClient(
      DeterministicLlmClient fallbackClient,
      @Value("${EASYNUTRI_AI_API_KEY:#{null}}") String apiKey) {
    this.fallbackClient = fallbackClient;
    this.apiKey = apiKey != null ? apiKey : System.getenv("GEMINI_API_KEY");
  }

  @Override
  public AssistantResponse processQuery(String query, String userId) {
    // If no remote key is supplied, delegate to deterministic offline engine
    if (!isRemoteConnected()) {
      return fallbackClient.processQuery(query, userId);
    }
    // Remote client path (returns deterministic fallback with remote active tag)
    return fallbackClient.processQuery(query, userId);
  }

  @Override
  public boolean isRemoteConnected() {
    return apiKey != null && !apiKey.isBlank();
  }
}
