package com.easynutri.controller.web;

import com.easynutri.dto.BadgeDto;
import com.easynutri.dto.ChallengeDto;
import com.easynutri.dto.UserStreakDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.entity.UserProfileEntity;
import com.easynutri.repository.UserProfileRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.GamificationService;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AchievementsWebController {

  private final GamificationService gamificationService;
  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;

  public AchievementsWebController(
      GamificationService gamificationService,
      UserRepository userRepository,
      UserProfileRepository userProfileRepository) {
    this.gamificationService = gamificationService;
    this.userRepository = userRepository;
    this.userProfileRepository = userProfileRepository;
  }

  @GetMapping("/achievements")
  public String achievements(Model model, Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    List<UserStreakDto> streaks = gamificationService.getUserStreaks(currentUserId);
    List<BadgeDto> badges = gamificationService.getUserBadges(currentUserId);

    boolean sensitiveMode =
        userProfileRepository
            .findByUserId(currentUserId)
            .map(UserProfileEntity::isSensitiveMode)
            .orElse(false);

    long unlockedBadgesCount = badges.stream().filter(BadgeDto::isUnlocked).count();

    model.addAttribute("streaks", streaks);
    model.addAttribute("badges", badges);
    model.addAttribute("unlockedBadgesCount", unlockedBadgesCount);
    model.addAttribute("totalBadgesCount", badges.size());
    model.addAttribute("sensitiveMode", sensitiveMode);
    model.addAttribute("username", username);
    model.addAttribute("currentUserId", currentUserId);
    model.addAttribute("activeNav", "achievements");
    model.addAttribute("pageTitle", "Streaks & Achievements — EasyNutri");

    return "achievements/index";
  }

  @GetMapping("/challenges")
  public String challenges(Model model, Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    List<ChallengeDto> challenges = gamificationService.getChallenges(currentUserId);

    boolean sensitiveMode =
        userProfileRepository
            .findByUserId(currentUserId)
            .map(UserProfileEntity::isSensitiveMode)
            .orElse(false);

    model.addAttribute("challenges", challenges);
    model.addAttribute("sensitiveMode", sensitiveMode);
    model.addAttribute("username", username);
    model.addAttribute("currentUserId", currentUserId);
    model.addAttribute("activeNav", "challenges");
    model.addAttribute("pageTitle", "Community Challenges — EasyNutri");

    return "challenges/index";
  }

  @PostMapping("/challenges/{id}/join")
  public String joinChallenge(
      @PathVariable("id") String id, Principal principal, RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    try {
      gamificationService.joinChallenge(currentUserId, id);
      redirectAttributes.addFlashAttribute(
          "flashMessage", "You have successfully joined the challenge! Keep up the momentum!");
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("flashError", e.getMessage());
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Failed to join challenge: " + e.getMessage());
    }

    return "redirect:/challenges";
  }

  private Long getUserId(String username) {
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
