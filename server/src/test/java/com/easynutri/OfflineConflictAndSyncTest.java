package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.BatchSyncRequest;
import com.easynutri.dto.SyncActionDto;
import com.easynutri.entity.FoodEntity;
import com.easynutri.entity.GroceryItemEntity;
import com.easynutri.entity.PantryItemEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodRepository;
import com.easynutri.repository.GroceryItemRepository;
import com.easynutri.repository.PantryItemRepository;
import com.easynutri.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class OfflineConflictAndSyncTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private UserRepository userRepository;
  @Autowired private PantryItemRepository pantryRepository;
  @Autowired private GroceryItemRepository groceryRepository;
  @Autowired private FoodRepository foodRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("Conflict Resolution: ADJUST_PANTRY on already deleted item flags CONFLICT_DETECTED")
  void testPantryAdjustmentConflictDetection() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();

    // Operation targets non-existent / already deleted pantry item ID 999999
    SyncActionDto conflictAction =
        new SyncActionDto(
            UUID.randomUUID().toString(),
            "idemp_conflict_" + UUID.randomUUID(),
            "ADJUST_PANTRY",
            demo.getId(),
            1,
            LocalDate.now().toString(),
            """
            {"itemId": 999999, "deltaG": 100.0}
            """);

    BatchSyncRequest request = new BatchSyncRequest(List.of(conflictAction));

    mockMvc
        .perform(
            post("/api/v1/sync/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allSuccessful").value(false))
        .andExpect(jsonPath("$.conflictCount").value(1))
        .andExpect(jsonPath("$.results[0].status").value("CONFLICT_DETECTED"))
        .andExpect(
            jsonPath(
                "$.results[0].message",
                org.hamcrest.Matchers.containsString("already consumed or deleted")));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("Delete Propagation: DELETE_PANTRY and DELETE_GROCERY synchronize clean deletions")
  void testDeletePropagation() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    FoodEntity food = foodRepository.findAll().stream().findFirst().orElseThrow();

    PantryItemEntity pantryItem =
        pantryRepository.save(new PantryItemEntity(demo, food, 300.0, LocalDate.now().plusDays(5)));
    GroceryItemEntity groceryItem =
        groceryRepository.save(
            new GroceryItemEntity(demo, food, "Organic Milk", 1.0, "L", "DAIRY"));

    Long pantryId = pantryItem.getId();
    Long groceryId = groceryItem.getId();

    SyncActionDto deletePantry =
        new SyncActionDto(
            UUID.randomUUID().toString(),
            "idemp_del_pan_" + UUID.randomUUID(),
            "DELETE_PANTRY",
            demo.getId(),
            1,
            LocalDate.now().toString(),
            "{\"itemId\": " + pantryId + "}");

    SyncActionDto deleteGrocery =
        new SyncActionDto(
            UUID.randomUUID().toString(),
            "idemp_del_groc_" + UUID.randomUUID(),
            "DELETE_GROCERY",
            demo.getId(),
            1,
            LocalDate.now().toString(),
            "{\"itemId\": " + groceryId + "}");

    BatchSyncRequest request = new BatchSyncRequest(List.of(deletePantry, deleteGrocery));

    mockMvc
        .perform(
            post("/api/v1/sync/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allSuccessful").value(true))
        .andExpect(jsonPath("$.processedCount").value(2));

    assertTrue(
        pantryRepository.findById(pantryId).isEmpty(), "Pantry item must be deleted on server");
    assertTrue(
        groceryRepository.findById(groceryId).isEmpty(), "Grocery item must be deleted on server");
  }
}
