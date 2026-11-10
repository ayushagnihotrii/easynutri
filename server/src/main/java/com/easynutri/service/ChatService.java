package com.easynutri.service;

import com.easynutri.entity.AiConversationEntity;
import com.easynutri.entity.AiMessageEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.AiConversationRepository;
import com.easynutri.repository.AiMessageRepository;
import com.easynutri.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChatService {

  private final AiConversationRepository conversationRepository;
  private final AiMessageRepository messageRepository;
  private final UserRepository userRepository;

  public ChatService(
      AiConversationRepository conversationRepository,
      AiMessageRepository messageRepository,
      UserRepository userRepository) {
    this.conversationRepository = conversationRepository;
    this.messageRepository = messageRepository;
    this.userRepository = userRepository;
  }

  public AiConversationEntity getOrCreateActiveConversation(Long userId) {
    List<AiConversationEntity> list =
        conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    if (!list.isEmpty()) {
      return list.get(0);
    }
    return createNewConversation(userId, "Nutrition Consultation");
  }

  public AiConversationEntity createNewConversation(Long userId, String title) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    AiConversationEntity conversation = new AiConversationEntity(user, title);
    return conversationRepository.save(conversation);
  }

  @Transactional(readOnly = true)
  public List<AiConversationEntity> getUserConversations(Long userId) {
    return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
  }

  @Transactional(readOnly = true)
  public Optional<AiConversationEntity> getConversationById(Long conversationId, Long userId) {
    return conversationRepository
        .findById(conversationId)
        .filter(c -> c.getUser().getId().equals(userId));
  }

  public void deleteConversation(Long conversationId, Long userId) {
    conversationRepository
        .findById(conversationId)
        .filter(c -> c.getUser().getId().equals(userId))
        .ifPresent(conversationRepository::delete);
  }

  public AiMessageEntity saveUserMessage(AiConversationEntity conversation, String content) {
    AiMessageEntity msg =
        new AiMessageEntity(
            conversation, "USER", content, "[]", "ALLOW", "User provided query.", false);
    conversation.addMessage(msg);
    conversation.setUpdatedAt(Instant.now());
    conversationRepository.save(conversation);
    return messageRepository.save(msg);
  }

  public AiMessageEntity saveAssistantMessage(
      AiConversationEntity conversation,
      String content,
      String executedToolsJson,
      String safetyVerdict,
      String safetyNotes,
      boolean deterministic) {
    AiMessageEntity msg =
        new AiMessageEntity(
            conversation,
            "ASSISTANT",
            content,
            executedToolsJson,
            safetyVerdict,
            safetyNotes,
            deterministic);
    conversation.addMessage(msg);
    conversation.setUpdatedAt(Instant.now());
    conversationRepository.save(conversation);
    return messageRepository.save(msg);
  }
}
