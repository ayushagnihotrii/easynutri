package com.easynutri.dto;

import java.time.LocalDate;

public record UserStreakDto(
    String streakType,
    String streakLabel,
    String icon,
    int currentStreakDays,
    int longestStreakDays,
    LocalDate lastActivityDate,
    boolean loggedToday,
    boolean gracePeriodActive) {}
