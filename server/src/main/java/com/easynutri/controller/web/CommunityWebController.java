package com.easynutri.controller.web;

import com.easynutri.dto.CommunityCommentDto;
import com.easynutri.dto.CommunityRecipeDto;
import com.easynutri.dto.CreateCommunityRecipeRequest;
import com.easynutri.dto.CreatorProfileDto;
import com.easynutri.dto.RecipeIngredientInputDto;
import com.easynutri.entity.FoodEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.CommunityService;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/community")
public class CommunityWebController {

  private final CommunityService communityService;
  private final UserRepository userRepository;
  private final FoodRepository foodRepository;

  public CommunityWebController(
      CommunityService communityService,
      UserRepository userRepository,
      FoodRepository foodRepository) {
    this.communityService = communityService;
    this.userRepository = userRepository;
    this.foodRepository = foodRepository;
  }

  @GetMapping
  public String communityFeed(
      @RequestParam(value = "feed", defaultValue = "latest") String feed,
      @RequestParam(value = "q", required = false) String query,
      Model model,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    List<CommunityRecipeDto> recipes = communityService.getFeed(currentUserId, feed, query);

    model.addAttribute("recipes", recipes);
    model.addAttribute("currentFeed", feed.toLowerCase());
    model.addAttribute("searchQuery", query != null ? query : "");
    model.addAttribute("username", username);
    model.addAttribute("currentUserId", currentUserId);
    model.addAttribute("activeNav", "community");
    model.addAttribute("pageTitle", "Community Kitchen & Recipes");

    return "community/index";
  }

  @GetMapping("/create")
  public String createRecipeForm(Model model, Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    List<FoodEntity> popularFoods = foodRepository.findAll().stream().limit(60).toList();

    model.addAttribute("popularFoods", popularFoods);
    model.addAttribute("username", username);
    model.addAttribute("currentUserId", currentUserId);
    model.addAttribute("activeNav", "community");
    model.addAttribute("pageTitle", "Share a Recipe — Community Kitchen");

    return "community/create";
  }

  @PostMapping("/create")
  public String createRecipe(
      @RequestParam("title") String title,
      @RequestParam("description") String description,
      @RequestParam(value = "coverImageUrl", required = false) String coverImageUrl,
      @RequestParam(value = "baseServings", defaultValue = "2") int baseServings,
      @RequestParam(value = "prepMinutes", defaultValue = "15") int prepMinutes,
      @RequestParam(value = "cookMinutes", defaultValue = "20") int cookMinutes,
      @RequestParam(value = "mealSlot", defaultValue = "LUNCH") String mealSlot,
      @RequestParam("instructionsText") String instructionsText,
      @RequestParam(value = "foodId", required = false) List<String> foodIds,
      @RequestParam(value = "ingredientName", required = false) List<String> ingredientNames,
      @RequestParam(value = "amountG", required = false) List<Double> amountsG,
      @RequestParam(value = "unit", required = false) List<String> units,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    List<RecipeIngredientInputDto> ingredients = new ArrayList<>();
    if (ingredientNames != null) {
      for (int i = 0; i < ingredientNames.size(); i++) {
        String name = ingredientNames.get(i);
        if (name != null && !name.trim().isEmpty()) {
          String fid = (foodIds != null && i < foodIds.size()) ? foodIds.get(i) : null;
          double amt =
              (amountsG != null && i < amountsG.size() && amountsG.get(i) != null)
                  ? amountsG.get(i)
                  : 100.0;
          String un =
              (units != null && i < units.size() && units.get(i) != null) ? units.get(i) : "g";
          ingredients.add(new RecipeIngredientInputDto(fid, name.trim(), amt, un));
        }
      }
    }

    if (ingredients.isEmpty()) {
      // Default fallback ingredient if user submitted without any
      ingredients.add(new RecipeIngredientInputDto(null, "Mixed Vegetables", 150.0, "g"));
    }

    CreateCommunityRecipeRequest request =
        new CreateCommunityRecipeRequest(
            title,
            description,
            coverImageUrl != null && !coverImageUrl.isBlank()
                ? coverImageUrl
                : "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=800&q=80",
            baseServings,
            prepMinutes,
            cookMinutes,
            mealSlot,
            instructionsText,
            ingredients);

    try {
      CommunityRecipeDto created = communityService.publishRecipe(currentUserId, request);
      if ("PENDING_REVIEW".equals(created.status())) {
        redirectAttributes.addFlashAttribute(
            "flashMessage",
            "Recipe submitted! It is currently under review by our moderation team.");
      } else {
        redirectAttributes.addFlashAttribute(
            "flashMessage", "Recipe published successfully to the community!");
      }
      return "redirect:/community/recipes/" + created.id();
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("flashError", e.getMessage());
      return "redirect:/community/create";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Failed to publish recipe: " + e.getMessage());
      return "redirect:/community/create";
    }
  }

