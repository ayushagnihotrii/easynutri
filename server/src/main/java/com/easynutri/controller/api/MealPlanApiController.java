package com.easynutri.controller.api;

import com.easynutri.dto.AddMealPlanRequest;
import com.easynutri.dto.MealPlanItemDto;
import com.easynutri.dto.RecipeDetailDto;
import com.easynutri.dto.ReplaceMealRequest;
import com.easynutri.dto.WeeklyMealPlanResponseDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.MealPlanService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/meal-plan")
public class MealPlanApiController {

  private final MealPlanService mealPlanService;
  private final UserRepository userRepository;

  public MealPlanApiController(MealPlanService mealPlanService, UserRepository userRepository) {
    this.mealPlanService = mealPlanService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public ResponseEntity<List<MealPlanItemDto>> getPlan(
      @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      Principal principal) {
    Long userId = getUserId(principal);
    LocalDate targetDate = date != null ? date : LocalDate.now();
    return ResponseEntity.ok(mealPlanService.getMealPlanForDate(userId, targetDate));
  }

  @GetMapping("/weekly")
  public ResponseEntity<WeeklyMealPlanResponseDto> getWeeklyPlan(
      @RequestParam(value = "weekStart", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate weekStart,
      Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(mealPlanService.getWeeklyPlan(userId, weekStart));
  }

  @PostMapping("/generate")
  public ResponseEntity<WeeklyMealPlanResponseDto> generateWeeklyPlan(
      @RequestParam(value = "weekStart", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate weekStart,
      Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(mealPlanService.generateIntelligentWeek(userId, weekStart));
  }

  @PostMapping("/add")
  public ResponseEntity<MealPlanItemDto> addMeal(
      @Valid @RequestBody AddMealPlanRequest request, Principal principal) {
    Long userId = getUserId(principal);
    MealPlanItemDto item = mealPlanService.addMealPlanItem(userId, request);
    return ResponseEntity.ok(item);
  }

  @PostMapping("/replace")
  public ResponseEntity<MealPlanItemDto> replaceMeal(
      @RequestBody ReplaceMealRequest request, Principal principal) {
    Long userId = getUserId(principal);
    MealPlanItemDto item =
        mealPlanService.replaceMeal(userId, request.mealPlanItemId(), request.newRecipeId());
    return ResponseEntity.ok(item);
  }

  @GetMapping("/{id}/alternatives")
  public ResponseEntity<List<RecipeDetailDto>> getAlternatives(
      @PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(mealPlanService.getReplacementAlternatives(userId, id));
  }

  @PostMapping("/{id}/lock")
  public ResponseEntity<Map<String, Object>> toggleLock(
      @PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    boolean locked = mealPlanService.toggleLock(userId, id);
    return ResponseEntity.ok(Map.of("id", id, "locked", locked));
  }

  @PostMapping("/{id}/complete")
  public ResponseEntity<Map<String, Object>> toggleComplete(
      @PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    boolean completed = mealPlanService.toggleComplete(userId, id);
    return ResponseEntity.ok(Map.of("id", id, "completed", completed));
  }

  @PutMapping("/{id}/servings")
  public ResponseEntity<Map<String, Object>> updateServings(
      @PathVariable("id") Long id, @RequestParam("servings") int servings, Principal principal) {
    Long userId = getUserId(principal);
    mealPlanService.updateServings(userId, id, servings);
    return ResponseEntity.ok(Map.of("id", id, "servings", servings));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Object>> deleteMeal(
      @PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    mealPlanService.removeMealPlanItem(userId, id);
    return ResponseEntity.ok(Map.of("success", true, "id", id));
  }

  private Long getUserId(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
