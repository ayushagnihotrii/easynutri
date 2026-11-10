package com.easynutri.controller.api;

import com.easynutri.dto.RecipeAdaptationDto;
import com.easynutri.entity.RecipeAdaptationEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.RecipeAdaptationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recipes/adaptations")
@Tag(
    name = "Recipe Adaptation",
    description = "AI recipe adaptation verified by pure Java safety rules")
public class RecipeAdaptationApiController {

  private final RecipeAdaptationService adaptationService;
  private final UserRepository userRepository;

  public RecipeAdaptationApiController(
      RecipeAdaptationService adaptationService, UserRepository userRepository) {
    this.adaptationService = adaptationService;
    this.userRepository = userRepository;
  }

  @PostMapping("/preview")
  @Operation(summary = "Preview recipe adaptation diff and recalculated nutrition")
  public ResponseEntity<RecipeAdaptationDto> preview(
      @RequestBody AdaptRecipeRequest request, Principal principal) {
    Long userId = resolveUserId(principal);
    RecipeAdaptationDto dto =
        adaptationService.previewAdaptation(
            userId, request.recipeId(), request.adaptationType(), request.customPrompt());
    return ResponseEntity.ok(dto);
  }

  @PostMapping("/save")
  @Operation(summary = "Save adapted recipe as user's personal variant")
  public ResponseEntity<RecipeAdaptationDto> save(
      @RequestBody AdaptRecipeRequest request, Principal principal) {
    Long userId = resolveUserId(principal);
    RecipeAdaptationDto dto =
        adaptationService.saveAdaptation(
            userId, request.recipeId(), request.adaptationType(), request.customPrompt());
    return ResponseEntity.ok(dto);
  }

  @GetMapping
  @Operation(summary = "List all user's adapted recipes")
  public ResponseEntity<List<RecipeAdaptationEntity>> list(Principal principal) {
    Long userId = resolveUserId(principal);
    return ResponseEntity.ok(adaptationService.getUserAdaptations(userId));
  }

  public record AdaptRecipeRequest(String recipeId, String adaptationType, String customPrompt) {}

  private Long resolveUserId(Principal principal) {
    if (principal == null) {
      return userRepository.findByUsername("demo").map(UserEntity::getId).orElse(1L);
    }
    return userRepository.findByUsername(principal.getName()).map(UserEntity::getId).orElse(1L);
  }
}
