package com.easynutri.repository;

import com.easynutri.entity.AiConversationEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversationEntity, Long> {

  List<AiConversationEntity> findByUserIdOrderByUpdatedAtDesc(Long userId);

  Optional<AiConversationEntity> findByIdAndUserId(Long id, Long userId);
}
