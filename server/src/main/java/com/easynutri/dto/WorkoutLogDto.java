package com.easynutri.dto;

import java.time.LocalDate;

public record WorkoutLogDto(
    Long id,
    LocalDate date,
    String activityType,
    String displayName,
    String icon,
    String intensity,
    int durationMinutes,
    Double distanceKm,
    double caloriesBurned,
    double metValue,
    String notes,
    String formattedBurn) {}
