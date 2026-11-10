package com.easynutri.dto;

import java.time.LocalDate;

public record LogWaterRequest(LocalDate date, int amountMl, String notes) {}
