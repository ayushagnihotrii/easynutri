package com.easynutri.dto;

public record SaveHydrationPreferencesRequest(
    Integer dailyTargetMl,
    Boolean reminderEnabled,
    Integer reminderIntervalHours,
    String startTime,
    String endTime) {}
