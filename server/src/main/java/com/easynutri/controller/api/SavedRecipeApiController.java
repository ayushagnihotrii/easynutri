package com.easynutri.controller.api;

import com.easynutri.dto.SaveRecipeRequest;
import com.easynutri.dto.SavedRecipeDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.SavedRecipeService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recipes")
public class SavedRecipeApiController {

  private final SavedRecipeService savedRecipeService;
  private final UserRepository userRepository;

  public SavedRecipeApiController(
      SavedRecipeService savedRecipeService, UserRepository userRepository) {
    this.savedRecipeService = savedRecipeService;
    this.userRepository = userRepository;
  }

  @GetMapping("/saved")
  public ResponseEntity<List<SavedRecipeDto>> getSaved(Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(savedRecipeService.getSavedRecipes(userId));
  }

  @PostMapping("/save")
  public ResponseEntity<Map<String, Object>> saveRecipe(
      @Valid @RequestBody SaveRecipeRequest request, Principal principal) {
    Long userId = getUserId(principal);
    boolean saved = savedRecipeService.saveRecipe(userId, request.recipeId(), request.notes());
    return ResponseEntity.ok(
        Map.of("success", true, "saved", saved, "recipeId", request.recipeId()));
  }

  @PostMapping("/unsave")
  public ResponseEntity<Map<String, Object>> unsaveRecipe(
      @RequestBody Map<String, String> payload, Principal principal) {
    Long userId = getUserId(principal);
    String recipeId = payload.get("recipeId");
    boolean removed = savedRecipeService.unsaveRecipe(userId, recipeId);
    return ResponseEntity.ok(Map.of("success", true, "removed", removed, "recipeId", recipeId));
  }

  @GetMapping("/{id}/is-saved")
  public ResponseEntity<Map<String, Object>> isSaved(
      @PathVariable("id") String recipeId, Principal principal) {
    Long userId = getUserId(principal);
    boolean saved = savedRecipeService.isRecipeSaved(userId, recipeId);
    return ResponseEntity.ok(Map.of("recipeId", recipeId, "saved", saved));
  }

  private Long getUserId(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
