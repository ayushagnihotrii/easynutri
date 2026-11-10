package com.easynutri.dto;

import java.time.LocalDate;

public record LogWorkoutRequest(
    LocalDate date,
    String activityType,
    String intensity,
    int durationMinutes,
    Double distanceKm,
    Double customCaloriesBurned,
    String notes) {}
