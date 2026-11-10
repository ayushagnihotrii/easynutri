package com.easynutri.repository;

import com.easynutri.entity.PantryItemEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PantryItemRepository extends JpaRepository<PantryItemEntity, Long> {

  List<PantryItemEntity> findByUserUsername(String username);

  List<PantryItemEntity> findByUserUsernameAndExpirationDateLessThanEqual(
      String username, LocalDate date);

  List<PantryItemEntity> findByUserId(Long userId);

  List<PantryItemEntity> findByUserIdOrderByExpirationDateAsc(Long userId);

  Optional<PantryItemEntity> findByUserIdAndFoodId(Long userId, String foodId);

  Optional<PantryItemEntity> findByIdAndUserId(Long id, Long userId);

  List<PantryItemEntity> findByExpirationDateLessThanEqual(LocalDate date);

  void deleteByUserId(Long userId);
}
