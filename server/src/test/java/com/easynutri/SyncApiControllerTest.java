package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.BatchSyncRequest;
import com.easynutri.dto.SyncActionDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodDiaryEntryRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.repository.WaterLogRepository;
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
class SyncApiControllerTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private UserRepository userRepository;
  @Autowired private FoodDiaryEntryRepository diaryEntryRepository;
  @Autowired private WaterLogRepository waterLogRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /api/v1/sync/batch processes offline-queued diary and water logs")
  void testBatchSyncProcessing() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    String op1 = UUID.randomUUID().toString();
    String key1 = "idemp_" + op1;

    String op2 = UUID.randomUUID().toString();
    String key2 = "idemp_" + op2;

    SyncActionDto diaryAction =
        new SyncActionDto(
            op1,
            key1,
            "LOG_DIARY_ITEM",
            demo.getId(),
            1,
            LocalDate.now().toString(),
            "{\"foodId\":\"food_spinach_palak\",\"amountG\":150.0,\"mealSlot\":\"LUNCH\",\"date\":\""
                + LocalDate.now()
                + "\"}");

    SyncActionDto waterAction =
        new SyncActionDto(
            op2,
            key2,
            "LOG_WATER",
            demo.getId(),
            1,
            LocalDate.now().toString(),
            "{\"amountMl\":350,\"date\":\"" + LocalDate.now() + "\"}");

    BatchSyncRequest req = new BatchSyncRequest(List.of(diaryAction, waterAction));

    mockMvc
        .perform(
            post("/api/v1/sync/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allSuccessful").value(true))
        .andExpect(jsonPath("$.processedCount").value(2))
        .andExpect(jsonPath("$.results[0].status").value("SUCCESS"))
        .andExpect(jsonPath("$.results[1].status").value("SUCCESS"));

    // Verify database persistence
    var waterLogs = waterLogRepository.findByUserIdOrderByLoggedAtDesc(demo.getId());
    assertFalse(waterLogs.isEmpty());
    assertEquals(350, waterLogs.get(0).getAmountMl());
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName(
      "Idempotency: Re-submitting identical idempotency key returns DUPLICATE_IGNORED and prevents duplicate records")
  void testSyncIdempotency() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    String op = UUID.randomUUID().toString();
    String key = "idemp_test_repeat_" + op;

    SyncActionDto waterAction =
        new SyncActionDto(
            op,
            key,
            "LOG_WATER",
            demo.getId(),
            1,
            LocalDate.now().toString(),
            "{\"amountMl\":500,\"date\":\"" + LocalDate.now() + "\"}");

    BatchSyncRequest req = new BatchSyncRequest(List.of(waterAction));

    // First attempt -> SUCCESS
    mockMvc
        .perform(
            post("/api/v1/sync/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.results[0].status").value("SUCCESS"));

    int waterCountAfterFirst =
        waterLogRepository.findByUserIdOrderByLoggedAtDesc(demo.getId()).size();

    // Second attempt -> DUPLICATE_IGNORED
    mockMvc
        .perform(
            post("/api/v1/sync/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.results[0].status").value("DUPLICATE_IGNORED"));

    int waterCountAfterSecond =
        waterLogRepository.findByUserIdOrderByLoggedAtDesc(demo.getId()).size();
    assertEquals(
        waterCountAfterFirst,
        waterCountAfterSecond,
        "Re-submitted sync action must not duplicate records");
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("Security: Rejects sync actions attempting to write to unauthorized profiles")
  void testUnauthorizedProfileSyncRejected() throws Exception {
    UserEntity stranger =
        userRepository.save(
            new UserEntity(
                "stranger_sync", "stranger_sync@other.local", "hashedpass", "Stranger Sync"));

    String op = UUID.randomUUID().toString();
    String key = "idemp_unauth_" + op;

    SyncActionDto unauthorizedAction =
        new SyncActionDto(
            op,
            key,
            "LOG_WATER",
            stranger.getId(), // Unauthorized profile target!
            1,
            LocalDate.now().toString(),
            "{\"amountMl\":250,\"date\":\"" + LocalDate.now() + "\"}");

    BatchSyncRequest req = new BatchSyncRequest(List.of(unauthorizedAction));

    mockMvc
        .perform(
            post("/api/v1/sync/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.results[0].status").value("REJECTED"))
        .andExpect(
            jsonPath("$.results[0].message")
                .value(org.hamcrest.Matchers.containsString("Unauthorized access")));
  }
}
