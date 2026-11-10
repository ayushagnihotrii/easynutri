package com.easynutri.service;

import com.easynutri.entity.*;
import com.easynutri.repository.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user privacy rights, personal data portability, and account erasure workflows in
 * compliance with modern data protection regulations (e.g. Indian Digital Personal Data Protection
 * Act & GDPR).
 */
@Service
@Transactional
public class UserDataManagementService {

  private static final Logger log = LoggerFactory.getLogger(UserDataManagementService.class);

  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;
  private final FoodDiaryEntryRepository diaryEntryRepository;
  private final WaterLogRepository waterLogRepository;
  private final StepRecordRepository stepRecordRepository;
  private final WorkoutLogRepository workoutLogRepository;
  private final WeightMeasurementRepository weightRepository;
  private final PantryItemRepository pantryRepository;
  private final GroceryItemRepository groceryRepository;
  private final SavedRecipeRepository savedRecipeRepository;
  private final UserStreakRepository streakRepository;
  private final UserBadgeRepository badgeRepository;
  private final HouseholdMemberRepository householdMemberRepository;
  private final HouseholdRepository householdRepository;
  private final PasswordEncoder passwordEncoder;

  public UserDataManagementService(
      UserRepository userRepository,
      UserProfileRepository userProfileRepository,
      FoodDiaryEntryRepository diaryEntryRepository,
      WaterLogRepository waterLogRepository,
      StepRecordRepository stepRecordRepository,
      WorkoutLogRepository workoutLogRepository,
      WeightMeasurementRepository weightRepository,
      PantryItemRepository pantryRepository,
      GroceryItemRepository groceryRepository,
      SavedRecipeRepository savedRecipeRepository,
      UserStreakRepository streakRepository,
      UserBadgeRepository badgeRepository,
      HouseholdMemberRepository householdMemberRepository,
      HouseholdRepository householdRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.userProfileRepository = userProfileRepository;
    this.diaryEntryRepository = diaryEntryRepository;
    this.waterLogRepository = waterLogRepository;
    this.stepRecordRepository = stepRecordRepository;
    this.workoutLogRepository = workoutLogRepository;
    this.weightRepository = weightRepository;
    this.pantryRepository = pantryRepository;
    this.groceryRepository = groceryRepository;
    this.savedRecipeRepository = savedRecipeRepository;
    this.streakRepository = streakRepository;
    this.badgeRepository = badgeRepository;
    this.householdMemberRepository = householdMemberRepository;
    this.householdRepository = householdRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> exportUserData(Long userId) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    Map<String, Object> export = new LinkedHashMap<>();

    // 1. Account Metadata
    Map<String, Object> account = new LinkedHashMap<>();
    account.put("username", user.getUsername());
    account.put("email", user.getEmail());
    account.put("fullName", user.getFullName());
    account.put("createdAt", user.getCreatedAt().toString());
    export.put("account", account);

    // 2. Health & Biometrics Profile
    userProfileRepository
        .findByUserId(userId)
        .ifPresent(
            p -> {
              Map<String, Object> prof = new LinkedHashMap<>();
              prof.put("age", p.getAge());
              prof.put("biologicalSex", p.getBiologicalSex());
              prof.put("heightCm", p.getHeightCm());
              prof.put("weightKg", p.getWeightKg());
              prof.put("activityLevel", p.getActivityLevel());
              prof.put("healthGoal", p.getHealthGoal());
              prof.put("dietaryPreference", p.getDietaryPreference());
              prof.put("allergies", new ArrayList<>(p.getAllergies()));
              prof.put("healthConditions", new ArrayList<>(p.getHealthConditions()));
              prof.put("targetCalories", p.getTargetCalories());
              prof.put("targetProteinG", p.getTargetProteinG());
              prof.put("targetCarbG", p.getTargetCarbG());
              prof.put("targetFatG", p.getTargetFatG());
              prof.put("sensitiveMode", p.isSensitiveMode());
              export.put("profile", prof);
            });

    // 3. Nutrition & Food Diary
    List<FoodDiaryEntryEntity> diaryEntries = diaryEntryRepository.findByUserId(userId);
    List<Map<String, Object>> diaryList = new ArrayList<>();
    for (FoodDiaryEntryEntity entry : diaryEntries) {
      Map<String, Object> e = new LinkedHashMap<>();
      e.put("date", entry.getDiaryDate().toString());
      e.put("mealSlot", entry.getMealSlot());
      List<Map<String, Object>> items = new ArrayList<>();
      for (FoodDiaryItemEntity it : entry.getItems()) {
        Map<String, Object> itemMap = new LinkedHashMap<>();
        itemMap.put("name", it.getItemName());
        itemMap.put("amountG", it.getAmountG());
        itemMap.put("servings", it.getServings());
        itemMap.put("calories", it.getCalories());
        itemMap.put("proteinG", it.getProteinG());
        itemMap.put("carbsG", it.getCarbsG());
        itemMap.put("fatG", it.getFatG());
        items.add(itemMap);
      }
      e.put("items", items);
      diaryList.add(e);
    }
    export.put("diaryEntries", diaryList);

    // 4. Hydration Logs
    List<WaterLogEntity> waterLogs = waterLogRepository.findByUserIdOrderByLoggedAtDesc(userId);
    List<Map<String, Object>> waterList =
        waterLogs.stream()
            .map(
                w -> {
                  Map<String, Object> wm = new LinkedHashMap<>();
                  wm.put("date", w.getRecordDate().toString());
                  wm.put("amountMl", w.getAmountMl());
                  wm.put("notes", w.getNotes() != null ? w.getNotes() : "");
                  wm.put("loggedAt", w.getLoggedAt().toString());
                  return wm;
                })
            .toList();
    export.put("hydrationLogs", waterList);

    // 5. Activity & Workouts
    List<WorkoutLogEntity> workouts =
        workoutLogRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
            userId, LocalDate.MIN, LocalDate.MAX);
    List<Map<String, Object>> workoutList =
        workouts.stream()
            .map(
                w -> {
                  Map<String, Object> wom = new LinkedHashMap<>();
                  wom.put("date", w.getRecordDate().toString());
                  wom.put("activityType", w.getActivityType());
                  wom.put("intensity", w.getIntensity());
                  wom.put("durationMinutes", w.getDurationMinutes());
                  wom.put("caloriesBurned", w.getCaloriesBurned());
                  return wom;
                })
            .toList();
    export.put("workoutLogs", workoutList);

