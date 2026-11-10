package com.easynutri.dto;

import java.time.Instant;
import java.time.LocalDate;

public record WaterLogDto(
    Long id, LocalDate date, int amountMl, String notes, Instant loggedAt, String formattedTime) {}
