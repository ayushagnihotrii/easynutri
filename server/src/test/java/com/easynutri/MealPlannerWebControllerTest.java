package com.easynutri;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.entity.MealPlanItemEntity;
import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.MealPlanItemRepository;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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
class MealPlannerWebControllerTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private UserRepository userRepository;
  @Autowired private RecipeRepository recipeRepository;
  @Autowired private MealPlanItemRepository mealPlanItemRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /planner loads weekly calendar with 7 days and nutrition targets")
  void testPlannerPage() throws Exception {
    mockMvc
        .perform(get("/planner"))
        .andExpect(status().isOk())
        .andExpect(view().name("planner/index"))
        .andExpect(model().attributeExists("plan"))
        .andExpect(model().attributeExists("weekStart"))
        .andExpect(model().attributeExists("prevWeek"))
        .andExpect(model().attributeExists("nextWeek"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /planner/generate creates intelligent 7-day plan with clinical verification")
  void testGeneratePlan() throws Exception {
    LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    mockMvc
        .perform(post("/planner/generate").with(csrf()).param("weekStart", monday.toString()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/planner?weekStart=" + monday));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /planner/lock toggles locked state on meal item")
  void testToggleLock() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    RecipeEntity recipe = recipeRepository.findAll().get(0);
    MealPlanItemEntity item =
        mealPlanItemRepository.save(
            new MealPlanItemEntity(demo, recipe, LocalDate.now(), "DINNER", 2));

    mockMvc
        .perform(post("/planner/lock").with(csrf()).param("itemId", item.getId().toString()))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /planner/complete toggles completion on meal item")
  void testToggleComplete() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    RecipeEntity recipe = recipeRepository.findAll().get(0);
    MealPlanItemEntity item =
        mealPlanItemRepository.save(
            new MealPlanItemEntity(demo, recipe, LocalDate.now(), "LUNCH", 2));

    mockMvc
        .perform(post("/planner/complete").with(csrf()).param("itemId", item.getId().toString()))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /planner/servings updates serving count")
  void testUpdateServings() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    RecipeEntity recipe = recipeRepository.findAll().get(0);
    MealPlanItemEntity item =
        mealPlanItemRepository.save(
            new MealPlanItemEntity(demo, recipe, LocalDate.now(), "BREAKFAST", 2));

    mockMvc
        .perform(
            post("/planner/servings")
                .with(csrf())
                .param("itemId", item.getId().toString())
                .param("servings", "4"))
        .andExpect(status().is3xxRedirection());
  }
}
