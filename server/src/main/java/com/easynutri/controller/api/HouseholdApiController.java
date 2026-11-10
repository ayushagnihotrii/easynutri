package com.easynutri.controller.api;

import com.easynutri.dto.*;
import com.easynutri.service.ActiveProfileContextService;
import com.easynutri.service.HouseholdMealPlanService;
import com.easynutri.service.HouseholdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/household")
@Tag(
    name = "Family & Household",
    description =
        "Endpoints for household management, family member profiles, and shared meal planning")
public class HouseholdApiController {

  private final HouseholdService householdService;
  private final HouseholdMealPlanService householdMealPlanService;
  private final ActiveProfileContextService activeProfileContextService;

  public HouseholdApiController(
      HouseholdService householdService,
      HouseholdMealPlanService householdMealPlanService,
      ActiveProfileContextService activeProfileContextService) {
    this.householdService = householdService;
    this.householdMealPlanService = householdMealPlanService;
    this.activeProfileContextService = activeProfileContextService;
  }

  @GetMapping
  @Operation(summary = "Get current user's household and member profiles")
  public ResponseEntity<HouseholdDto> getHousehold(Principal principal) {
    Long authUserId = activeProfileContextService.getAuthenticatedUserId(principal);
    HouseholdDto household =
        householdService
            .getHouseholdForUser(authUserId)
            .orElseGet(() -> householdService.createHousehold(authUserId, null));
    return ResponseEntity.ok(household);
  }

  @PostMapping
  @Operation(summary = "Create a new family household")
  public ResponseEntity<HouseholdDto> createHousehold(
      @RequestBody CreateHouseholdRequest req, Principal principal) {
    Long authUserId = activeProfileContextService.getAuthenticatedUserId(principal);
    return ResponseEntity.ok(householdService.createHousehold(authUserId, req.name()));
  }

  @PostMapping("/members")
  @Operation(summary = "Add a family member or dependent profile to household")
  public ResponseEntity<HouseholdMemberDto> addMember(
      @RequestParam("householdId") Long householdId,
      @RequestBody AddHouseholdMemberRequest req,
      Principal principal) {
    Long authUserId = activeProfileContextService.getAuthenticatedUserId(principal);
    return ResponseEntity.ok(householdService.addMember(authUserId, householdId, req));
  }

  @PostMapping("/switch/{profileId}")
  @Operation(summary = "Switch active profile context in session with strict authorization check")
  public ResponseEntity<Map<String, Object>> switchProfile(
      @PathVariable("profileId") Long profileId, Principal principal, HttpSession session) {
    activeProfileContextService.switchActiveProfile(principal, session, profileId);
    return ResponseEntity.ok(Map.of("activeProfileId", profileId, "switched", true));
  }

  @GetMapping("/recommendations")
  @Operation(
      summary = "Get household meal recommendations filtered by combined member restrictions")
  public ResponseEntity<List<HouseholdRecipeRecommendationDto>> getRecommendations(
      @RequestParam("householdId") Long householdId,
      @RequestParam(value = "memberIds", required = false) List<Long> memberIds,
      Principal principal) {
    Long authUserId = activeProfileContextService.getAuthenticatedUserId(principal);
    if (!householdService.isMemberOfHousehold(authUserId, householdId)) {
      throw new SecurityException("User does not belong to household #" + householdId);
    }
    return ResponseEntity.ok(
        householdMealPlanService.recommendHouseholdMeals(householdId, memberIds));
  }

  @PostMapping("/schedule")
  @Operation(summary = "Schedule a household meal and synchronize individual portions")
  public ResponseEntity<HouseholdMealPlanDto> scheduleMeal(
      @RequestBody HouseholdMealPlanRequest req, Principal principal) {
    Long authUserId = activeProfileContextService.getAuthenticatedUserId(principal);
    return ResponseEntity.ok(householdMealPlanService.scheduleHouseholdMeal(authUserId, req));
  }
}
