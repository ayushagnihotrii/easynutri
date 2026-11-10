package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.insights.DefaultInsightEngine;
import com.easynutri.domain.insights.InsightEngine;
import com.easynutri.domain.model.ProfileContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultInsightEngineTest {

  private final InsightEngine engine = new DefaultInsightEngine();

  @Test
  void shouldRequireMinimumDaysBeforeGeneratingTrends() {
    Map<String, Map<String, Double>> days = new HashMap<>();
    days.put("2026-11-01", Map.of("calories", 1800.0, "protein", 55.0));
    days.put("2026-11-02", Map.of("calories", 1950.0, "protein", 62.0));

    List<String> insights = engine.generateWeeklyInsights(days, null);
    assertEquals(1, insights.size());
    assertTrue(insights.get(0).contains("at least 3 days"));
  }

  @Test
  void shouldGenerateDietaryAndHypertensionInsights() {
    Map<String, Map<String, Double>> days = new HashMap<>();
    days.put(
        "2026-11-01", Map.of("calories", 1800.0, "protein", 70.0, "fiber", 28.0, "sodium", 2400.0));
    days.put(
        "2026-11-02", Map.of("calories", 1900.0, "protein", 65.0, "fiber", 26.0, "sodium", 2200.0));
    days.put(
        "2026-11-03", Map.of("calories", 1850.0, "protein", 72.0, "fiber", 29.0, "sodium", 2100.0));

    ProfileContext profile =
        new ProfileContext(
            "user-1", 45, "MALE", 172.0, 78.0, "VEGETARIAN", List.of(), List.of("HYPERTENSION"));

    List<String> insights = engine.generateWeeklyInsights(days, profile);
    assertFalse(insights.isEmpty());

    boolean hasProteinInsight = insights.stream().anyMatch(s -> s.contains("protein"));
    boolean hasSodiumAlert = insights.stream().anyMatch(s -> s.contains("hypertensive"));
    boolean hasFiberInsight = insights.stream().anyMatch(s -> s.contains("fiber"));

    assertTrue(hasProteinInsight);
    assertTrue(hasSodiumAlert);
    assertTrue(hasFiberInsight);
  }
}
