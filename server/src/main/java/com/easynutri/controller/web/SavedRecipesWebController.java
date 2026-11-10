package com.easynutri.controller.web;

import com.easynutri.dto.RecipeCollectionDto;
import com.easynutri.dto.SavedRecipeDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.RecipeCollectionService;
import com.easynutri.service.SavedRecipeService;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/saved-recipes")
public class SavedRecipesWebController {

  private final SavedRecipeService savedRecipeService;
  private final RecipeCollectionService collectionService;
  private final UserRepository userRepository;

  public SavedRecipesWebController(
      SavedRecipeService savedRecipeService,
      RecipeCollectionService collectionService,
      UserRepository userRepository) {
    this.savedRecipeService = savedRecipeService;
    this.collectionService = collectionService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public String savedRecipesPage(
      @RequestParam(value = "collectionId", required = false) Long collectionId,
      @RequestParam(value = "search", required = false) String search,
      @RequestParam(value = "tag", required = false) String tag,
      Model model,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Saved Recipes & Collections");
    model.addAttribute("activeNav", "saved-recipes");

    List<RecipeCollectionDto> collections = collectionService.getUserCollections(userId);
    model.addAttribute("collections", collections);
    model.addAttribute("activeCollectionId", collectionId);
    model.addAttribute("searchQuery", search != null ? search : "");
    model.addAttribute("activeTag", tag != null ? tag : "");

    List<SavedRecipeDto> rawList = savedRecipeService.getSavedRecipes(userId);
    List<SavedRecipeDto> filtered = new ArrayList<>(rawList);

    // Apply collection filter if specified
    if (collectionId != null) {
      Optional<RecipeCollectionDto> selectedCol =
          collections.stream().filter(c -> c.id().equals(collectionId)).findFirst();
      if (selectedCol.isPresent()) {
        List<String> validIds = selectedCol.get().recipeIds();
        filtered =
            filtered.stream()
                .filter(r -> validIds.contains(r.recipeId()))
                .collect(Collectors.toList());
      }
    }

    // Apply search filter
    if (search != null && !search.isBlank()) {
      String q = search.trim().toLowerCase();
      filtered =
          filtered.stream()
              .filter(
                  r ->
                      r.recipeTitleEn().toLowerCase().contains(q)
                          || (r.recipeTitleHi() != null
                              && r.recipeTitleHi().toLowerCase().contains(q))
                          || (r.notes() != null && r.notes().toLowerCase().contains(q)))
              .collect(Collectors.toList());
    }

    // Apply tag/slot filter
    if (tag != null && !tag.isBlank()) {
      filtered =
          filtered.stream()
              .filter(r -> tag.equalsIgnoreCase(r.mealSlot()))
              .collect(Collectors.toList());
    }

    model.addAttribute("savedRecipes", filtered);
    model.addAttribute("totalSavedCount", filtered.size());

    return "saved-recipes/index";
  }

  @PostMapping("/collections/create")
  public String createCollection(
      @RequestParam("name") String name,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    collectionService.createCollection(userId, name);
    redirectAttributes.addFlashAttribute(
        "flashMessage", "Collection \"" + name + "\" created successfully!");
    return "redirect:/saved-recipes";
  }

  @PostMapping("/collections/delete")
  public String deleteCollection(
      @RequestParam("collectionId") Long collectionId,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    collectionService.deleteCollection(userId, collectionId);
    redirectAttributes.addFlashAttribute("flashMessage", "Collection deleted.");
    return "redirect:/saved-recipes";
  }

  @PostMapping("/collections/add")
  public String addRecipeToCollection(
      @RequestParam("collectionId") Long collectionId,
      @RequestParam("recipeId") String recipeId,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    collectionService.addRecipeToCollection(userId, collectionId, recipeId);
    redirectAttributes.addFlashAttribute("flashMessage", "Recipe added to collection.");
    return "redirect:/saved-recipes?collectionId=" + collectionId;
  }

  @PostMapping("/collections/remove")
  public String removeRecipeFromCollection(
      @RequestParam("collectionId") Long collectionId,
      @RequestParam("recipeId") String recipeId,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    collectionService.removeRecipeFromCollection(userId, collectionId, recipeId);
    redirectAttributes.addFlashAttribute("flashMessage", "Recipe removed from collection.");
    return "redirect:/saved-recipes?collectionId=" + collectionId;
  }

  @PostMapping("/notes")
  public String updateNotes(
      @RequestParam("recipeId") String recipeId,
      @RequestParam("notes") String notes,
      @RequestParam(value = "collectionId", required = false) Long collectionId,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    savedRecipeService.updateNotes(userId, recipeId, notes);
    redirectAttributes.addFlashAttribute("flashMessage", "Personal recipe notes updated.");
    String param = collectionId != null ? "?collectionId=" + collectionId : "";
    return "redirect:/saved-recipes" + param;
  }

  @PostMapping("/toggle")
  public String toggleSave(
      @RequestParam("recipeId") String recipeId,
      @RequestParam(value = "redirectUrl", defaultValue = "/saved-recipes") String redirectUrl,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    if (savedRecipeService.isRecipeSaved(userId, recipeId)) {
      savedRecipeService.unsaveRecipe(userId, recipeId);
    } else {
      savedRecipeService.saveRecipe(userId, recipeId, "");
    }

    return "redirect:" + redirectUrl;
  }

  private Long getUserId(String username) {
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
