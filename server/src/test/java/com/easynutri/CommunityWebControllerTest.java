package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.CommunityRecipeDto;
import com.easynutri.dto.CreateCommunityRecipeRequest;
import com.easynutri.dto.RecipeIngredientInputDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.CommunityRecipeRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.CommunityService;
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
class CommunityWebControllerTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private CommunityService communityService;
  @Autowired private CommunityRecipeRepository communityRecipeRepository;
  @Autowired private UserRepository userRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /community renders social feeds (latest, popular, following)")
  void testCommunityFeedLoads() throws Exception {
    mockMvc
        .perform(get("/community"))
        .andExpect(status().isOk())
        .andExpect(view().name("community/index"))
        .andExpect(model().attributeExists("recipes"))
        .andExpect(model().attribute("currentFeed", "latest"))
        .andExpect(model().attribute("activeNav", "community"));

    mockMvc
        .perform(get("/community").param("feed", "popular"))
        .andExpect(status().isOk())
        .andExpect(view().name("community/index"))
        .andExpect(model().attribute("currentFeed", "popular"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /community/create renders recipe composer form")
  void testRecipeComposerFormLoads() throws Exception {
    mockMvc
        .perform(get("/community/create"))
        .andExpect(status().isOk())
        .andExpect(view().name("community/create"))
        .andExpect(model().attributeExists("popularFoods"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName(
      "POST /community/create publishes recipe with exact calculated macros and dietary tags")
  void testPublishRecipe() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();

    mockMvc
        .perform(
            post("/community/create")
                .with(csrf())
                .param("title", "Mediterranean Chickpea Skillet")
                .param("description", "A vibrant plant-powered skillet meal packed with fiber.")
                .param("coverImageUrl", "https://images.unsplash.com/photo-example")
                .param("baseServings", "2")
                .param("prepMinutes", "10")
                .param("cookMinutes", "15")
                .param("mealSlot", "DINNER")
                .param(
                    "instructionsText",
                    "Step 1: Rinse chickpeas.\nStep 2: Sauté in olive oil with herbs.")
                .param("ingredientName", "Chickpeas")
                .param("amountG", "200")
                .param("unit", "g"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/community/recipes/*"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /community/create blocks dangerous medical claims via ContentScreener")
  void testBlockDangerousRecipe() throws Exception {
    mockMvc
        .perform(
            post("/community/create")
                .with(csrf())
                .param("title", "Miracle Brew that cures diabetes completely")
                .param("description", "Guaranteed to replace your insulin shots forever.")
                .param("baseServings", "1")
                .param("prepMinutes", "5")
                .param("cookMinutes", "5")
                .param("mealSlot", "BREAKFAST")
                .param(
                    "instructionsText",
                    "Drink this concoction and throw away all prescription medications.")
                .param("ingredientName", "Lemon Juice")
                .param("amountG", "50")
                .param("unit", "ml"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/community/create"))
        .andExpect(flash().attributeExists("flashError"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /community/recipes/{id}/like toggles like state")
  void testToggleLike() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    CreateCommunityRecipeRequest req =
        new CreateCommunityRecipeRequest(
            "Lentil Soup",
            "Wholesome lentil soup",
            null,
            2,
            10,
            20,
            "LUNCH",
            "Simmer lentils and veggies.",
            List.of(new RecipeIngredientInputDto(null, "Lentils", 150.0, "g")));
    CommunityRecipeDto published = communityService.publishRecipe(demo.getId(), req);

    // 1st like -> liked
    mockMvc
        .perform(post("/community/recipes/" + published.id() + "/like").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/community/recipes/" + published.id()))
        .andExpect(flash().attribute("flashMessage", "Recipe saved to your liked recipes!"));

    // 2nd like -> unliked
    mockMvc
        .perform(post("/community/recipes/" + published.id() + "/like").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/community/recipes/" + published.id()))
        .andExpect(flash().attribute("flashMessage", "Recipe unliked."));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /community/recipes/{id}/comments posts comment; DELETE removes comment")
  void testCommentWorkflow() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    CreateCommunityRecipeRequest req =
        new CreateCommunityRecipeRequest(
            "Bean Salad",
            "Refreshing fiber salad",
            null,
            1,
            5,
            0,
            "LUNCH",
            "Toss ingredients together.",
            List.of(new RecipeIngredientInputDto(null, "Black Beans", 100.0, "g")));
    CommunityRecipeDto published = communityService.publishRecipe(demo.getId(), req);

    mockMvc
        .perform(
            post("/community/recipes/" + published.id() + "/comments")
                .with(csrf())
                .param("content", "Loved making this! Added fresh lemon juice."))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/community/recipes/" + published.id()));

    var comments = communityService.getCommentsForRecipe(published.id(), demo.getId());
    assertFalse(comments.isEmpty());
    Long commentId = comments.get(0).id();

    // Delete comment
    mockMvc
        .perform(
            post("/community/comments/" + commentId + "/delete")
                .with(csrf())
                .param("recipeId", String.valueOf(published.id())))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/community/recipes/" + published.id()));

    var updatedComments = communityService.getCommentsForRecipe(published.id(), demo.getId());
    assertTrue(updatedComments.isEmpty());
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /community/users/{username} loads creator public profile")
  void testCreatorProfile() throws Exception {
    mockMvc
        .perform(get("/community/users/demo"))
        .andExpect(status().isOk())
        .andExpect(view().name("community/profile"))
        .andExpect(model().attributeExists("profile"));
  }
}
