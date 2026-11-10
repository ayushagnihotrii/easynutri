package com.easynutri;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.BatchSyncRequest;
import com.easynutri.dto.RegisterRequest;
import com.easynutri.dto.SyncActionDto;
import com.easynutri.entity.FoodEntity;
import com.easynutri.entity.GroceryItemEntity;
import com.easynutri.entity.PantryItemEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodDiaryEntryRepository;
import com.easynutri.repository.FoodRepository;
import com.easynutri.repository.GroceryItemRepository;
import com.easynutri.repository.PantryItemRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.repository.WaterLogRepository;
import com.easynutri.service.DiaryService;
import com.easynutri.service.HouseholdService;
import com.easynutri.service.UserService;
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
  @Autowired private UserService userService;
  @Autowired private PantryItemRepository pantryRepository;
  @Autowired private GroceryItemRepository groceryRepository;
  @Autowired private FoodRepository foodRepository;
  @Autowired private FoodDiaryEntryRepository diaryEntryRepository;
  @Autowired private WaterLogRepository waterLogRepository;
  @Autowired private DiaryService diaryService;
  @Autowired private HouseholdService householdService;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("Conflict Resolution: ADJUST_PANTRY on already deleted item flags CONFLICT_DETECTED")
  void testPantryAdjustmentConflictDetection() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();

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
        .andExpect(jsonPath("$.results[0].message", containsString("already consumed or deleted")));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("Conflict Resolution: TOGGLE_GROCERY on deleted server item flags CONFLICT_DETECTED")
  void testGroceryToggleConflictDetection() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();

    SyncActionDto staleToggle =
        new SyncActionDto(
            UUID.randomUUID().toString(),
            "idemp_groc_stale_" + UUID.randomUUID(),
            "TOGGLE_GROCERY",
            demo.getId(),
            1,
            LocalDate.now().toString(),
            """
            {"itemId": 888888}
            """);

    BatchSyncRequest request = new BatchSyncRequest(List.of(staleToggle));

    mockMvc
        .perform(
            post("/api/v1/sync/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allSuccessful").value(false))
        .andExpect(jsonPath("$.conflictCount").value(1))
        .andExpect(jsonPath("$.results[0].status").value("CONFLICT_DETECTED"))
        .andExpect(jsonPath("$.results[0].message", containsString("was removed on server")));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName(
      "Duplicate Append-Only Operations: Retried sync returns DUPLICATE_IGNORED and prevents duplicates")
  void testDuplicateAppendOnlyOperationsIdempotency() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    String idempotencyKey = "idemp_water_" + UUID.randomUUID();

    SyncActionDto waterOp =
        new SyncActionDto(
            UUID.randomUUID().toString(),
            idempotencyKey,
            "LOG_WATER",
            demo.getId(),
            1,
            LocalDate.now().toString(),
            """
            {"amountMl": 350, "notes": "Morning water"}
            """);

    BatchSyncRequest request = new BatchSyncRequest(List.of(waterOp));

    // First transmission: success
    mockMvc
        .perform(
            post("/api/v1/sync/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allSuccessful").value(true))
        .andExpect(jsonPath("$.processedCount").value(1))
        .andExpect(jsonPath("$.results[0].status").value("SUCCESS"));

    int initialCount = waterLogRepository.findByUserIdOrderByLoggedAtDesc(demo.getId()).size();

    // Second transmission (network retry with same idempotency key): duplicate ignored
    mockMvc
        .perform(
            post("/api/v1/sync/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allSuccessful").value(true))
        .andExpect(jsonPath("$.results[0].status").value("DUPLICATE_IGNORED"));

    int subsequentCount = waterLogRepository.findByUserIdOrderByLoggedAtDesc(demo.getId()).size();
    assertEquals(initialCount, subsequentCount, "Water log must not be duplicated on retry");
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

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("Delete Propagation: DELETE_DIARY removes food diary entry item cleanly")
  void testDiaryDeletePropagation() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    FoodEntity food = foodRepository.findAll().stream().findFirst().orElseThrow();

    var logged =
        diaryService.logItem(
            demo.getId(),
            new com.easynutri.dto.LogFoodItemRequest(
                LocalDate.now(), "BREAKFAST", food.getId(), null, null, 150.0, 1.0));

    Long itemId = logged.id();

    SyncActionDto deleteDiary =
        new SyncActionDto(
            UUID.randomUUID().toString(),
            "idemp_del_diary_" + UUID.randomUUID(),
            "DELETE_DIARY",
            demo.getId(),
            1,
            LocalDate.now().toString(),
            "{\"itemId\": " + itemId + "}");

    BatchSyncRequest request = new BatchSyncRequest(List.of(deleteDiary));

    mockMvc
        .perform(
            post("/api/v1/sync/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allSuccessful").value(true));

    var dayEntries = diaryEntryRepository.findByUserIdAndDiaryDate(demo.getId(), LocalDate.now());
    boolean exists =
        dayEntries.stream()
            .flatMap(e -> e.getItems().stream())
            .anyMatch(i -> i.getId().equals(itemId));
    assertFalse(exists, "Food diary item must be cleanly removed");
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("Security & Isolation: Sync operation targeting unauthorized profile is REJECTED")
  void testUnauthorizedProfileSyncRejected() throws Exception {
    UserEntity foreignUser =
        userService.registerUser(
            new RegisterRequest(
                "foreign_user_99", "foreign99@example.com", "SecretPass123!", "Foreign User"));

    SyncActionDto foreignAction =
        new SyncActionDto(
            UUID.randomUUID().toString(),
            "idemp_unauth_" + UUID.randomUUID(),
            "LOG_WATER",
            foreignUser.getId(),
            1,
            LocalDate.now().toString(),
            "{\"amountMl\": 500}");

    BatchSyncRequest request = new BatchSyncRequest(List.of(foreignAction));

    mockMvc
        .perform(
            post("/api/v1/sync/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allSuccessful").value(false))
        .andExpect(jsonPath("$.results[0].status").value("REJECTED"))
        .andExpect(jsonPath("$.results[0].message", containsString("Unauthorized access")));

    assertTrue(
        waterLogRepository.findByUserIdOrderByLoggedAtDesc(foreignUser.getId()).isEmpty(),
        "Foreign user's health data must not be touched by unauthorized sync operation");
  }

  @Test
  @DisplayName("Security: Unauthenticated request to /api/v1/sync/batch is blocked")
  void testUnauthenticatedSyncBlocked() throws Exception {
    SyncActionDto action =
        new SyncActionDto(
            UUID.randomUUID().toString(),
            "idemp_anon_" + UUID.randomUUID(),
            "LOG_WATER",
            1L,
            1,
            LocalDate.now().toString(),
            "{\"amountMl\": 250}");

    BatchSyncRequest request = new BatchSyncRequest(List.of(action));

    mockMvc
        .perform(
            post("/api/v1/sync/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", containsString("/login")));
  }
}
