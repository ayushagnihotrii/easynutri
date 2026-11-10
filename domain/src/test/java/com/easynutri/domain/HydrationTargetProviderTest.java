package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.hydration.DefaultHydrationTargetProvider;
import com.easynutri.domain.hydration.HydrationTargetProvider;
import com.easynutri.domain.hydration.HydrationTargetResult;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HydrationTargetProviderTest {

  private final HydrationTargetProvider provider = new DefaultHydrationTargetProvider();

  @Test
  void shouldCalculateStandardTargetBasedOnWeight() {
    // 70 kg * 35 ml = 2450 ml
    HydrationTargetResult result = provider.calculateTarget(70.0, 0, Set.of(), null);
    assertEquals(2450, result.dailyTargetMl());
    assertFalse(result.isClinicallyRestricted());
    assertNull(result.clinicalNotice());
  }

  @Test
  void shouldAddActivitySupplement() {
    // 70 kg * 35 ml = 2450 ml + 60 min workout (2 * 300ml = 600ml) = 3050 ml
    HydrationTargetResult result = provider.calculateTarget(70.0, 60, Set.of(), null);
    assertEquals(3050, result.dailyTargetMl());
    assertFalse(result.isClinicallyRestricted());
  }

  @Test
  void shouldApplyClinicalFluidRestrictionForHeartFailure() {
    HydrationTargetResult result =
        provider.calculateTarget(80.0, 0, Set.of("CONGESTIVE_HEART_FAILURE"), null);
    assertEquals(1500, result.dailyTargetMl());
    assertTrue(result.isClinicallyRestricted());
    assertNotNull(result.clinicalNotice());
    assertTrue(result.clinicalNotice().toLowerCase().contains("heart"));
  }

  @Test
  void shouldHonorDoctorPrescribedTarget() {
    HydrationTargetResult result =
        provider.calculateTarget(70.0, 0, Set.of("CHRONIC_KIDNEY_DISEASE"), 1200);
    assertEquals(1200, result.dailyTargetMl());
    assertTrue(result.isClinicallyRestricted());
  }
}
