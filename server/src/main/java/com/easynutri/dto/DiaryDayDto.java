package com.easynutri.dto;

import java.time.LocalDate;
import java.util.List;

public record DiaryDayDto(
    LocalDate date,
    String formattedDate,
    List<DiarySlotDto> slots,
    double totalCalories,
    double totalProteinG,
    double totalCarbsG,
    double totalFatG,
    double totalFiberG,
    double totalSodiumMg,
    Integer targetCalories,
    Integer targetProteinG,
    Integer targetCarbG,
    Integer targetFatG,
    boolean sensitiveMode,
    String formattedCalories,
    String formattedProtein,
    String formattedCarbs,
    String formattedFat,
    int calorieProgressPercent,
    int proteinProgressPercent,
    int carbsProgressPercent,
    int fatProgressPercent) {}
