package com.easynutri.domain.hydration;

/** Nutritional and clinical assessment result for daily fluid intake target. */
public record HydrationTargetResult(
    int dailyTargetMl,
    boolean isClinicallyRestricted,
    String clinicalNotice,
    String recommendation) {}
