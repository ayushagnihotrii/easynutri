package com.easynutri.dto;

import java.time.LocalDate;

public record CopyMealRequest(
    LocalDate sourceDate, String sourceSlot, LocalDate targetDate, String targetSlot) {}
