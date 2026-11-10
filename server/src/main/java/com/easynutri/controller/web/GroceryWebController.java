package com.easynutri.controller.web;

import com.easynutri.dto.AddGroceryItemRequest;
import com.easynutri.dto.GroceryItemDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.GroceryService;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/grocery")
public class GroceryWebController {

  private final GroceryService groceryService;
  private final UserRepository userRepository;

  public GroceryWebController(GroceryService groceryService, UserRepository userRepository) {
    this.groceryService = groceryService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public String groceryList(Model model, Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Smart Grocery Checklist");
    model.addAttribute("activeNav", "grocery");

    List<GroceryItemDto> items = groceryService.getGroceryItems(userId);
    model.addAttribute("items", items);

    long checkedCount = items.stream().filter(GroceryItemDto::checked).count();
    model.addAttribute("totalCount", items.size());
    model.addAttribute("checkedCount", checkedCount);

    return "grocery/index";
  }

  @PostMapping("/add")
  public String addItem(
      @RequestParam("itemName") String itemName,
      @RequestParam(value = "quantity", defaultValue = "1.0") double quantity,
      @RequestParam(value = "unit", defaultValue = "item") String unit,
      @RequestParam(value = "category", defaultValue = "GENERAL") String category,
      Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    groceryService.addItem(
        userId, new AddGroceryItemRequest(itemName, quantity, unit, category, null));
    return "redirect:/grocery";
  }

  @PostMapping("/{id}/toggle")
  public String toggleItem(@PathVariable("id") Long id, Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    groceryService.toggleChecked(userId, id);
    return "redirect:/grocery";
  }

  @PostMapping("/{id}/delete")
  public String deleteItem(@PathVariable("id") Long id, Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    groceryService.deleteItem(userId, id);
    return "redirect:/grocery";
  }

  @PostMapping("/clear-completed")
  public String clearCompleted(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    groceryService.clearCompleted(userId);
    return "redirect:/grocery";
  }

  private Long getUserId(String username) {
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
