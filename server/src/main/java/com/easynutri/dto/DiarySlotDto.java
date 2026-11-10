package com.easynutri.dto;

import com.easynutri.domain.diary.MealSlot;
import java.util.List;

public record DiarySlotDto(
    MealSlot slot,
    String slotName,
    String displayName,
    String icon,
    List<DiaryItemDto> items,
    double totalCalories,
    double totalProteinG,
    double totalCarbsG,
    double totalFatG,
    double totalFiberG,
    double totalSodiumMg) {}
