package com.easynutri.repository;

import com.easynutri.entity.GroceryItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroceryItemRepository extends JpaRepository<GroceryItemEntity, Long> {

  List<GroceryItemEntity> findByUserIdOrderByCheckedAscCreatedAtDesc(Long userId);

  void deleteByUserIdAndCheckedTrue(Long userId);

  void deleteByUserId(Long userId);
}
