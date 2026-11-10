package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent record of an individual tool call executed by the AI orchestrator. */
@Entity
@Table(name = "ai_tool_executions")
public class AiToolExecutionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id", nullable = false)
  private AiMessageEntity message;

  @Column(name = "tool_name", nullable = false, length = 100)
  private String toolName;

  @Column(name = "arguments_json", columnDefinition = "TEXT")
  private String argumentsJson;

  @Column(name = "result_json", columnDefinition = "TEXT")
  private String resultJson;

  @Column(name = "execution_duration_ms", nullable = false)
  private long executionDurationMs = 0;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public AiToolExecutionEntity() {}

  public AiToolExecutionEntity(
      AiMessageEntity message,
      String toolName,
      String argumentsJson,
      String resultJson,
      long executionDurationMs) {
    this.message = message;
    this.toolName = toolName;
    this.argumentsJson = argumentsJson;
    this.resultJson = resultJson;
    this.executionDurationMs = executionDurationMs;
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public AiMessageEntity getMessage() {
    return message;
  }

  public void setMessage(AiMessageEntity message) {
    this.message = message;
  }

  public String getToolName() {
    return toolName;
  }

  public void setToolName(String toolName) {
    this.toolName = toolName;
  }

  public String getArgumentsJson() {
    return argumentsJson;
  }

  public void setArgumentsJson(String argumentsJson) {
    this.argumentsJson = argumentsJson;
  }

  public String getResultJson() {
    return resultJson;
  }

  public void setResultJson(String resultJson) {
    this.resultJson = resultJson;
  }

  public long getExecutionDurationMs() {
    return executionDurationMs;
  }

  public void setExecutionDurationMs(long executionDurationMs) {
    this.executionDurationMs = executionDurationMs;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
