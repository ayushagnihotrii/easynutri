package com.easynutri.controller.web;

import com.easynutri.dto.RegisterRequest;
import com.easynutri.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthWebController {

  private final UserService userService;

  public AuthWebController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/login")
  public String login(
      @RequestParam(value = "error", required = false) String error,
      @RequestParam(value = "logout", required = false) String logout,
      Model model) {
    if (error != null) {
      model.addAttribute("errorMessage", "Invalid username or password. Please try again.");
    }
    if (logout != null) {
      model.addAttribute("successMessage", "You have been logged out successfully.");
    }
    return "auth/login";
  }

  @GetMapping("/register")
  public String register(Model model) {
    model.addAttribute("registerForm", new RegisterForm("", "", "", ""));
    return "auth/register";
  }

  @PostMapping("/register")
  public String processRegister(
      @Valid @ModelAttribute("registerForm") RegisterForm form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "auth/register";
    }

    try {
      userService.registerUser(
          new RegisterRequest(form.username(), form.email(), form.password(), form.fullName()));
      return "redirect:/login?registered=true";
    } catch (IllegalArgumentException e) {
      model.addAttribute("registrationError", e.getMessage());
      return "auth/register";
    }
  }

  public record RegisterForm(String username, String email, String password, String fullName) {}
}
