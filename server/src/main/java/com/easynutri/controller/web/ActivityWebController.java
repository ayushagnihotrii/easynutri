package com.easynutri.controller.web;

import com.easynutri.domain.activity.ActivityIntensity;
import com.easynutri.domain.activity.ActivityType;
import com.easynutri.dto.ActivitySummaryDto;
import com.easynutri.dto.LogStepsRequest;
import com.easynutri.dto.LogWorkoutRequest;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.ActivityTrackingService;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/tracking", "/activity"})
public class ActivityWebController {

  private final ActivityTrackingService trackingService;
  private final UserRepository userRepository;

  public ActivityWebController(
      ActivityTrackingService trackingService, UserRepository userRepository) {
    this.trackingService = trackingService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public String activityPage(
      @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      Model model,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);
    LocalDate targetDate = date != null ? date : LocalDate.now();

    ActivitySummaryDto summary = trackingService.getActivitySummary(userId, targetDate);

    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Activity & Burn Tracker: " + summary.formattedDate());
    model.addAttribute("activeNav", "tracking");
    model.addAttribute("summary", summary);
    model.addAttribute("currentDate", targetDate);
    model.addAttribute("prevDate", targetDate.minusDays(1));
    model.addAttribute("nextDate", targetDate.plusDays(1));
    model.addAttribute("isToday", targetDate.equals(LocalDate.now()));

    model.addAttribute("activityTypes", ActivityType.values());
    model.addAttribute("intensities", ActivityIntensity.values());

    return "tracking/index";
  }

  @PostMapping("/steps")
  public String logSteps(
      @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam("steps") int steps,
      @RequestParam(value = "goalSteps", required = false) Integer goalSteps,
      @RequestParam(value = "sourceType", defaultValue = "MANUAL") String sourceType,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    try {
      LogStepsRequest req = new LogStepsRequest(date, steps, goalSteps, sourceType);
      trackingService.logSteps(userId, req);
      redirectAttributes.addFlashAttribute(
          "flashMessage", "Step count recorded successfully (" + steps + " steps)!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Failed to update steps: " + e.getMessage());
    }

    return "redirect:/tracking?date=" + date;
  }

  @PostMapping("/workout")
  public String logWorkout(
      @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam("activityType") String activityType,
      @RequestParam("intensity") String intensity,
      @RequestParam("durationMinutes") int durationMinutes,
      @RequestParam(value = "distanceKm", required = false) Double distanceKm,
      @RequestParam(value = "customCaloriesBurned", required = false) Double customCaloriesBurned,
      @RequestParam(value = "notes", required = false) String notes,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    try {
      LogWorkoutRequest req =
          new LogWorkoutRequest(
              date,
              activityType,
              intensity,
              durationMinutes,
              distanceKm,
              customCaloriesBurned,
              notes);
      trackingService.logWorkout(userId, req);
      redirectAttributes.addFlashAttribute("flashMessage", "Workout logged successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Failed to record workout: " + e.getMessage());
    }

    return "redirect:/tracking?date=" + date;
  }

  @PostMapping("/workout/delete/{id}")
  public String deleteWorkout(
      @PathVariable("id") Long id,
      @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);
    LocalDate targetDate = date != null ? date : LocalDate.now();

    try {
      trackingService.deleteWorkout(userId, id);
      redirectAttributes.addFlashAttribute("flashMessage", "Workout removed.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Could not remove workout: " + e.getMessage());
    }

    return "redirect:/tracking?date=" + targetDate;
  }

  private Long getUserId(String username) {
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
