package com.easynutri.dto;

import java.util.List;

public record AiChatResponseDto(
    Long conversationId,
    Long messageId,
    String content,
    List<ToolExecutionSummaryDto> toolExecutions,
    String safetyVerdict,
    String safetyNotes,
    boolean isEmergency,
    boolean isDeterministic) {

  public record ToolExecutionSummaryDto(
      String toolName, String argumentsJson, String resultJson, long executionTimeMs) {}
}
