package com.easynutri.controller.web;

import com.easynutri.entity.UserEntity;
import com.easynutri.entity.WeeklyInsightEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.WeeklyInsightService;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/insights")
public class InsightWebController {

  private final WeeklyInsightService insightService;
  private final UserRepository userRepository;

  public InsightWebController(WeeklyInsightService insightService, UserRepository userRepository) {
    this.insightService = insightService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public String weeklyInsights(
      @RequestParam(value = "date", required = false) String dateStr,
      Model model,
      Principal principal) {
    Long userId = resolveUserId(principal);
    LocalDate date =
        dateStr != null && !dateStr.isBlank() ? LocalDate.parse(dateStr) : LocalDate.now();

    List<WeeklyInsightEntity> insights = insightService.getOrGenerateInsights(userId, date);

    model.addAttribute("username", principal != null ? principal.getName() : "Demo User");
    model.addAttribute("pageTitle", "Weekly Health Check-in & Insights");
    model.addAttribute("activeNav", "insights");
    model.addAttribute("insights", insights);
    model.addAttribute("date", date);
    return "insights/index";
  }

  @PostMapping("/generate")
  public String regenerateInsights(
      @RequestParam(value = "date", required = false) String dateStr, Principal principal) {
    Long userId = resolveUserId(principal);
    LocalDate date =
        dateStr != null && !dateStr.isBlank() ? LocalDate.parse(dateStr) : LocalDate.now();
    insightService.generateInsightsForWeek(userId, date);
    return "redirect:/insights" + (dateStr != null ? "?date=" + dateStr : "");
  }

  private Long resolveUserId(Principal principal) {
    if (principal == null) {
      return userRepository.findByUsername("demo").map(UserEntity::getId).orElse(1L);
    }
    return userRepository.findByUsername(principal.getName()).map(UserEntity::getId).orElse(1L);
  }
}
