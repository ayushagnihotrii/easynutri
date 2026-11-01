package com.easynutri.domain.insights;

import com.easynutri.domain.model.ProfileContext;
import java.util.List;
import java.util.Map;

/** Contract for analyzing nutrition logs and generating personalized clinical insights. */
public interface InsightEngine {

  /**
   * Generates actionable dietary feedback from multi-day food logs and goals.
   *
   * @param weeklyIntake map of dates to daily nutrient totals
   * @param profile user's health profile and conditions
   * @return list of clinical and behavioral insights
   */
  List<String> generateWeeklyInsights(
      Map<String, Map<String, Double>> weeklyIntake, ProfileContext profile);

  /**
   * Evaluates streak stability and consistency score.
   *
   * @param consecutiveLoggedDays current logging streak length
   * @param macroAdherenceRate percentage of days within target macro bounds (0.0 to 1.0)
   * @return qualitative adherence score
   */
  double calculateAdherenceScore(int consecutiveLoggedDays, double macroAdherenceRate);
}
