package com.easynutri.dto;

import java.time.LocalDate;

public record AddPantryItemRequest(
    String foodId,
    Double quantityG,
    String unit,
    String storageLocation,
    LocalDate expirationDate,
    LocalDate purchaseDate,
    String notes) {}
