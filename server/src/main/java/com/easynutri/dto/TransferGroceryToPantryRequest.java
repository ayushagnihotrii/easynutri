package com.easynutri.dto;

import java.time.LocalDate;

public record TransferGroceryToPantryRequest(
    Long groceryItemId, LocalDate expirationDate, String storageLocation) {}
