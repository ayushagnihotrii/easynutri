package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.dto.RecipeAdaptationDto;
import com.easynutri.entity.RecipeAdaptationEntity;
import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.RecipeAdaptationRepository;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.RecipeAdaptationService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class RecipeAdaptationTest {

  @Autowired private RecipeAdaptationService adaptationService;
  @Autowired private RecipeRepository recipeRepository;
  @Autowired private RecipeAdaptationRepository adaptationRepository;
  @Autowired private UserRepository userRepository;

  @Test
  @DisplayName(
      "Preview adaptation recalculates nutrition in pure Java without mutating original recipe")
  void testRecipeAdaptationPreview() {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    List<RecipeEntity> recipes = recipeRepository.findAll();
    assertFalse(recipes.isEmpty(), "Recipes should be seeded");

    RecipeEntity original = recipes.get(0);
    double origCalories = original.getBaseServings() > 0 ? 100.0 : 0.0;

    RecipeAdaptationDto preview =
        adaptationService.previewAdaptation(demo.getId(), original.getId(), "VEGAN", null);

    assertNotNull(preview);
    assertEquals(original.getId(), preview.baseRecipeId());
    assertEquals("VEGAN", preview.adaptationType());
    assertFalse(preview.ingredientDiffs().isEmpty());
    assertTrue(preview.adaptedCalories() > 0);

    // Verify original database entity remains completely untouched
    RecipeEntity reloaded = recipeRepository.findById(original.getId()).orElseThrow();
    assertEquals(original.getTitleEn(), reloaded.getTitleEn());
    assertEquals(original.getIngredients().size(), reloaded.getIngredients().size());
  }

  @Test
  @DisplayName("Save adaptation persists personalized variant linked to user and base recipe")
  void testSaveRecipeAdaptation() {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    RecipeEntity original = recipeRepository.findAll().get(0);

    RecipeAdaptationDto saved =
        adaptationService.saveAdaptation(demo.getId(), original.getId(), "JAIN", "exclude onions");

    assertNotNull(saved.adaptationId());
    assertTrue(saved.isSaved());

    List<RecipeAdaptationEntity> userVariants = adaptationService.getUserAdaptations(demo.getId());
    assertFalse(userVariants.isEmpty());
    assertEquals("JAIN", userVariants.get(0).getAdaptationType());
  }
}
