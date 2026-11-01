package com.easynutri.domain.planner;

import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.model.RecipeCandidate;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Contract for generating and validating personalized meal plans. */
public interface MealPlanner {

  /**
   * Generates a weekly plan fulfilling calorie and macro goals while enforcing health rules.
   *
   * @param startDate starting date of the plan
   * @param profile user's health profile and clinical constraints
   * @param availableRecipes catalog of candidate recipes
   * @return map of dates to daily meal lists
   */
  Map<LocalDate, List<RecipeCandidate>> generateWeeklyPlan(
      LocalDate startDate, ProfileContext profile, List<RecipeCandidate> availableRecipes);

  /**
   * Validates if a proposed day plan complies with user dietary and health safety rules.
   *
   * @param meals meals scheduled for the day
   * @param profile user's health profile
   * @return true if all safety gates pass
   */
  boolean validateDailyPlan(List<RecipeCandidate> meals, ProfileContext profile);
}
