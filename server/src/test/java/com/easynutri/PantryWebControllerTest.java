package com.easynutri;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.AddPantryItemRequest;
import com.easynutri.dto.PantryItemDto;
import com.easynutri.entity.FoodEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.PantryService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class PantryWebControllerTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private UserRepository userRepository;
  @Autowired private FoodRepository foodRepository;
  @Autowired private PantryService pantryService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  private FoodEntity getOrCreateFood() {
    List<FoodEntity> foods = foodRepository.findAll();
    if (!foods.isEmpty()) {
      return foods.get(0);
    }
    FoodEntity food =
        new FoodEntity(
            "food_spinach_test",
            "Spinach Test",
            "पालक",
            "VEGETABLES",
            23.0,
            2.9,
            3.6,
            0.4,
            "IFCT",
            true);
    return foodRepository.save(food);
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /pantry loads inventory, expiry metrics, and Cook Before Expiring suggestions")
  void testPantryPage() throws Exception {
    mockMvc
        .perform(get("/pantry"))
        .andExpect(status().isOk())
        .andExpect(view().name("pantry/index"))
        .andExpect(model().attributeExists("items"))
        .andExpect(model().attributeExists("totalCount"))
        .andExpect(model().attributeExists("cookRecommendations"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /pantry/add creates persistent pantry inventory item")
  void testAddPantryItem() throws Exception {
    FoodEntity food = getOrCreateFood();

    mockMvc
        .perform(
            post("/pantry/add")
                .with(csrf())
                .param("foodId", food.getId())
                .param("quantityG", "300.0")
                .param("unit", "g")
                .param("storageLocation", "CUPBOARD")
                .param("expirationDate", LocalDate.now().plusDays(5).toString())
                .param("notes", "Bought from local store"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/pantry"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /pantry/{id}/adjust modifies item quantity")
  void testAdjustQuantity() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    FoodEntity food = getOrCreateFood();
    PantryItemDto item =
        pantryService.addPantryItem(
            demo.getId(),
            new AddPantryItemRequest(
                food.getId(),
                200.0,
                "g",
                "CUPBOARD",
                LocalDate.now().plusDays(4),
                LocalDate.now(),
                ""));

    mockMvc
        .perform(post("/pantry/" + item.id() + "/adjust").with(csrf()).param("delta", "50"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/pantry"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /pantry/{id}/consume marks item consumed")
  void testConsumeItem() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    FoodEntity food = getOrCreateFood();
    PantryItemDto item =
        pantryService.addPantryItem(
            demo.getId(),
            new AddPantryItemRequest(
                food.getId(),
                150.0,
                "g",
                "CUPBOARD",
                LocalDate.now().plusDays(2),
                LocalDate.now(),
                ""));

    mockMvc
        .perform(post("/pantry/" + item.id() + "/consume").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/pantry"));
  }
}
