package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.gamification.StreakCalculationResult;
import com.easynutri.domain.gamification.StreakCalculator;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StreakCalculatorTest {

  @Test
  @DisplayName("Calculates consecutive daily streak including today")
  void testActiveStreakIncludingToday() {
    LocalDate today = LocalDate.of(2026, 11, 10);
    Set<LocalDate> dates =
        Set.of(today, today.minusDays(1), today.minusDays(2), today.minusDays(3));

    StreakCalculationResult res = StreakCalculator.computeStreak(dates, today);

    assertEquals(4, res.currentStreak());
    assertEquals(4, res.longestStreak());
    assertTrue(res.loggedToday());
    assertFalse(res.gracePeriodActive());
  }

  @Test
  @DisplayName("Grace period: yesterday logged but today pending retains active streak")
  void testGracePeriodRetainsStreak() {
    LocalDate today = LocalDate.of(2026, 11, 10);
    Set<LocalDate> dates = Set.of(today.minusDays(1), today.minusDays(2), today.minusDays(3));

    StreakCalculationResult res = StreakCalculator.computeStreak(dates, today);

    assertEquals(3, res.currentStreak());
    assertFalse(res.loggedToday());
    assertTrue(res.gracePeriodActive());
  }

  @Test
  @DisplayName("Resets active streak when gap exceeds 1 day while preserving longest streak")
  void testResetStreakWithGap() {
    LocalDate today = LocalDate.of(2026, 11, 10);
    // Gap: last logged 2 days ago
    Set<LocalDate> dates =
        Set.of(today.minusDays(2), today.minusDays(3), today.minusDays(4), today.minusDays(5));

    StreakCalculationResult res = StreakCalculator.computeStreak(dates, today);

    assertEquals(0, res.currentStreak());
    assertEquals(4, res.longestStreak());
    assertFalse(res.loggedToday());
    assertFalse(res.gracePeriodActive());
  }
}