    // 6. Pantry Stock
    List<PantryItemEntity> pantry = pantryRepository.findByUserId(userId);
    List<Map<String, Object>> pantryList =
        pantry.stream()
            .map(
                pi -> {
                  Map<String, Object> pim = new LinkedHashMap<>();
                  pim.put(
                      "foodName", pi.getFood() != null ? pi.getFood().getNameEn() : "Custom Item");
                  pim.put("quantityG", pi.getQuantityG());
                  pim.put("storageLocation", pi.getStorageLocation());
                  pim.put("expirationDate", pi.getExpirationDate().toString());
                  return pim;
                })
            .toList();
    export.put("pantryStock", pantryList);

    // 7. Grocery Checklist
    List<GroceryItemEntity> grocery =
        groceryRepository.findByUserIdOrderByCheckedAscCreatedAtDesc(userId);
    List<Map<String, Object>> groceryList =
        grocery.stream()
            .map(
                gi -> {
                  Map<String, Object> gim = new LinkedHashMap<>();
                  gim.put("itemName", gi.getItemName());
                  gim.put("quantity", gi.getQuantity());
                  gim.put("unit", gi.getUnit());
                  gim.put("checked", gi.isChecked());
                  return gim;
                })
            .toList();
    export.put("groceryList", groceryList);

