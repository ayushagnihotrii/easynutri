package com.easynutri.controller.api;

import com.easynutri.dto.HydrationSummaryDto;
import com.easynutri.dto.LogWaterRequest;
import com.easynutri.dto.SaveHydrationPreferencesRequest;
import com.easynutri.dto.WaterLogDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.HydrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hydration")
@Tag(
    name = "Hydration Tracker",
    description = "Endpoints for fluid intake tracking and clinical restriction targets")
public class HydrationApiController {

  private final HydrationService hydrationService;
  private final UserRepository userRepository;

  public HydrationApiController(HydrationService hydrationService, UserRepository userRepository) {
    this.hydrationService = hydrationService;
    this.userRepository = userRepository;
  }

  @GetMapping
  @Operation(summary = "Get daily hydration summary including intake, clinical targets, and logs")
  public ResponseEntity<HydrationSummaryDto> getHydration(
      @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(hydrationService.getHydrationSummary(userId, date));
  }

  @PostMapping("/logs")
  @Operation(summary = "Log water intake portion")
  public ResponseEntity<WaterLogDto> logWater(
      @RequestBody LogWaterRequest request, Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(hydrationService.logWater(userId, request));
  }

  @DeleteMapping("/logs/{id}")
  @Operation(summary = "Delete a water log entry")
  public ResponseEntity<Void> deleteWaterLog(@PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    hydrationService.deleteWaterLog(userId, id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/preferences")
  @Operation(summary = "Update hydration preferences, clinical overrides, and reminders")
  public ResponseEntity<Void> updatePreferences(
      @RequestBody SaveHydrationPreferencesRequest request, Principal principal) {
    Long userId = getUserId(principal);
    hydrationService.savePreferences(userId, request);
    return ResponseEntity.ok().build();
  }

  private Long getUserId(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
