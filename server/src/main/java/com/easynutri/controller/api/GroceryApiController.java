package com.easynutri.controller.api;

import com.easynutri.dto.AddGroceryItemRequest;
import com.easynutri.dto.AddMissingGroceryRequest;
import com.easynutri.dto.GroceryItemDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.GroceryService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/grocery")
public class GroceryApiController {

  private final GroceryService groceryService;
  private final UserRepository userRepository;

  public GroceryApiController(GroceryService groceryService, UserRepository userRepository) {
    this.groceryService = groceryService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public ResponseEntity<List<GroceryItemDto>> getItems(Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(groceryService.getGroceryItems(userId));
  }

  @PostMapping("/add")
  public ResponseEntity<GroceryItemDto> addItem(
      @Valid @RequestBody AddGroceryItemRequest request, Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(groceryService.addItem(userId, request));
  }

  @PostMapping("/missing")
  public ResponseEntity<Map<String, Object>> addMissingIngredients(
      @RequestBody AddMissingGroceryRequest request, Principal principal) {
    Long userId = getUserId(principal);
    int addedCount = groceryService.addMissingIngredients(userId, request);
    return ResponseEntity.ok(
        Map.of(
            "success",
            true,
            "addedCount",
            addedCount,
            "message",
            "Added " + addedCount + " items to your grocery list"));
  }

  @PostMapping("/{id}/toggle")
  public ResponseEntity<Map<String, Object>> toggleItem(
      @PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    boolean checked = groceryService.toggleChecked(userId, id);
    return ResponseEntity.ok(Map.of("id", id, "checked", checked));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteItem(@PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    groceryService.deleteItem(userId, id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/clear-completed")
  public ResponseEntity<Map<String, Object>> clearCompleted(Principal principal) {
    Long userId = getUserId(principal);
    groceryService.clearCompleted(userId);
    return ResponseEntity.ok(Map.of("success", true));
  }

  private Long getUserId(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
