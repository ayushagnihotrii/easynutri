package com.easynutri.dto;

import java.time.LocalDate;
import java.util.List;

public record HydrationSummaryDto(
    LocalDate date,
    String formattedDate,
    int currentIntakeMl,
    int dailyTargetMl,
    int progressPercent,
    boolean isClinicallyRestricted,
    String clinicalNotice,
    String recommendation,
    List<WaterLogDto> logs,
    boolean reminderEnabled,
    int reminderIntervalHours,
    String startTime,
    String endTime) {}
