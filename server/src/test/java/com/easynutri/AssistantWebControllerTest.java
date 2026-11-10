package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.entity.AiConversationEntity;
import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.AiConversationRepository;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.ChatService;
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
class AssistantWebControllerTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private UserRepository userRepository;
  @Autowired private RecipeRepository recipeRepository;
  @Autowired private ChatService chatService;
  @Autowired private AiConversationRepository conversationRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /assistant loads chat interface, sidebar conversations, and prompt chips")
  void testAssistantPage() throws Exception {
    mockMvc
        .perform(get("/assistant"))
        .andExpect(status().isOk())
        .andExpect(view().name("assistant/index"))
        .andExpect(model().attributeExists("conversations"))
        .andExpect(model().attributeExists("activeConversation"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /assistant/chat persists user message and invokes Java tool pipeline")
  void testAssistantChatFlow() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    AiConversationEntity conversation = chatService.getOrCreateActiveConversation(demo.getId());

    mockMvc
        .perform(
            post("/assistant/chat")
                .with(csrf())
                .param("conversationId", String.valueOf(conversation.getId()))
                .param("query", "What is expiring in my pantry?"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/assistant?conversationId=*"));

    AiConversationEntity reloaded =
        conversationRepository.findById(conversation.getId()).orElseThrow();
    assertTrue(
        reloaded.getMessages().size() >= 2, "Conversation should have user and assistant messages");
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /recipes/adapt/{id} displays side-by-side comparison and recalculation")
  void testRecipeAdaptationPage() throws Exception {
    RecipeEntity recipe = recipeRepository.findAll().get(0);

    mockMvc
        .perform(get("/recipes/adapt/" + recipe.getId()).param("type", "VEGAN"))
        .andExpect(status().isOk())
        .andExpect(view().name("recipes/adapt"))
        .andExpect(model().attributeExists("adaptation"))
        .andExpect(model().attributeExists("availableTypes"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /insights loads weekly check-in cards")
  void testWeeklyInsightsPage() throws Exception {
    mockMvc
        .perform(get("/insights"))
        .andExpect(status().isOk())
        .andExpect(view().name("insights/index"))
        .andExpect(model().attributeExists("insights"));
  }
}
