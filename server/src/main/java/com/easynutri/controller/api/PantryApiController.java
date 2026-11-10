package com.easynutri.controller.api;

import com.easynutri.dto.AddPantryItemRequest;
import com.easynutri.dto.CookBeforeExpiresDto;
import com.easynutri.dto.PantryItemDto;
import com.easynutri.dto.UpdatePantryItemRequest;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.PantryService;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pantry")
public class PantryApiController {

  private final PantryService pantryService;
  private final UserRepository userRepository;

  public PantryApiController(PantryService pantryService, UserRepository userRepository) {
    this.pantryService = pantryService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public ResponseEntity<List<PantryItemDto>> getPantry(Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(pantryService.getUserPantry(userId));
  }

  @PostMapping
  public ResponseEntity<PantryItemDto> addPantryItem(
      @RequestBody AddPantryItemRequest request, Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(pantryService.addPantryItem(userId, request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<PantryItemDto> updatePantryItem(
      @PathVariable("id") Long id,
      @RequestBody UpdatePantryItemRequest request,
      Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(pantryService.updatePantryItem(userId, id, request));
  }

  @PostMapping("/{id}/adjust")
  public ResponseEntity<Map<String, Object>> adjustQuantity(
      @PathVariable("id") Long id, @RequestParam("delta") double delta, Principal principal) {
    Long userId = getUserId(principal);
    pantryService.adjustQuantity(userId, id, delta);
    return ResponseEntity.ok(Map.of("success", true, "id", id, "delta", delta));
  }

  @PostMapping("/{id}/consume")
  public ResponseEntity<Map<String, Object>> consumeItem(
      @PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    pantryService.markConsumed(userId, id);
    return ResponseEntity.ok(Map.of("success", true, "id", id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Object>> deleteItem(
      @PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    pantryService.deletePantryItem(userId, id);
    return ResponseEntity.ok(Map.of("success", true, "id", id));
  }

  @GetMapping("/cook-before-expires")
  public ResponseEntity<List<CookBeforeExpiresDto>> getCookBeforeExpires(Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(pantryService.getCookBeforeItExpires(userId));
  }

  private Long getUserId(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
