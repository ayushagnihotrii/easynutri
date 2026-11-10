package com.easynutri.domain.planner;

import com.easynutri.domain.model.ProfileContext;
import java.util.Set;

/**
 * Domain provider interface for identifying active micronutrient gaps. Uses an empty/no-op provider
 * until genuine diary tracking data is implemented in Phase 4.
 */
public interface NutrientGapProvider {
  Set<String> getNutrientGaps(ProfileContext profile);
}
