package com.easynutri.dto;

import java.time.LocalDate;

public record UpdatePantryItemRequest(
    Double quantityG,
    String unit,
    String storageLocation,
    LocalDate expirationDate,
    String notes) {}
