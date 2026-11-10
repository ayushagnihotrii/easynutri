package com.easynutri.repository;

import com.easynutri.entity.CommunityRecipeIngredientEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityRecipeIngredientRepository
    extends JpaRepository<CommunityRecipeIngredientEntity, Long> {

  List<CommunityRecipeIngredientEntity> findByCommunityRecipeId(Long communityRecipeId);
}
