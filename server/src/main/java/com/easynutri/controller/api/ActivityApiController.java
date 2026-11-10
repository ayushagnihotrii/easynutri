package com.easynutri.controller.api;

import com.easynutri.dto.ActivitySummaryDto;
import com.easynutri.dto.LogStepsRequest;
import com.easynutri.dto.LogWorkoutRequest;
import com.easynutri.dto.WorkoutLogDto;
import com.easynutri.entity.StepRecordEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.ActivityTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activity")
@Tag(
    name = "Activity & Workouts",
    description = "Endpoints for steps, workout logging, and MET expenditure")
public class ActivityApiController {

  private final ActivityTrackingService trackingService;
  private final UserRepository userRepository;

  public ActivityApiController(
      ActivityTrackingService trackingService, UserRepository userRepository) {
    this.trackingService = trackingService;
    this.userRepository = userRepository;
  }

  @GetMapping
  @Operation(summary = "Get daily activity summary including steps, workouts, and energy balance")
  public ResponseEntity<ActivitySummaryDto> getActivitySummary(
      @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(trackingService.getActivitySummary(userId, date));
  }

  @PostMapping("/steps")
  @Operation(summary = "Record or update daily step count")
  public ResponseEntity<StepRecordEntity> logSteps(
      @RequestBody LogStepsRequest request, Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(trackingService.logSteps(userId, request));
  }

  @PostMapping("/workouts")
  @Operation(summary = "Log a physical workout session")
  public ResponseEntity<WorkoutLogDto> logWorkout(
      @RequestBody LogWorkoutRequest request, Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(trackingService.logWorkout(userId, request));
  }

  @DeleteMapping("/workouts/{id}")
  @Operation(summary = "Delete a logged workout")
  public ResponseEntity<Void> deleteWorkout(@PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    trackingService.deleteWorkout(userId, id);
    return ResponseEntity.noContent().build();
  }

  private Long getUserId(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
