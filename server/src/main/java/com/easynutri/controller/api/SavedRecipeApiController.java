package com.easynutri.controller.api;

import com.easynutri.dto.CreateCollectionRequest;
import com.easynutri.dto.RecipeCollectionDto;
import com.easynutri.dto.SaveRecipeRequest;
import com.easynutri.dto.SavedRecipeDto;
import com.easynutri.dto.UpdateRecipeNoteRequest;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.RecipeCollectionService;
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
  private final RecipeCollectionService collectionService;
  private final UserRepository userRepository;

  public SavedRecipeApiController(
      SavedRecipeService savedRecipeService,
      RecipeCollectionService collectionService,
      UserRepository userRepository) {
    this.savedRecipeService = savedRecipeService;
    this.collectionService = collectionService;
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

  @PutMapping("/notes")
  public ResponseEntity<Map<String, Object>> updateNotes(
      @RequestBody UpdateRecipeNoteRequest request, Principal principal) {
    Long userId = getUserId(principal);
    savedRecipeService.updateNotes(userId, request.recipeId(), request.notes());
    return ResponseEntity.ok(Map.of("success", true, "recipeId", request.recipeId()));
  }

  @GetMapping("/collections")
  public ResponseEntity<List<RecipeCollectionDto>> getCollections(Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(collectionService.getUserCollections(userId));
  }

  @PostMapping("/collections")
  public ResponseEntity<RecipeCollectionDto> createCollection(
      @RequestBody CreateCollectionRequest request, Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(collectionService.createCollection(userId, request.name()));
  }

  @DeleteMapping("/collections/{id}")
  public ResponseEntity<Map<String, Object>> deleteCollection(
      @PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    collectionService.deleteCollection(userId, id);
    return ResponseEntity.ok(Map.of("success", true, "id", id));
  }

  @PostMapping("/collections/{id}/add")
  public ResponseEntity<Map<String, Object>> addRecipeToCollection(
      @PathVariable("id") Long id, @RequestBody Map<String, String> payload, Principal principal) {
    Long userId = getUserId(principal);
    String recipeId = payload.get("recipeId");
    collectionService.addRecipeToCollection(userId, id, recipeId);
    return ResponseEntity.ok(Map.of("success", true, "collectionId", id, "recipeId", recipeId));
  }

  @PostMapping("/collections/{id}/remove")
  public ResponseEntity<Map<String, Object>> removeRecipeFromCollection(
      @PathVariable("id") Long id, @RequestBody Map<String, String> payload, Principal principal) {
    Long userId = getUserId(principal);
    String recipeId = payload.get("recipeId");
    collectionService.removeRecipeFromCollection(userId, id, recipeId);
    return ResponseEntity.ok(Map.of("success", true, "collectionId", id, "recipeId", recipeId));
  }

  private Long getUserId(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
