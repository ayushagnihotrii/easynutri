package com.easynutri.dto;

import java.time.LocalDate;

public record LogStepsRequest(LocalDate date, int steps, Integer goalSteps, String sourceType) {}
