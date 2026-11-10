package com.easynutri.controller.web;

import com.easynutri.dto.HydrationSummaryDto;
import com.easynutri.dto.LogWaterRequest;
import com.easynutri.dto.SaveHydrationPreferencesRequest;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.HydrationService;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hydration")
public class HydrationWebController {

  private final HydrationService hydrationService;
  private final UserRepository userRepository;

  public HydrationWebController(HydrationService hydrationService, UserRepository userRepository) {
    this.hydrationService = hydrationService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public String hydrationPage(
      @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      Model model,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);
    LocalDate targetDate = date != null ? date : LocalDate.now();

    HydrationSummaryDto summary = hydrationService.getHydrationSummary(userId, targetDate);

    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Water Tracker & Hydration Goals");
    model.addAttribute("activeNav", "hydration");
    model.addAttribute("summary", summary);
    model.addAttribute("currentDate", targetDate);
    model.addAttribute("prevDate", targetDate.minusDays(1));
    model.addAttribute("nextDate", targetDate.plusDays(1));
    model.addAttribute("isToday", targetDate.equals(LocalDate.now()));

    return "hydration/index";
  }

  @PostMapping("/log")
  public String logWater(
      @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam("amountMl") int amountMl,
      @RequestParam(value = "notes", required = false) String notes,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    try {
      LogWaterRequest req = new LogWaterRequest(date, amountMl, notes);
      hydrationService.logWater(userId, req);
      redirectAttributes.addFlashAttribute(
          "flashMessage", "Hydration logged: +" + amountMl + " ml! Keep it up! 💧");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Failed to log hydration: " + e.getMessage());
    }

    return "redirect:/hydration?date=" + date;
  }

  @PostMapping("/delete/{id}")
  public String deleteWaterLog(
      @PathVariable("id") Long id,
      @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);
    LocalDate targetDate = date != null ? date : LocalDate.now();

    try {
      hydrationService.deleteWaterLog(userId, id);
      redirectAttributes.addFlashAttribute("flashMessage", "Water log entry deleted.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("flashError", "Failed to delete log: " + e.getMessage());
    }

    return "redirect:/hydration?date=" + targetDate;
  }

  @PostMapping("/preferences")
  public String savePreferences(
      @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      @RequestParam(value = "dailyTargetMl", required = false) Integer dailyTargetMl,
      @RequestParam(value = "reminderEnabled", defaultValue = "false") boolean reminderEnabled,
      @RequestParam(value = "reminderIntervalHours", defaultValue = "2") int reminderIntervalHours,
      @RequestParam(value = "startTime", defaultValue = "08:00") String startTime,
      @RequestParam(value = "endTime", defaultValue = "22:00") String endTime,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);
    LocalDate targetDate = date != null ? date : LocalDate.now();

    try {
      SaveHydrationPreferencesRequest req =
          new SaveHydrationPreferencesRequest(
              dailyTargetMl, reminderEnabled, reminderIntervalHours, startTime, endTime);
      hydrationService.savePreferences(userId, req);
      redirectAttributes.addFlashAttribute(
          "flashMessage", "Hydration targets and reminder preferences updated!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Could not save preferences: " + e.getMessage());
    }

    return "redirect:/hydration?date=" + targetDate;
  }

  private Long getUserId(String username) {
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
