package com.easynutri.repository;

import com.easynutri.entity.PantryItemEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PantryItemRepository extends JpaRepository<PantryItemEntity, Long> {

  List<PantryItemEntity> findByUserUsername(String username);

  List<PantryItemEntity> findByUserUsernameAndExpirationDateLessThanEqual(
      String username, LocalDate date);
}
