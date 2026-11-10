package com.easynutri.domain.gamification;

/** Value object containing calculated streak metrics for a specific activity habit. */
public record StreakCalculationResult(
    int currentStreak, int longestStreak, boolean loggedToday, boolean gracePeriodActive) {}
