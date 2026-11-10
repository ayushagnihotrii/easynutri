package com.easynutri;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.CookingSessionDto;
import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.CookingService;
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
class CookingWebControllerTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private UserRepository userRepository;
  @Autowired private RecipeRepository recipeRepository;
  @Autowired private CookingService cookingService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName(
      "GET /cook/{recipeId} initializes distraction-free cooking mode with scaled checklist")
  void testCookingModePage() throws Exception {
    RecipeEntity recipe = recipeRepository.findAll().get(0);

    mockMvc
        .perform(get("/cook/" + recipe.getId()))
        .andExpect(status().isOk())
        .andExpect(view().name("cook/index"))
        .andExpect(model().attributeExists("session"))
        .andExpect(model().attributeExists("recipe"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /cook/{recipeId}/finish completes session with pantry deduction")
  void testFinishCooking() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    RecipeEntity recipe = recipeRepository.findAll().get(0);

    CookingSessionDto session =
        cookingService.startOrResumeSession(demo.getId(), recipe.getId(), 2);

    mockMvc
        .perform(
            post("/cook/" + recipe.getId() + "/finish")
                .with(csrf())
                .param("sessionId", session.id().toString())
                .param("servings", "2")
                .param("deductPantry", "true")
                .param("logDiary", "true")
                .param("notes", "Turned out delicious!"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/planner"));

    CookingSessionDto finished = cookingService.getSession(demo.getId(), session.id());
    assertTrue(finished.completed());
    assertTrue(finished.pantryDeducted());
  }
}