  @GetMapping("/recipes/{id}")
  public String recipeDetail(@PathVariable("id") Long id, Model model, Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    CommunityRecipeDto recipe =
        communityService
            .getRecipeById(id, currentUserId)
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + id));

    List<CommunityCommentDto> comments = communityService.getCommentsForRecipe(id, currentUserId);

    model.addAttribute("recipe", recipe);
    model.addAttribute("comments", comments);
    model.addAttribute("username", username);
    model.addAttribute("currentUserId", currentUserId);
    model.addAttribute("activeNav", "community");
    model.addAttribute("pageTitle", recipe.title() + " — EasyNutri Community");

    return "community/detail";
  }

  @PostMapping("/recipes/{id}/like")
  public String toggleLike(
      @PathVariable("id") Long id, Principal principal, RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    try {
      boolean liked = communityService.toggleLike(currentUserId, id);
      redirectAttributes.addFlashAttribute(
          "flashMessage", liked ? "Recipe saved to your liked recipes!" : "Recipe unliked.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Failed to update like status: " + e.getMessage());
    }

    return "redirect:/community/recipes/" + id;
  }

  @PostMapping("/recipes/{id}/comments")
  public String addComment(
      @PathVariable("id") Long id,
      @RequestParam("content") String content,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    try {
      communityService.addComment(currentUserId, id, content);
      redirectAttributes.addFlashAttribute("flashMessage", "Comment posted successfully!");
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("flashError", e.getMessage());
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Failed to post comment: " + e.getMessage());
    }

    return "redirect:/community/recipes/" + id;
  }

  @PostMapping("/comments/{commentId}/delete")
  public String deleteComment(
      @PathVariable("commentId") Long commentId,
      @RequestParam("recipeId") Long recipeId,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    try {
      communityService.deleteComment(currentUserId, commentId);
      redirectAttributes.addFlashAttribute("flashMessage", "Comment deleted.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Could not delete comment: " + e.getMessage());
    }

    return "redirect:/community/recipes/" + recipeId;
  }

  @PostMapping("/users/{creatorId}/follow")
  public String toggleFollow(
      @PathVariable("creatorId") Long creatorId,
      @RequestParam(value = "returnTo", required = false) String returnTo,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    try {
      boolean following = communityService.toggleFollow(currentUserId, creatorId);
      redirectAttributes.addFlashAttribute(
          "flashMessage",
          following ? "You are now following this creator!" : "Unfollowed creator.");
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("flashError", e.getMessage());
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Could not update follow: " + e.getMessage());
    }

    if (returnTo != null && !returnTo.isBlank()) {
      return "redirect:" + returnTo;
    }
    return "redirect:/community";
  }

  @GetMapping("/users/{username}")
  public String creatorProfile(
      @PathVariable("username") String targetUsername, Model model, Principal principal) {

    String currentUsername = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(currentUsername);

    CreatorProfileDto profile = communityService.getCreatorProfile(targetUsername, currentUserId);

    model.addAttribute("profile", profile);
    model.addAttribute("username", currentUsername);
    model.addAttribute("currentUserId", currentUserId);
    model.addAttribute("activeNav", "community");
    model.addAttribute(
        "pageTitle", profile.fullName() + " (@" + profile.username() + ") — Community Profile");

    return "community/profile";
  }

  private Long getUserId(String username) {
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
