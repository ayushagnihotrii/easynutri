package com.easynutri.controller.api;

import com.easynutri.dto.OnboardingRequest;
import com.easynutri.dto.ProfileUpdateRequest;
import com.easynutri.dto.UserProfileDto;
import com.easynutri.entity.UserProfileEntity;
import com.easynutri.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(
    name = "User Profile & Onboarding",
    description = "Endpoints for biometric profile and clinical targets")
public class ProfileApiController {

  private final UserProfileService profileService;

  public ProfileApiController(UserProfileService profileService) {
    this.profileService = profileService;
  }

  @GetMapping
  @Operation(summary = "Get user profile and computed nutritional targets")
  public ResponseEntity<UserProfileDto> getProfile(Principal principal) {
    String username = getUsername(principal);
    return profileService
        .findByUsername(username)
        .map(p -> ResponseEntity.ok(toDto(p, username)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/onboarding")
  @Operation(summary = "Submit onboarding wizard setup and calculate personalized targets")
  public ResponseEntity<UserProfileDto> submitOnboarding(
      @Valid @RequestBody OnboardingRequest request, Principal principal) {
    String username = getUsername(principal);
    UserProfileEntity profile = profileService.saveOnboardingProfile(username, request);
    return ResponseEntity.ok(toDto(profile, username));
  }

  @PutMapping
  @Operation(summary = "Update biometric parameters and recalculate dietary goals")
  public ResponseEntity<UserProfileDto> updateProfile(
      @Valid @RequestBody ProfileUpdateRequest request, Principal principal) {
    String username = getUsername(principal);
    UserProfileEntity profile = profileService.updateProfile(username, request);
    return ResponseEntity.ok(toDto(profile, username));
  }

  @PostMapping("/sensitive-mode")
  @Operation(summary = "Toggle or explicitly set eating-disorder sensitive-user mode")
  public ResponseEntity<Map<String, Object>> toggleSensitiveMode(
      @RequestParam(value = "enabled", required = false) Boolean enabled, Principal principal) {
    String username = getUsername(principal);
    UserProfileEntity profile =
        profileService
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + username));

    boolean newStatus = enabled != null ? enabled : !profile.isSensitiveMode();
    ProfileUpdateRequest update =
        new ProfileUpdateRequest(
            profile.getAge(),
            profile.getBiologicalSex(),
            profile.getHeightCm(),
            profile.getWeightKg(),
            profile.getActivityLevel(),
            profile.getHealthGoal(),
            profile.getDietaryPreference(),
            profile.isPregnant(),
            newStatus,
            profile.getAllergies().stream().toList(),
            profile.getHealthConditions().stream().toList());
    profileService.updateProfile(username, update);
    return ResponseEntity.ok(Map.of("success", true, "sensitiveMode", newStatus));
  }

  private String getUsername(Principal principal) {
    if (principal == null) {
      throw new IllegalStateException("Authentication required");
    }
    return principal.getName();
  }

  private UserProfileDto toDto(UserProfileEntity p, String username) {
    return new UserProfileDto(
        p.getId(),
        username,
        p.getAge(),
        p.getBiologicalSex(),
        p.getHeightCm(),
        p.getWeightKg(),
        p.getActivityLevel(),
        p.getHealthGoal(),
        p.getDietaryPreference(),
        p.isPregnant(),
        p.isSensitiveMode(),
        p.isOnboardingCompleted(),
        p.getTargetCalories(),
        p.getTargetProteinG(),
        p.getTargetCarbG(),
        p.getTargetFatG(),
        p.getTargetWaterL(),
        p.getAllergies(),
        p.getHealthConditions());
  }
}
