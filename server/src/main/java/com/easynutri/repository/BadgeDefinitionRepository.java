package com.easynutri.repository;

import com.easynutri.entity.BadgeDefinitionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeDefinitionRepository extends JpaRepository<BadgeDefinitionEntity, String> {

  List<BadgeDefinitionEntity> findByCategory(String category);
}
