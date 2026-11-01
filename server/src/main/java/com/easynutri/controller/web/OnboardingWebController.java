package com.easynutri.controller.web;

import com.easynutri.dto.OnboardingRequest;
import com.easynutri.service.UserProfileService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class OnboardingWebController {

  private final UserProfileService profileService;

  public OnboardingWebController(UserProfileService profileService) {
    this.profileService = profileService;
  }

  @GetMapping("/onboarding")
  public String showOnboardingWizard(Model model, Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Personalized Health Setup");

    // Populate default form values
    OnboardingRequest form =
        new OnboardingRequest(
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
    model.addAttribute("onboardingForm", form);
    return "onboarding/wizard";
  }

  @PostMapping("/onboarding")
  public String processOnboarding(
      @Valid @ModelAttribute("onboardingForm") OnboardingRequest form,
      BindingResult bindingResult,
      Principal principal,
      Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("onboardingForm", form);
      model.addAttribute("pageTitle", "Personalized Health Setup");
      model.addAttribute("errorMessage", "Please verify the highlighted inputs and try again.");
      return "onboarding/wizard";
    }

    String username = principal != null ? principal.getName() : "demo";
    try {
      profileService.saveOnboardingProfile(username, form);
      return "redirect:/dashboard?onboarded=true";
    } catch (Exception e) {
      model.addAttribute("onboardingForm", form);
      model.addAttribute("pageTitle", "Personalized Health Setup");
      model.addAttribute("errorMessage", "Error saving profile: " + e.getMessage());
      return "onboarding/wizard";
    }
  }
}
