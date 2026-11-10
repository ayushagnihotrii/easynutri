package com.easynutri.dto;

import java.time.LocalDate;

public record PantryItemDto(
    Long id,
    String foodId,
    String nameEn,
    String nameHi,
    double quantityG,
    String unit,
    String storageLocation,
    LocalDate expirationDate,
    LocalDate purchaseDate,
    long daysRemaining,
    String urgencyLevel,
    String notes) {

  public PantryItemDto(
      Long id,
      String foodId,
      String nameEn,
      String nameHi,
      double quantityG,
      LocalDate expirationDate,
      long daysRemaining,
      String urgencyLevel) {
    this(
        id,
        foodId,
        nameEn,
        nameHi,
        quantityG,
        "g",
        "CUPBOARD",
        expirationDate,
        null,
        daysRemaining,
        urgencyLevel,
        "");
  }

  public long daysUntilExpiry() {
    return daysRemaining();
  }

  public String foodName() {
    return nameEn();
  }
}
