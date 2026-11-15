package com.easynutri.controller.web;

import com.easynutri.dto.RegisterRequest;
import com.easynutri.service.DemoDataSeederService;
import com.easynutri.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
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
  private final UserDetailsService userDetailsService;
  private final DemoDataSeederService demoDataSeederService;
  private final SecurityContextRepository securityContextRepository =
      new HttpSessionSecurityContextRepository();

  public AuthWebController(
      UserService userService,
      UserDetailsService userDetailsService,
      DemoDataSeederService demoDataSeederService) {
    this.userService = userService;
    this.userDetailsService = userDetailsService;
    this.demoDataSeederService = demoDataSeederService;
  }

  @GetMapping("/demo-login")
  public String demoLogin(HttpServletRequest request, HttpServletResponse response) {
    try {
      demoDataSeederService.seedPresentationDemoData();
      UserDetails userDetails = userDetailsService.loadUserByUsername("demo");
      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(auth);
      SecurityContextHolder.setContext(context);
      securityContextRepository.saveContext(context, request, response);
      return "redirect:/dashboard";
    } catch (Exception e) {
      return "redirect:/login?error=true";
    }
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
