package com.easynutri.controller.web;

import com.easynutri.dto.AddPantryItemRequest;
import com.easynutri.dto.CookBeforeExpiresDto;
import com.easynutri.dto.PantryItemDto;
import com.easynutri.dto.UpdatePantryItemRequest;
import com.easynutri.entity.FoodEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.PantryService;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pantry")
public class PantryWebController {

  private final PantryService pantryService;
  private final FoodRepository foodRepository;
  private final UserRepository userRepository;

  public PantryWebController(
      PantryService pantryService, FoodRepository foodRepository, UserRepository userRepository) {
    this.pantryService = pantryService;
    this.foodRepository = foodRepository;
    this.userRepository = userRepository;
  }

  @GetMapping
  public String pantryPage(
      @RequestParam(value = "location", required = false) String location,
      @RequestParam(value = "filter", required = false) String filter,
      Model model,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Smart Pantry & Expiry Tracker");
    model.addAttribute("activeNav", "pantry");
    model.addAttribute("activeLocation", location != null ? location : "ALL");
    model.addAttribute("activeFilter", filter != null ? filter : "ALL");

    List<PantryItemDto> allItems = pantryService.getUserPantry(userId);
    List<PantryItemDto> items = allItems;

    // Filter by location
    if (location != null && !location.isBlank() && !"ALL".equalsIgnoreCase(location)) {
      items =
          items.stream()
              .filter(i -> location.equalsIgnoreCase(i.storageLocation()))
              .collect(Collectors.toList());
    }

    // Filter by expiry status
    if (filter != null && !filter.isBlank() && !"ALL".equalsIgnoreCase(filter)) {
      items =
          items.stream()
              .filter(
                  i -> {
                    if ("EXPIRED".equalsIgnoreCase(filter)) {
                      return i.daysRemaining() < 0;
                    } else if ("TODAY".equalsIgnoreCase(filter)) {
                      return i.daysRemaining() >= 0 && i.daysRemaining() <= 1;
                    } else if ("SOON".equalsIgnoreCase(filter)) {
                      return i.daysRemaining() > 1 && i.daysRemaining() <= 3;
                    } else if ("FRESH".equalsIgnoreCase(filter)) {
                      return i.daysRemaining() > 3;
                    }
                    return true;
                  })
              .collect(Collectors.toList());
    }

    model.addAttribute("items", items);

    long expiringSoonCount =
        allItems.stream().filter(i -> i.daysRemaining() >= 0 && i.daysRemaining() <= 3).count();
    long expiredCount = allItems.stream().filter(i -> i.daysRemaining() < 0).count();
    long freshCount = allItems.stream().filter(i -> i.daysRemaining() > 3).count();

    model.addAttribute("totalCount", allItems.size());
    model.addAttribute("expiringSoonCount", expiringSoonCount);
    model.addAttribute("expiredCount", expiredCount);
    model.addAttribute("freshCount", freshCount);

    // Load "Cook Before It Expires" recommendations
    List<CookBeforeExpiresDto> cookBeforeExpires = pantryService.getCookBeforeItExpires(userId);
    model.addAttribute("cookRecommendations", cookBeforeExpires);

    // Available foods for add pantry modal
    List<FoodEntity> availableFoods = foodRepository.findAll();
    model.addAttribute("availableFoods", availableFoods);

    return "pantry/index";
  }

  @PostMapping("/add")
  public String addPantryItem(
      @RequestParam("foodId") String foodId,
      @RequestParam(value = "quantityG", defaultValue = "250.0") double quantityG,
      @RequestParam(value = "unit", defaultValue = "g") String unit,
      @RequestParam(value = "storageLocation", defaultValue = "CUPBOARD") String storageLocation,
      @RequestParam(value = "expirationDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate expirationDate,
      @RequestParam(value = "notes", required = false) String notes,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    LocalDate expiry = expirationDate != null ? expirationDate : LocalDate.now().plusDays(7);

    pantryService.addPantryItem(
        userId,
        new AddPantryItemRequest(
            foodId, quantityG, unit, storageLocation, expiry, LocalDate.now(), notes));

    redirectAttributes.addFlashAttribute("flashMessage", "Item added to pantry inventory.");
    return "redirect:/pantry";
  }

  @PostMapping("/{id}/update")
  public String updatePantryItem(
      @PathVariable("id") Long id,
      @RequestParam("quantityG") double quantityG,
      @RequestParam(value = "unit", defaultValue = "g") String unit,
      @RequestParam(value = "storageLocation", defaultValue = "CUPBOARD") String storageLocation,
      @RequestParam(value = "expirationDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate expirationDate,
      @RequestParam(value = "notes", required = false) String notes,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    pantryService.updatePantryItem(
        userId,
        id,
        new UpdatePantryItemRequest(quantityG, unit, storageLocation, expirationDate, notes));

    redirectAttributes.addFlashAttribute("flashMessage", "Pantry item updated.");
    return "redirect:/pantry";
  }

  @PostMapping("/{id}/adjust")
  public String adjustQuantity(
      @PathVariable("id") Long id, @RequestParam("delta") double delta, Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    pantryService.adjustQuantity(userId, id, delta);
    return "redirect:/pantry";
  }

  @PostMapping("/{id}/consume")
  public String consumeItem(
      @PathVariable("id") Long id, Principal principal, RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    pantryService.markConsumed(userId, id);
    redirectAttributes.addFlashAttribute("flashMessage", "Item consumed and removed from pantry.");
    return "redirect:/pantry";
  }

  @PostMapping("/{id}/delete")
  public String deleteItem(
      @PathVariable("id") Long id, Principal principal, RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    pantryService.deletePantryItem(userId, id);
    redirectAttributes.addFlashAttribute("flashMessage", "Item deleted from pantry.");
    return "redirect:/pantry";
  }

  private Long getUserId(String username) {
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
