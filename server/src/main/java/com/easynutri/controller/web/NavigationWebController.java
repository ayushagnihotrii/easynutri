package com.easynutri.controller.web;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavigationWebController {

  private void populateCommon(Model model, Principal principal, String title, String activeNav) {
    String username = principal != null ? principal.getName() : "Demo User";
    model.addAttribute("username", username);
    model.addAttribute("pageTitle", title);
    model.addAttribute("activeNav", activeNav);
  }

  @GetMapping("/family")
  public String family(Model model, Principal principal) {
    populateCommon(model, principal, "Family Profiles", "family");
    return "family/index";
  }
}
