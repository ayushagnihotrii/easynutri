package com.easynutri.controller.api;

import com.easynutri.dto.BadgeDto;
import com.easynutri.dto.ChallengeDto;
import com.easynutri.dto.UserStreakDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.GamificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gamification")
@Tag(
    name = "Gamification & Streaks",
    description = "Endpoints for habit streaks, health badges, and community challenges")
public class GamificationApiController {

  private final GamificationService gamificationService;
  private final UserRepository userRepository;

  public GamificationApiController(
      GamificationService gamificationService, UserRepository userRepository) {
    this.gamificationService = gamificationService;
    this.userRepository = userRepository;
  }

  @GetMapping("/streaks")
  @Operation(summary = "Get current habit streaks with 1-day grace period status")
  public ResponseEntity<List<UserStreakDto>> getStreaks(Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(gamificationService.getUserStreaks(userId));
  }

  @GetMapping("/badges")
  @Operation(summary = "Get user badges including newly unlocked habit and cooking achievements")
  public ResponseEntity<List<BadgeDto>> getBadges(Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(gamificationService.getUserBadges(userId));
  }

  @GetMapping("/challenges")
  @Operation(summary = "Get active community wellness challenges and progress")
  public ResponseEntity<List<ChallengeDto>> getChallenges(Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(gamificationService.getChallenges(userId));
  }

  @PostMapping("/challenges/{id}/join")
  @Operation(summary = "Join a community challenge")
  public ResponseEntity<Map<String, Object>> joinChallenge(
      @PathVariable("id") String id, Principal principal) {
    Long userId = getUserId(principal);
    gamificationService.joinChallenge(userId, id);
    return ResponseEntity.ok(Map.of("challengeId", id, "joined", true));
  }

  private Long getUserId(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
