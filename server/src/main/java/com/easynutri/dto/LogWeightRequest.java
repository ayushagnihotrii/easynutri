package com.easynutri.dto;

import java.time.LocalDate;

public record LogWeightRequest(LocalDate date, double weightKg, String notes) {}
