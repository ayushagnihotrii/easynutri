package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.AddHouseholdMemberRequest;
import com.easynutri.dto.HouseholdDto;
import com.easynutri.entity.FoodEntity;
import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.RecipeIngredientEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodRepository;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.HouseholdMealPlanService;
import com.easynutri.service.HouseholdService;
import java.time.LocalDate;
import java.util.List;
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
class HouseholdMealPlanningTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private HouseholdService householdService;
  @Autowired private HouseholdMealPlanService householdMealPlanService;
  @Autowired private UserRepository userRepository;
  @Autowired private FoodRepository foodRepository;
  @Autowired private RecipeRepository recipeRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName(
      "Recommend household meals evaluates union of allergens across participating members")
  void testHouseholdMealRecommendationsAllergenSafety() {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    HouseholdDto household = householdService.createHousehold(demo.getId(), "Demo Family");

    // Add a child with Peanut allergy
    var member =
        householdService.addMember(
            demo.getId(),
            household.id(),
            new AddHouseholdMemberRequest(
                "Aarav",
                "CHILD",
                "DEPENDENT",
                7,
                "MALE",
                120.0,
                24.0,
                "MODERATE",
                "MAINTAIN",
                "VEGETARIAN",
                List.of("Peanut"),
                List.of(),
                false,
                "#2563EB"));

    // Create recipe containing Peanut
    FoodEntity peanutFood =
        new FoodEntity(
            "food_peanut_test",
            "Peanuts",
            "मूंगफली",
            "NUTS",
            567.0,
            25.8,
            16.1,
            49.2,
            "CURATED",
            true);
    peanutFood.setAllergenTags("Peanut");
    foodRepository.save(peanutFood);

    RecipeEntity peanutRecipe =
        new RecipeEntity(
            "recipe_peanut_chutney",
            "Peanut Chutney",
            "मूंगफली चटनी",
            "Chutney",
            2,
            5,
            5,
            "DINNER",
            "SIDE");
    peanutRecipe.setInstructionsText("Blend.");
    peanutRecipe.setTags("chutney");
    peanutRecipe.setSource("CURATED");
    peanutRecipe.setVerified(true);
    peanutRecipe
        .getIngredients()
        .add(new RecipeIngredientEntity(peanutRecipe, peanutFood, 50.0, false));
    recipeRepository.save(peanutRecipe);

    // Recommend meals for both demo and peanutAllergicMember
    var recs =
        householdMealPlanService.recommendHouseholdMeals(
            household.id(), List.of(demo.getId(), member.userId()));

    var peanutRec =
        recs.stream()
            .filter(r -> "recipe_peanut_chutney".equals(r.recipeId()))
            .findFirst()
            .orElseThrow();

    assertFalse(peanutRec.isSafe());
    assertTrue(peanutRec.violations().get(0).contains("Peanut"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName(
      "POST /family/meal-planner/schedule synchronizes scaled individual portions to member meal plans")
  void testScheduleHouseholdMeal() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    HouseholdDto household = householdService.createHousehold(demo.getId(), "Demo Family");

    var child =
        householdService.addMember(
            demo.getId(),
            household.id(),
            new AddHouseholdMemberRequest(
                "Anaya",
                "CHILD",
                "MEMBER",
                10,
                "FEMALE",
                135.0,
                30.0,
                "LIGHT",
                "MAINTAIN",
                "VEGETARIAN",
                List.of(),
                List.of(),
                false,
                "#EC4899"));

    // Ensure recipe exists for scheduling
    FoodEntity lentilFood =
        new FoodEntity(
            "food_lentil_test",
            "Yellow Lentils",
            "दाल",
            "LEGUMES",
            340.0,
            24.0,
            60.0,
            1.5,
            "CURATED",
            true);
    lentilFood.setDietFlags("VEGETARIAN,VEGAN");
    foodRepository.save(lentilFood);

    RecipeEntity dalRecipe =
        new RecipeEntity(
            "recipe_dal_tadka",
            "Dal Tadka",
            "दाल तड़का",
            "Classic tempered yellow lentils",
            2,
            10,
            20,
            "LUNCH",
            "MAIN");
    dalRecipe.setInstructionsText("Boil and temper.");
    dalRecipe.setTags("curry,dal");
    dalRecipe.setSource("CURATED");
    dalRecipe.setVerified(true);
    dalRecipe.getIngredients().add(new RecipeIngredientEntity(dalRecipe, lentilFood, 100.0, false));
    recipeRepository.save(dalRecipe);

    String planDate = LocalDate.now().plusDays(1).toString();
    String jsonPayload =
        """
        {
          "householdId": %d,
          "recipeId": "recipe_dal_tadka",
          "planDate": "%s",
          "mealSlot": "LUNCH",
          "participatingUserIds": [%d, %d]
        }
        """
            .formatted(household.id(), planDate, demo.getId(), child.userId());

    mockMvc
        .perform(
            post("/api/v1/household/schedule")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.portions").isArray())
        .andExpect(jsonPath("$.portions.length()").value(2));
  }
}
