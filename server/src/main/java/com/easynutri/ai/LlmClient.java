package com.easynutri.ai;

/** Provider-independent contract for LLM assistant requests. */
public interface LlmClient {

  /**
   * Processes a user's natural language query using verified application tools.
   *
   * @param query user's prompt or question
   * @param userId authenticated user ID
   * @return verified assistant response detailing tool executions and clinical safety verdict
   */
  AssistantResponse processQuery(String query, String userId);

  /**
   * Indicates whether a real remote LLM provider (e.g. Gemini, OpenAI) is configured.
   *
   * @return true if remote API key is active, false if operating in deterministic demo mode
   */
  boolean isRemoteConnected();
}
