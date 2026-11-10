package com.easynutri.repository;

import com.easynutri.entity.AiMessageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiMessageRepository extends JpaRepository<AiMessageEntity, Long> {

  List<AiMessageEntity> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}
