package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.dto.AddGroceryItemRequest;
import com.easynutri.dto.AddMissingGroceryRequest;
import com.easynutri.dto.GroceryItemDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.GroceryService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class GroceryServiceTest {

  @Autowired private GroceryService groceryService;
  @Autowired private UserRepository userRepository;

  @Test
  @DisplayName("Add custom grocery item and toggle checked")
  void testAddAndToggleGroceryItem() {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    Long userId = demo.getId();

    GroceryItemDto item =
        groceryService.addItem(
            userId, new AddGroceryItemRequest("Organic Spinach", 200.0, "g", "VEGETABLES", null));

    assertNotNull(item.id());
    assertEquals("Organic Spinach", item.itemName());
    assertFalse(item.checked());

    boolean checked = groceryService.toggleChecked(userId, item.id());
    assertTrue(checked);

    List<GroceryItemDto> list = groceryService.getGroceryItems(userId);
    assertTrue(list.stream().anyMatch(i -> i.id().equals(item.id()) && i.checked()));
  }

  @Test
  @DisplayName("Batch add missing ingredients from recipe matcher")
  void testAddMissingIngredients() {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    Long userId = demo.getId();

    int added =
        groceryService.addMissingIngredients(
            userId, new AddMissingGroceryRequest(List.of("Ghee", "Fresh Coriander"), "r1"));

    assertEquals(2, added);
    List<GroceryItemDto> list = groceryService.getGroceryItems(userId);
    assertTrue(list.stream().anyMatch(i -> i.itemName().contains("Ghee")));
    assertTrue(list.stream().anyMatch(i -> i.itemName().contains("Fresh Coriander")));
  }
}
