package com.easynutri.domain.gamification;

import java.time.LocalDate;
import java.util.*;

/** Pure Java algorithm computing consecutive streaks with compassionate grace handling. */
public final class StreakCalculator {

  private StreakCalculator() {}

  public static StreakCalculationResult computeStreak(
      Collection<LocalDate> activityDates, LocalDate today) {
    if (activityDates == null || activityDates.isEmpty()) {
      return new StreakCalculationResult(0, 0, false, false);
    }

    LocalDate refDate = today != null ? today : LocalDate.now();
    Set<LocalDate> dates = new HashSet<>(activityDates);

    boolean loggedToday = dates.contains(refDate);
    boolean loggedYesterday = dates.contains(refDate.minusDays(1));

    // If neither today nor yesterday has a log, the active streak has reset to 0
    if (!loggedToday && !loggedYesterday) {
      int longest = computeLongestStreak(dates);
      return new StreakCalculationResult(0, longest, false, false);
    }

    // Determine starting anchor: if today is logged, anchor is today; else yesterday (grace active)
    LocalDate checkDate = loggedToday ? refDate : refDate.minusDays(1);
    boolean graceActive = !loggedToday && loggedYesterday;

    int currentStreak = 0;
    while (dates.contains(checkDate)) {
      currentStreak++;
      checkDate = checkDate.minusDays(1);
    }

    int longestStreak = Math.max(currentStreak, computeLongestStreak(dates));

    return new StreakCalculationResult(currentStreak, longestStreak, loggedToday, graceActive);
  }

  private static int computeLongestStreak(Set<LocalDate> dates) {
    if (dates.isEmpty()) return 0;

    List<LocalDate> sorted = new ArrayList<>(dates);
    Collections.sort(sorted);

    int maxStreak = 1;
    int curStreak = 1;

    for (int i = 1; i < sorted.size(); i++) {
      if (sorted.get(i).equals(sorted.get(i - 1).plusDays(1))) {
        curStreak++;
        if (curStreak > maxStreak) {
          maxStreak = curStreak;
        }
      } else if (!sorted.get(i).equals(sorted.get(i - 1))) {
        curStreak = 1;
      }
    }

    return maxStreak;
  }
}
