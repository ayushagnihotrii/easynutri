package com.easynutri.controller.web;

import com.easynutri.domain.calculation.BmiCalculator;
import com.easynutri.dto.ProfileUpdateRequest;
import com.easynutri.entity.UserProfileEntity;
import com.easynutri.service.UserProfileService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProfileWebController {

  private final UserProfileService profileService;

  public ProfileWebController(UserProfileService profileService) {
    this.profileService = profileService;
  }

  @GetMapping("/settings")
  public String settings(
      @RequestParam(value = "updated", required = false) String updated,
      Model model,
      Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Settings & Dietary Preferences");
    model.addAttribute("activeNav", "settings");

    Optional<UserProfileEntity> profileOpt = profileService.findByUsername(username);
    UserProfileEntity profile = profileOpt.orElse(null);

    ProfileUpdateRequest form;
    if (profile != null) {
      form =
          new ProfileUpdateRequest(
              profile.getAge(),
              profile.getBiologicalSex(),
              profile.getHeightCm(),
              profile.getWeightKg(),
              profile.getActivityLevel(),
              profile.getHealthGoal(),
              profile.getDietaryPreference(),
              profile.isPregnant(),
              profile.isSensitiveMode(),
              new ArrayList<>(profile.getAllergies()),
              new ArrayList<>(profile.getHealthConditions()));

      var bmiResult = BmiCalculator.calculate(profile.getWeightKg(), profile.getHeightCm());
      model.addAttribute("bmiResult", bmiResult);
    } else {
      form =
          new ProfileUpdateRequest(
              28,
              "MALE",
              172.0,
              68.0,
              "MODERATE",
              "MAINTAIN",
              "VEGETARIAN",
              false,
              false,
              List.of(),
              List.of());
    }

    model.addAttribute("profileForm", form);
    model.addAttribute("profile", profile);

    if (updated != null) {
      model.addAttribute(
          "successMessage", "Profile preferences and clinical targets updated successfully!");
    }

    return "settings/index";
  }

  @PostMapping("/settings/profile")
  public String updateProfile(
      @Valid @ModelAttribute("profileForm") ProfileUpdateRequest form,
      BindingResult bindingResult,
      Principal principal,
      Model model) {
    String username = principal != null ? principal.getName() : "demo";

    if (bindingResult.hasErrors()) {
      model.addAttribute("username", username);
      model.addAttribute("pageTitle", "Settings & Dietary Preferences");
      model.addAttribute("activeNav", "settings");
      model.addAttribute("errorMessage", "Please check your inputs and try again.");
      return "settings/index";
    }

    try {
      profileService.updateProfile(username, form);
      return "redirect:/settings?updated=true";
    } catch (Exception e) {
      model.addAttribute("username", username);
      model.addAttribute("pageTitle", "Settings & Dietary Preferences");
      model.addAttribute("activeNav", "settings");
      model.addAttribute("errorMessage", "Error updating profile: " + e.getMessage());
      return "settings/index";
    }
  }
}
