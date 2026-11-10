package com.easynutri.controller.api;

import com.easynutri.dto.CopyMealRequest;
import com.easynutri.dto.DiaryDayDto;
import com.easynutri.dto.DiaryItemDto;
import com.easynutri.dto.LogFoodItemRequest;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/diary")
@Tag(
    name = "Food Diary",
    description = "Endpoints for daily nutritional logging and macro tracking")
public class DiaryApiController {

  private final DiaryService diaryService;
  private final UserRepository userRepository;

  public DiaryApiController(DiaryService diaryService, UserRepository userRepository) {
    this.diaryService = diaryService;
    this.userRepository = userRepository;
  }

  @GetMapping
  @Operation(summary = "Get daily food diary with slot breakdowns and macro totals")
  public ResponseEntity<DiaryDayDto> getDayDiary(
      @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(diaryService.getDayDiary(userId, date));
  }

  @PostMapping("/items")
  @Operation(summary = "Log a food portion or recipe portion to a meal slot")
  public ResponseEntity<DiaryItemDto> logItem(
      @RequestBody LogFoodItemRequest request, Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(diaryService.logItem(userId, request));
  }

  @DeleteMapping("/items/{id}")
  @Operation(summary = "Delete a logged food item portion")
  public ResponseEntity<Void> deleteItem(@PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    diaryService.deleteItem(userId, id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/duplicate")
  @Operation(summary = "Duplicate/copy a meal slot to another date or slot")
  public ResponseEntity<Void> duplicateMeal(
      @RequestBody CopyMealRequest request, Principal principal) {
    Long userId = getUserId(principal);
    diaryService.duplicateMeal(userId, request);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/log-cooking/{sessionId}")
  @Operation(summary = "Log completed cooking session meal directly to diary")
  public ResponseEntity<DiaryItemDto> logCookingSession(
      @PathVariable("sessionId") Long sessionId,
      @RequestParam(value = "slot", required = false) String slot,
      Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(diaryService.logCookingSessionMeal(userId, sessionId, slot));
  }

  private Long getUserId(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