    // 8. Badges & Habits
    List<UserStreakEntity> streaks = streakRepository.findByUserId(userId);
    List<Map<String, Object>> streakList = new ArrayList<>();
    for (UserStreakEntity s : streaks) {
      Map<String, Object> sm = new LinkedHashMap<>();
      sm.put("streakType", s.getStreakType());
      sm.put("currentStreakDays", s.getCurrentStreakDays());
      sm.put("longestStreakDays", s.getLongestStreakDays());
      sm.put(
          "lastActivityDate",
          s.getLastActivityDate() != null ? s.getLastActivityDate().toString() : "");
      streakList.add(sm);
    }
    export.put("streaks", streakList);

    // Export metadata & legal notice
    export.put(
        "legalNotice",
        "Exported under personal data portability principles. EasyNutri is not a medical device. Sourced from ICMR-NIN IFCT 2017 & USDA FoodData Central.");
    export.put("exportTimestamp", Instant.now().toString());
    export.put("formatVersion", "EASYNUTRI_DPDP_V1");

    return export;
  }

  public void deleteUserAccount(Long userId, String passwordConfirmation) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    if (!passwordEncoder.matches(passwordConfirmation, user.getPasswordHash())) {
      throw new IllegalArgumentException(
          "Invalid password confirmation. Account deletion aborted.");
    }

    log.warn(
        "Initiating permanent account erasure for user: {} (ID: {})", user.getUsername(), userId);

    // 1. Delete Personal Telemetry & Biometrics
    userProfileRepository.findByUserId(userId).ifPresent(userProfileRepository::delete);

    List<FoodDiaryEntryEntity> diary = diaryEntryRepository.findByUserId(userId);
    diaryEntryRepository.deleteAll(diary);

    List<WaterLogEntity> water = waterLogRepository.findByUserIdOrderByLoggedAtDesc(userId);
    waterLogRepository.deleteAll(water);

    List<WorkoutLogEntity> workouts =
        workoutLogRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
            userId, LocalDate.MIN, LocalDate.MAX);
    workoutLogRepository.deleteAll(workouts);

    List<StepRecordEntity> steps =
        stepRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
            userId, LocalDate.MIN, LocalDate.MAX);
    stepRecordRepository.deleteAll(steps);

    List<WeightMeasurementEntity> weights =
        weightRepository.findByUserIdAndMeasurementDateBetweenOrderByMeasurementDateAsc(
            userId, LocalDate.MIN, LocalDate.MAX);
    weightRepository.deleteAll(weights);

    // 2. Delete Pantry & Grocery
    List<PantryItemEntity> pantry = pantryRepository.findByUserId(userId);
    pantryRepository.deleteAll(pantry);

    List<GroceryItemEntity> grocery =
        groceryRepository.findByUserIdOrderByCheckedAscCreatedAtDesc(userId);
    groceryRepository.deleteAll(grocery);

    // 3. Delete Streaks & Badges
    List<UserStreakEntity> streaks = streakRepository.findByUserId(userId);
    streakRepository.deleteAll(streaks);

    List<UserBadgeEntity> badges = badgeRepository.findByUserIdOrderByAwardedAtDesc(userId);
    badgeRepository.deleteAll(badges);

    List<SavedRecipeEntity> saved = savedRecipeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    savedRecipeRepository.deleteAll(saved);

    // 4. Detach from Household safely
    List<HouseholdMemberEntity> memberships = householdMemberRepository.findByUserId(userId);
    for (HouseholdMemberEntity member : memberships) {
      HouseholdEntity household = member.getHousehold();
      householdMemberRepository.delete(member);

      // If user was household owner:
      if (household.getOwner().getId().equals(userId)) {
        List<HouseholdMemberEntity> remaining =
            householdMemberRepository.findByHouseholdId(household.getId());
        if (remaining.isEmpty()) {
          householdRepository.delete(household);
        } else {
          // Promote next remaining member to owner
          household.setOwner(remaining.get(0).getUser());
          householdRepository.save(household);
        }
      }
    }

    // 5. Finally delete UserEntity
    userRepository.delete(user);
    log.info("User {} successfully erased from system.", user.getUsername());
  }
}
