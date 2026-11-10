package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Persistent chat message with role, content, executed tool references, and safety verdict. */
@Entity
@Table(name = "ai_messages")
public class AiMessageEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "conversation_id", nullable = false)
  private AiConversationEntity conversation;

  @Column(nullable = false, length = 30)
  private String role; // USER, ASSISTANT, SYSTEM

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "executed_tools", columnDefinition = "TEXT")
  private String executedTools;

  @Column(name = "safety_verdict", nullable = false, length = 30)
  private String safetyVerdict = "ALLOW"; // ALLOW, WARN, BLOCK

  @Column(name = "safety_notes", columnDefinition = "TEXT")
  private String safetyNotes;

  @Column(name = "is_deterministic", nullable = false)
  private boolean deterministic = true;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AiToolExecutionEntity> toolExecutions = new ArrayList<>();

  public AiMessageEntity() {}

  public AiMessageEntity(
      AiConversationEntity conversation,
      String role,
      String content,
      String executedTools,
      String safetyVerdict,
      String safetyNotes,
      boolean deterministic) {
    this.conversation = conversation;
    this.role = role;
    this.content = content;
    this.executedTools = executedTools;
    this.safetyVerdict = safetyVerdict != null ? safetyVerdict : "ALLOW";
    this.safetyNotes = safetyNotes;
    this.deterministic = deterministic;
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public AiConversationEntity getConversation() {
    return conversation;
  }

  public void setConversation(AiConversationEntity conversation) {
    this.conversation = conversation;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getExecutedTools() {
    return executedTools;
  }

  public void setExecutedTools(String executedTools) {
    this.executedTools = executedTools;
  }

  public String getSafetyVerdict() {
    return safetyVerdict;
  }

  public void setSafetyVerdict(String safetyVerdict) {
    this.safetyVerdict = safetyVerdict;
  }

  public String getSafetyNotes() {
    return safetyNotes;
  }

  public void setSafetyNotes(String safetyNotes) {
    this.safetyNotes = safetyNotes;
  }

  public boolean isDeterministic() {
    return deterministic;
  }

  public void setDeterministic(boolean deterministic) {
    this.deterministic = deterministic;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public List<AiToolExecutionEntity> getToolExecutions() {
    return toolExecutions;
  }

  public void setToolExecutions(List<AiToolExecutionEntity> toolExecutions) {
    this.toolExecutions = toolExecutions;
  }

  public void addToolExecution(AiToolExecutionEntity tool) {
    toolExecutions.add(tool);
    tool.setMessage(this);
  }
}
