package com.easynutri.dto;

import java.time.LocalDate;

public record WeightMeasurementDto(
    Long id,
    LocalDate date,
    double weightKg,
    Double bmi,
    String notes,
    String formattedWeight,
    String bmiCategory) {}
