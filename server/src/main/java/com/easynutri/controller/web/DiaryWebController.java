package com.easynutri.controller.web;

import com.easynutri.dto.CopyMealRequest;
import com.easynutri.dto.DiaryDayDto;
import com.easynutri.dto.LogFoodItemRequest;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodRepository;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.DiaryService;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/diary")
public class DiaryWebController {

  private final DiaryService diaryService;
  private final UserRepository userRepository;
  private final FoodRepository foodRepository;
  private final RecipeRepository recipeRepository;

  public DiaryWebController(
      DiaryService diaryService,
      UserRepository userRepository,
      FoodRepository foodRepository,
      RecipeRepository recipeRepository) {
    this.diaryService = diaryService;
    this.userRepository = userRepository;
    this.foodRepository = foodRepository;
    this.recipeRepository = recipeRepository;
  }

  @GetMapping
  public String foodDiary(
      @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      Model model,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);
    LocalDate targetDate = date != null ? date : LocalDate.now();

    DiaryDayDto diaryDay = diaryService.getDayDiary(userId, targetDate);

    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Food Diary: " + diaryDay.formattedDate());
    model.addAttribute("activeNav", "diary");
    model.addAttribute("diaryDay", diaryDay);
    model.addAttribute("currentDate", targetDate);
    model.addAttribute("prevDate", targetDate.minusDays(1));
    model.addAttribute("nextDate", targetDate.plusDays(1));
    model.addAttribute("todayDate", LocalDate.now());
    model.addAttribute("isToday", targetDate.equals(LocalDate.now()));

    // Available foods and recipes for quick selection modal
    model.addAttribute("foods", foodRepository.findAll().stream().limit(50).toList());
    model.addAttribute("recipes", recipeRepository.findAll().stream().limit(30).toList());

    return "diary/index";
  }

  @PostMapping("/log")
  public String logItem(
      @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam("mealSlot") String mealSlot,
      @RequestParam(value = "foodId", required = false) String foodId,
      @RequestParam(value = "recipeId", required = false) String recipeId,
      @RequestParam(value = "customName", required = false) String customName,
      @RequestParam(value = "amountG", required = false) Double amountG,
      @RequestParam(value = "servings", required = false) Double servings,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    try {
      LogFoodItemRequest req =
          new LogFoodItemRequest(date, mealSlot, foodId, recipeId, customName, amountG, servings);
      diaryService.logItem(userId, req);
      redirectAttributes.addFlashAttribute(
          "flashMessage", "Food portion successfully logged to your diary!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("flashError", "Failed to log entry: " + e.getMessage());
    }

    return "redirect:/diary?date=" + date;
  }

  @PostMapping("/duplicate")
  public String duplicateMeal(
      @RequestParam("sourceDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate sourceDate,
      @RequestParam("sourceSlot") String sourceSlot,
      @RequestParam("targetDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate targetDate,
      @RequestParam("targetSlot") String targetSlot,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    try {
      CopyMealRequest req = new CopyMealRequest(sourceDate, sourceSlot, targetDate, targetSlot);
      diaryService.duplicateMeal(userId, req);
      redirectAttributes.addFlashAttribute("flashMessage", "Meal successfully copied!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("flashError", "Could not copy meal: " + e.getMessage());
    }

    return "redirect:/diary?date=" + targetDate;
  }

  @PostMapping("/delete/{itemId}")
  public String deleteItem(
      @PathVariable("itemId") Long itemId,
      @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);
    LocalDate targetDate = date != null ? date : LocalDate.now();

    try {
      diaryService.deleteItem(userId, itemId);
      redirectAttributes.addFlashAttribute("flashMessage", "Entry removed from diary.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Failed to delete item: " + e.getMessage());
    }

    return "redirect:/diary?date=" + targetDate;
  }

  private Long getUserId(String username) {
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
