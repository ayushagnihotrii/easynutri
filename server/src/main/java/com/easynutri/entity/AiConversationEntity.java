package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Persistent chat conversation between user and AI Nutrition Assistant. */
@Entity
@Table(name = "ai_conversations")
public class AiConversationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(nullable = false, length = 200)
  private String title = "Nutrition Consultation";

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("createdAt ASC")
  private List<AiMessageEntity> messages = new ArrayList<>();

  public AiConversationEntity() {}

  public AiConversationEntity(UserEntity user, String title) {
    this.user = user;
    this.title = title != null && !title.isBlank() ? title : "Nutrition Consultation";
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public UserEntity getUser() {
    return user;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public List<AiMessageEntity> getMessages() {
    return messages;
  }

  public void setMessages(List<AiMessageEntity> messages) {
    this.messages = messages;
  }

  public void addMessage(AiMessageEntity msg) {
    messages.add(msg);
    msg.setConversation(this);
    this.updatedAt = Instant.now();
  }
}
