package com.easynutri;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.RecipeCollectionService;
import com.easynutri.service.SavedRecipeService;
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
class SavedRecipesWebControllerTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private UserRepository userRepository;
  @Autowired private RecipeRepository recipeRepository;
  @Autowired private SavedRecipeService savedRecipeService;
  @Autowired private RecipeCollectionService collectionService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /saved-recipes loads collections, tabs, and bookmarked meals")
  void testSavedRecipesPage() throws Exception {
    mockMvc
        .perform(get("/saved-recipes"))
        .andExpect(status().isOk())
        .andExpect(view().name("saved-recipes/index"))
        .andExpect(model().attributeExists("collections"))
        .andExpect(model().attributeExists("savedRecipes"))
        .andExpect(model().attributeExists("totalSavedCount"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /saved-recipes/toggle saves and unsaves recipes")
  void testToggleSave() throws Exception {
    RecipeEntity recipe = recipeRepository.findAll().get(0);

    mockMvc
        .perform(post("/saved-recipes/toggle").with(csrf()).param("recipeId", recipe.getId()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/saved-recipes"));

    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    assertTrue(savedRecipeService.isRecipeSaved(demo.getId(), recipe.getId()));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /saved-recipes/collections/create creates a custom user collection")
  void testCreateCollection() throws Exception {
    mockMvc
        .perform(
            post("/saved-recipes/collections/create")
                .with(csrf())
                .param("name", "High Protein Dinners"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/saved-recipes"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /saved-recipes/notes updates personal notes on a saved recipe")
  void testUpdateNotes() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    RecipeEntity recipe = recipeRepository.findAll().get(0);
    savedRecipeService.saveRecipe(demo.getId(), recipe.getId(), "Initial note");

    mockMvc
        .perform(
            post("/saved-recipes/notes")
                .with(csrf())
                .param("recipeId", recipe.getId())
                .param("notes", "Add extra cumin and green chillies"))
        .andExpect(status().is3xxRedirection());
  }
}
