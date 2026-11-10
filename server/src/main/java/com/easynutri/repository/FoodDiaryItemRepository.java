package com.easynutri.repository;

import com.easynutri.entity.FoodDiaryItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodDiaryItemRepository extends JpaRepository<FoodDiaryItemEntity, Long> {

  List<FoodDiaryItemEntity> findByEntryId(Long entryId);
}
