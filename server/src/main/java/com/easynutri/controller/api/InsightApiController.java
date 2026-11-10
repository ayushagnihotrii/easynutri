package com.easynutri.controller.api;

import com.easynutri.entity.UserEntity;
import com.easynutri.entity.WeeklyInsightEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.WeeklyInsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/insights")
@Tag(
    name = "Personalized Insights",
    description = "Weekly check-ins and evidence-based nutrition insights")
public class InsightApiController {

  private final WeeklyInsightService insightService;
  private final UserRepository userRepository;

  public InsightApiController(WeeklyInsightService insightService, UserRepository userRepository) {
    this.insightService = insightService;
    this.userRepository = userRepository;
  }

  @GetMapping
  @Operation(summary = "Get or generate weekly check-in insights")
  public ResponseEntity<List<WeeklyInsightEntity>> getInsights(
      @RequestParam(value = "date", required = false) String dateStr, Principal principal) {
    Long userId = resolveUserId(principal);
    LocalDate date =
        dateStr != null && !dateStr.isBlank() ? LocalDate.parse(dateStr) : LocalDate.now();
    List<WeeklyInsightEntity> insights = insightService.getOrGenerateInsights(userId, date);
    return ResponseEntity.ok(insights);
  }

  @PostMapping("/regenerate")
  @Operation(summary = "Force regenerate weekly insights")
  public ResponseEntity<List<WeeklyInsightEntity>> regenerate(
      @RequestParam(value = "date", required = false) String dateStr, Principal principal) {
    Long userId = resolveUserId(principal);
    LocalDate date =
        dateStr != null && !dateStr.isBlank() ? LocalDate.parse(dateStr) : LocalDate.now();
    List<WeeklyInsightEntity> insights = insightService.generateInsightsForWeek(userId, date);
    return ResponseEntity.ok(insights);
  }

  private Long resolveUserId(Principal principal) {
    if (principal == null) {
      return userRepository.findByUsername("demo").map(UserEntity::getId).orElse(1L);
    }
    return userRepository.findByUsername(principal.getName()).map(UserEntity::getId).orElse(1L);
  }
}
