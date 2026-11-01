package com.easynutri.ai;

import java.time.Instant;
import java.util.List;

/** Represents a verified AI assistant response with audited tool executions. */
public record AssistantResponse(
    String content,
    List<String> toolsExecuted,
    boolean safetyPassed,
    String safetyNotes,
    boolean deterministicDemoMode,
    Instant timestamp) {}
