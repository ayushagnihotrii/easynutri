package com.easynutri.domain.insights;

import com.easynutri.domain.model.ProfileContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Pure Java clinical implementation of dietary progress analysis and rule-based insights. */
public final class DefaultInsightEngine implements InsightEngine {

  private static final int MINIMUM_INSIGHT_DAYS = 3;

  @Override
  public List<String> generateWeeklyInsights(
      Map<String, Map<String, Double>> weeklyIntake, ProfileContext profile) {
    List<String> insights = new ArrayList<>();

    if (weeklyIntake == null || weeklyIntake.size() < MINIMUM_INSIGHT_DAYS) {
      insights.add(
          "Continue logging your meals for at least "
              + MINIMUM_INSIGHT_DAYS
              + " days to reveal meaningful nutritional trends.");
      return insights;
    }

    int daysCount = weeklyIntake.size();
    double totalCalories = 0.0;
    double totalProtein = 0.0;
    double totalFiber = 0.0;
    double totalSodium = 0.0;

    for (Map<String, Double> dayMap : weeklyIntake.values()) {
      if (dayMap == null) {
        continue;
      }
      totalCalories += dayMap.getOrDefault("calories", 0.0);
      totalProtein += dayMap.getOrDefault("protein", 0.0);
      totalFiber += dayMap.getOrDefault("fiber", 0.0);
      totalSodium += dayMap.getOrDefault("sodium", 0.0);
    }

    double avgCalories = totalCalories / daysCount;
    double avgProtein = totalProtein / daysCount;
    double avgFiber = totalFiber / daysCount;
    double avgSodium = totalSodium / daysCount;

    // 1. Protein consistency
    if (avgProtein >= 60.0) {
      insights.add(
          String.format(
              Locale.US,
              "Excellent protein intake! You averaged %.0fg of daily protein across %d logged days.",
              avgProtein,
              daysCount));
    } else if (avgProtein < 45.0) {
      insights.add(
          String.format(
              Locale.US,
              "Daily protein averaged %.0fg. Consider incorporating lentils, paneer, tofu, or sprouts to reach optimal targets.",
              avgProtein));
    }

    // 2. Dietary Fiber intake
    if (avgFiber >= 25.0) {
      insights.add(
          String.format(
              Locale.US,
              "High fiber champion: Averaged %.0fg fiber/day, promoting metabolic and digestive health.",
              avgFiber));
    } else if (avgFiber < 18.0) {
      insights.add(
          "Daily fiber is below recommended 25g. Adding leafy greens, oats, or whole legumes will enhance gut satiety.");
    }

    // 3. Sodium & Hypertension Watch
    boolean isHypertensive =
        profile != null
            && profile.healthConditions() != null
            && profile.healthConditions().stream()
                .anyMatch(c -> c.toLowerCase(Locale.ROOT).contains("hyper"));

    if (isHypertensive && avgSodium > 2000.0) {
      insights.add(
          String.format(
              Locale.US,
              "Clinical Alert: Average sodium was %.0f mg/day, exceeding the 2,000 mg hypertensive safety threshold. Limit pickled foods and salted papad.",
              avgSodium));
    }

    // 4. Caloric stability
    insights.add(
        String.format(
            Locale.US,
            "Logged %d active days with an average daily energy intake of %.0f kcal.",
            daysCount,
            avgCalories));

    return insights;
  }

  @Override
  public double calculateAdherenceScore(int consecutiveLoggedDays, double macroAdherenceRate) {
    double streakFactor = Math.min(1.0, consecutiveLoggedDays / 7.0) * 40.0;
    double macroFactor = Math.max(0.0, Math.min(1.0, macroAdherenceRate)) * 60.0;
    return Math.round((streakFactor + macroFactor) * 10.0) / 10.0;
  }
}
