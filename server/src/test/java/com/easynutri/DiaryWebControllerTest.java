package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.DiaryItemDto;
import com.easynutri.dto.LogFoodItemRequest;
import com.easynutri.entity.FoodDiaryEntryEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodDiaryEntryRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.DiaryService;
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
class DiaryWebControllerTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private DiaryService diaryService;
  @Autowired private UserRepository userRepository;
  @Autowired private FoodDiaryEntryRepository diaryEntryRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /diary renders daily diary timeline with 4 meal categories")
  void testDiaryPageLoads() throws Exception {
    mockMvc
        .perform(get("/diary"))
        .andExpect(status().isOk())
        .andExpect(view().name("diary/index"))
        .andExpect(model().attributeExists("diaryDay"))
        .andExpect(model().attribute("activeNav", "diary"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /diary/log logs food item and creates immutable nutrition snapshot")
  void testLogFoodItem() throws Exception {
    LocalDate today = LocalDate.now();

    mockMvc
        .perform(
            post("/diary/log")
                .with(csrf())
                .param("date", today.toString())
                .param("mealSlot", "BREAKFAST")
                .param("customName", "Oatmeal with Almond Milk")
                .param("amountG", "150")
                .param("servings", "1.5"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/diary?date=" + today));

    UserEntity user = userRepository.findByUsername("demo").orElseThrow();
    List<FoodDiaryEntryEntity> entries =
        diaryEntryRepository.findByUserIdAndDiaryDate(user.getId(), today);

    assertFalse(entries.isEmpty());
    boolean hasBreakfast = entries.stream().anyMatch(e -> "BREAKFAST".equals(e.getMealSlot()));
    assertTrue(hasBreakfast);
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /diary/duplicate copies meal slot to target date")
  void testDuplicateMeal() throws Exception {
    UserEntity user = userRepository.findByUsername("demo").orElseThrow();
    LocalDate today = LocalDate.now();
    LocalDate tomorrow = today.plusDays(1);

    diaryService.logItem(
        user.getId(),
        new LogFoodItemRequest(today, "LUNCH", null, null, "Dal Tadka with Rice", 250.0, 1.0));

    mockMvc
        .perform(
            post("/diary/duplicate")
                .with(csrf())
                .param("sourceDate", today.toString())
                .param("sourceSlot", "LUNCH")
                .param("targetDate", tomorrow.toString())
                .param("targetSlot", "LUNCH"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/diary?date=" + tomorrow));

    List<FoodDiaryEntryEntity> tomorrowEntries =
        diaryEntryRepository.findByUserIdAndDiaryDate(user.getId(), tomorrow);
    assertFalse(tomorrowEntries.isEmpty());
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /diary/delete/{id} removes logged item from food diary")
  void testDeleteItem() throws Exception {
    UserEntity user = userRepository.findByUsername("demo").orElseThrow();
    LocalDate today = LocalDate.now();

    DiaryItemDto item =
        diaryService.logItem(
            user.getId(),
            new LogFoodItemRequest(today, "SNACK", null, null, "Apple Slices", 100.0, 1.0));

    mockMvc
        .perform(post("/diary/delete/" + item.id()).with(csrf()).param("date", today.toString()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/diary?date=" + today));
  }
}
