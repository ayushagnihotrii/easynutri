package com.easynutri.controller.api;

import com.easynutri.dto.AddMealPlanRequest;
import com.easynutri.dto.MealPlanItemDto;
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

  @PostMapping("/add")
  public ResponseEntity<MealPlanItemDto> addMeal(
      @Valid @RequestBody AddMealPlanRequest request, Principal principal) {
    Long userId = getUserId(principal);
    MealPlanItemDto item = mealPlanService.addMealPlanItem(userId, request);
    return ResponseEntity.ok(item);
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
