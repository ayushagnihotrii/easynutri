package com.easynutri.dto;

import java.util.List;

public record FoodDto(
    String id,
    String nameEn,
    String nameHi,
    String category,
    double energyKcal,
    double proteinG,
    double carbohydrateG,
    double sugarG,
    double fatG,
    double saturatedFatG,
    double fibreG,
    double sodiumMg,
    double potassiumMg,
    double calciumMg,
    double ironMg,
    List<String> allergenTags,
    List<String> dietFlags,
    String source,
    boolean verified) {}
