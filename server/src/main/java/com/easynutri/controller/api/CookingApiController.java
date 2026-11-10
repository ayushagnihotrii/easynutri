package com.easynutri.controller.api;

import com.easynutri.dto.CookingSessionDto;
import com.easynutri.dto.FinishCookingRequest;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.CookingService;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cook")
public class CookingApiController {

  private final CookingService cookingService;
  private final UserRepository userRepository;

  public CookingApiController(CookingService cookingService, UserRepository userRepository) {
    this.cookingService = cookingService;
    this.userRepository = userRepository;
  }

  @PostMapping("/{recipeId}/start")
  public ResponseEntity<CookingSessionDto> startSession(
      @PathVariable("recipeId") String recipeId,
      @RequestParam(value = "servings", required = false) Integer servings,
      Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(cookingService.startOrResumeSession(userId, recipeId, servings));
  }

  @GetMapping("/{sessionId}")
  public ResponseEntity<CookingSessionDto> getSession(
      @PathVariable("sessionId") Long sessionId, Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(cookingService.getSession(userId, sessionId));
  }

  @PostMapping("/{sessionId}/step")
  public ResponseEntity<CookingSessionDto> updateStep(
      @PathVariable("sessionId") Long sessionId,
      @RequestParam("step") int step,
      Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(cookingService.updateStep(userId, sessionId, step));
  }

  @PostMapping("/{sessionId}/finish")
  public ResponseEntity<CookingSessionDto> finishSession(
      @PathVariable("sessionId") Long sessionId,
      @RequestBody(required = false) FinishCookingRequest request,
      Principal principal) {
    Long userId = getUserId(principal);
    int servings = request != null && request.servings() != null ? request.servings() : 2;
    boolean deduct = request == null || request.deductPantry() == null || request.deductPantry();
    boolean diary = request == null || request.logDiary() == null || request.logDiary();
    String notes = request != null ? request.notes() : null;

    CookingSessionDto session =
        cookingService.finishCooking(userId, sessionId, servings, deduct, diary, notes);
    return ResponseEntity.ok(session);
  }

  private Long getUserId(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
