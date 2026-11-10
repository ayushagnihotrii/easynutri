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
 * Service managing user privacy controls, complete personal data portability export, and cascading
 * account erasure workflows in accordance with technical privacy principles (GDPR & Indian DPDP
 * Act).
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
  private final MealPlanItemRepository mealPlanItemRepository;
  private final SavedRecipeRepository savedRecipeRepository;
  private final RecipeCollectionRepository recipeCollectionRepository;
  private final RecipeAdaptationRepository recipeAdaptationRepository;
  private final UserStreakRepository streakRepository;
  private final UserBadgeRepository badgeRepository;
  private final ChallengeParticipationRepository challengeParticipationRepository;
  private final HouseholdMemberRepository householdMemberRepository;
  private final HouseholdRepository householdRepository;
  private final HouseholdMealPlanRepository householdMealPlanRepository;
  private final AiConversationRepository aiConversationRepository;
  private final AiMessageRepository aiMessageRepository;
  private final CommunityRecipeRepository communityRecipeRepository;
  private final CommunityCommentRepository communityCommentRepository;
  private final CommunityLikeRepository communityLikeRepository;
  private final CommunityFollowRepository communityFollowRepository;
  private final ContentReportRepository contentReportRepository;
  private final SyncOperationRepository syncOperationRepository;
  private final UserNotificationRepository userNotificationRepository;
  private final HydrationPreferenceRepository hydrationPreferenceRepository;
  private final WeeklyInsightRepository weeklyInsightRepository;
  private final CookingSessionRepository cookingSessionRepository;
  private final ProfessionalVerificationRepository professionalVerificationRepository;
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
      MealPlanItemRepository mealPlanItemRepository,
      SavedRecipeRepository savedRecipeRepository,
      RecipeCollectionRepository recipeCollectionRepository,
      RecipeAdaptationRepository recipeAdaptationRepository,
      UserStreakRepository streakRepository,
      UserBadgeRepository badgeRepository,
      ChallengeParticipationRepository challengeParticipationRepository,
      HouseholdMemberRepository householdMemberRepository,
      HouseholdRepository householdRepository,
      HouseholdMealPlanRepository householdMealPlanRepository,
      AiConversationRepository aiConversationRepository,
      AiMessageRepository aiMessageRepository,
      CommunityRecipeRepository communityRecipeRepository,
      CommunityCommentRepository communityCommentRepository,
      CommunityLikeRepository communityLikeRepository,
      CommunityFollowRepository communityFollowRepository,
      ContentReportRepository contentReportRepository,
      SyncOperationRepository syncOperationRepository,
      UserNotificationRepository userNotificationRepository,
      HydrationPreferenceRepository hydrationPreferenceRepository,
      WeeklyInsightRepository weeklyInsightRepository,
      CookingSessionRepository cookingSessionRepository,
      ProfessionalVerificationRepository professionalVerificationRepository,
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
    this.mealPlanItemRepository = mealPlanItemRepository;
    this.savedRecipeRepository = savedRecipeRepository;
    this.recipeCollectionRepository = recipeCollectionRepository;
    this.recipeAdaptationRepository = recipeAdaptationRepository;
    this.streakRepository = streakRepository;
    this.badgeRepository = badgeRepository;
    this.challengeParticipationRepository = challengeParticipationRepository;
    this.householdMemberRepository = householdMemberRepository;
    this.householdRepository = householdRepository;
    this.householdMealPlanRepository = householdMealPlanRepository;
    this.aiConversationRepository = aiConversationRepository;
    this.aiMessageRepository = aiMessageRepository;
    this.communityRecipeRepository = communityRecipeRepository;
    this.communityCommentRepository = communityCommentRepository;
    this.communityLikeRepository = communityLikeRepository;
    this.communityFollowRepository = communityFollowRepository;
    this.contentReportRepository = contentReportRepository;
    this.syncOperationRepository = syncOperationRepository;
    this.userNotificationRepository = userNotificationRepository;
    this.hydrationPreferenceRepository = hydrationPreferenceRepository;
    this.weeklyInsightRepository = weeklyInsightRepository;
    this.cookingSessionRepository = cookingSessionRepository;
    this.professionalVerificationRepository = professionalVerificationRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> exportUserData(Long userId) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    Map<String, Object> export = new LinkedHashMap<>();

    // 1. Account Metadata (Strictly excludes password, passwordHash, and security tokens)
    Map<String, Object> account = new LinkedHashMap<>();
    account.put("username", user.getUsername());
    account.put("email", user.getEmail());
    account.put("fullName", user.getFullName());
    account.put("role", user.getRole());
    account.put("enabled", user.isEnabled());
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

    // 3. Hydration Preferences
    hydrationPreferenceRepository
        .findByUserId(userId)
        .ifPresent(
            hp -> {
              Map<String, Object> hpm = new LinkedHashMap<>();
              hpm.put("dailyTargetMl", hp.getDailyTargetMl());
              hpm.put("clinicalRestriction", hp.isClinicalRestriction());
              hpm.put("clinicalNotes", hp.getClinicalNotes() != null ? hp.getClinicalNotes() : "");
              hpm.put("reminderEnabled", hp.isReminderEnabled());
              hpm.put("reminderIntervalHours", hp.getReminderIntervalHours());
              hpm.put("startTime", hp.getStartTime());
              hpm.put("endTime", hp.getEndTime());
              export.put("hydrationPreferences", hpm);
            });

    // 4. Nutrition & Food Diary
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

    // 5. Hydration Logs
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

    // 6. Activity & Workouts
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

    // 7. Step Records
    List<StepRecordEntity> steps =
        stepRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
            userId, LocalDate.MIN, LocalDate.MAX);
    List<Map<String, Object>> stepList =
        steps.stream()
            .map(
                s -> {
                  Map<String, Object> sm = new LinkedHashMap<>();
                  sm.put("date", s.getRecordDate().toString());
                  sm.put("stepCount", s.getStepCount());
                  sm.put("goalSteps", s.getGoalSteps());
                  return sm;
                })
            .toList();
    export.put("stepRecords", stepList);

    // 8. Weight History
    List<WeightMeasurementEntity> weights =
        weightRepository.findByUserIdAndMeasurementDateBetweenOrderByMeasurementDateAsc(
            userId, LocalDate.MIN, LocalDate.MAX);
    List<Map<String, Object>> weightList =
        weights.stream()
            .map(
                wm -> {
                  Map<String, Object> wMap = new LinkedHashMap<>();
                  wMap.put("date", wm.getMeasurementDate().toString());
                  wMap.put("weightKg", wm.getWeightKg());
                  wMap.put("notes", wm.getNotes() != null ? wm.getNotes() : "");
                  return wMap;
                })
            .toList();
    export.put("weightHistory", weightList);

    // 9. Pantry Stock
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

    // 10. Grocery Checklist
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

    // 11. Individual Meal Plans
    List<MealPlanItemEntity> mealPlans = mealPlanItemRepository.findByUserId(userId);
    List<Map<String, Object>> mealPlanList =
        mealPlans.stream()
            .map(
                mp -> {
                  Map<String, Object> mpm = new LinkedHashMap<>();
                  mpm.put("plannedDate", mp.getPlannedDate().toString());
                  mpm.put("mealSlot", mp.getMealSlot());
                  mpm.put("recipeTitle", mp.getRecipe() != null ? mp.getRecipe().getTitleEn() : "");
                  mpm.put("servings", mp.getServings());
                  mpm.put("completed", mp.isCompleted());
                  return mpm;
                })
            .toList();
    export.put("mealPlans", mealPlanList);

    // 12. Saved Recipes & Collections
    List<SavedRecipeEntity> savedRecipes =
        savedRecipeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    List<Map<String, Object>> savedList =
        savedRecipes.stream()
            .map(
                sr -> {
                  Map<String, Object> srm = new LinkedHashMap<>();
                  srm.put("recipeTitle", sr.getRecipe() != null ? sr.getRecipe().getTitleEn() : "");
                  srm.put("notes", sr.getNotes() != null ? sr.getNotes() : "");
                  return srm;
                })
            .toList();
    export.put("savedRecipes", savedList);

    List<RecipeCollectionEntity> collections =
        recipeCollectionRepository.findByUserIdOrderByDisplayOrderAsc(userId);
    List<Map<String, Object>> collectionList =
        collections.stream()
            .map(
                col -> {
                  Map<String, Object> cm = new LinkedHashMap<>();
                  cm.put("name", col.getName());
                  cm.put("displayOrder", col.getDisplayOrder());
                  return cm;
                })
            .toList();
    export.put("recipeCollections", collectionList);

    // 13. Recipe Adaptations
    List<RecipeAdaptationEntity> adaptations =
        recipeAdaptationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    List<Map<String, Object>> adaptList =
        adaptations.stream()
            .map(
                ra -> {
                  Map<String, Object> ram = new LinkedHashMap<>();
                  ram.put(
                      "originalRecipeId",
                      ra.getBaseRecipe() != null ? ra.getBaseRecipe().getId() : "");
                  ram.put("adaptationType", ra.getAdaptationType());
                  ram.put("adaptationTitle", ra.getAdaptationTitle());
                  ram.put("instructions", ra.getInstructionsText());
                  ram.put("createdAt", ra.getCreatedAt().toString());
                  return ram;
                })
            .toList();
    export.put("recipeAdaptations", adaptList);

    // 14. Household Memberships & Roles
    List<HouseholdMemberEntity> memberships = householdMemberRepository.findByUserId(userId);
    List<Map<String, Object>> houseList =
        memberships.stream()
            .map(
                hm -> {
                  Map<String, Object> hmm = new LinkedHashMap<>();
                  hmm.put("householdId", hm.getHousehold().getId());
                  hmm.put("householdName", hm.getHousehold().getName());
                  hmm.put("role", hm.getRole());
                  hmm.put("relationship", hm.getRelationship());
                  return hmm;
                })
            .toList();
    export.put("householdMemberships", houseList);

    // 15. Habits, Streaks & Badges
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

    List<UserBadgeEntity> badges = badgeRepository.findByUserIdOrderByAwardedAtDesc(userId);
    List<Map<String, Object>> badgeList =
        badges.stream()
            .map(
                b -> {
                  Map<String, Object> bm = new LinkedHashMap<>();
                  bm.put("badgeId", b.getBadge() != null ? b.getBadge().getId() : "");
                  bm.put("badgeName", b.getBadge() != null ? b.getBadge().getName() : "");
                  bm.put("awardedAt", b.getAwardedAt().toString());
                  return bm;
                })
            .toList();
    export.put("badges", badgeList);

    // 16. Challenge Participations
    List<ChallengeParticipationEntity> challenges =
        challengeParticipationRepository.findByUserIdOrderByJoinedAtDesc(userId);
    List<Map<String, Object>> challengeList =
        challenges.stream()
            .map(
                cp -> {
                  Map<String, Object> cpm = new LinkedHashMap<>();
                  cpm.put(
                      "challengeId", cp.getChallenge() != null ? cp.getChallenge().getId() : "");
                  cpm.put(
                      "challengeTitle",
                      cp.getChallenge() != null ? cp.getChallenge().getTitle() : "");
                  cpm.put("progress", cp.getProgress());
                  cpm.put("completed", cp.isCompleted());
                  cpm.put(
                      "completedAt",
                      cp.getCompletedAt() != null ? cp.getCompletedAt().toString() : "");
                  cpm.put("joinedAt", cp.getJoinedAt().toString());
                  return cpm;
                })
            .toList();
    export.put("challengeParticipations", challengeList);

    // 17. User Notifications
    List<UserNotificationEntity> notifications =
        userNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    List<Map<String, Object>> notifList =
        notifications.stream()
            .map(
                n -> {
                  Map<String, Object> nm = new LinkedHashMap<>();
                  nm.put("title", n.getTitle());
                  nm.put("message", n.getMessage());
                  nm.put("type", n.getType());
                  nm.put("isRead", n.isRead());
                  nm.put("createdAt", n.getCreatedAt().toString());
                  return nm;
                })
            .toList();
    export.put("notifications", notifList);

    // 18. Community Content & Interactions
    Map<String, Object> community = new LinkedHashMap<>();

    List<CommunityRecipeEntity> authoredRecipes =
        communityRecipeRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
    List<Map<String, Object>> recipeList =
        authoredRecipes.stream()
            .map(
                r -> {
                  Map<String, Object> rm = new LinkedHashMap<>();
                  rm.put("title", r.getTitle());
                  rm.put("mealSlot", r.getMealSlot());
                  rm.put("status", r.getStatus());
                  rm.put("likeCount", r.getLikeCount());
                  rm.put("createdAt", r.getCreatedAt().toString());
                  return rm;
                })
            .toList();
    community.put("authoredRecipes", recipeList);

    List<CommunityCommentEntity> authoredComments =
        communityCommentRepository.findByAuthorId(userId);
    List<Map<String, Object>> commentList =
        authoredComments.stream()
            .map(
                c -> {
                  Map<String, Object> cm = new LinkedHashMap<>();
                  cm.put(
                      "recipeTitle",
                      c.getCommunityRecipe() != null ? c.getCommunityRecipe().getTitle() : "");
                  cm.put("content", c.getContent());
                  cm.put("status", c.getStatus());
                  cm.put("createdAt", c.getCreatedAt().toString());
                  return cm;
                })
            .toList();
    community.put("authoredComments", commentList);

    List<CommunityLikeEntity> likes = communityLikeRepository.findByUserId(userId);
    List<Map<String, Object>> likeList =
        likes.stream()
            .map(
                l -> {
                  Map<String, Object> lm = new LinkedHashMap<>();
                  lm.put(
                      "recipeId",
                      l.getCommunityRecipe() != null ? l.getCommunityRecipe().getId() : null);
                  lm.put(
                      "recipeTitle",
                      l.getCommunityRecipe() != null ? l.getCommunityRecipe().getTitle() : "");
                  return lm;
                })
            .toList();
    community.put("likedRecipes", likeList);

    List<CommunityFollowEntity> following = communityFollowRepository.findByFollowerId(userId);
    List<String> followingUsernames =
        following.stream()
            .map(f -> f.getFollowing() != null ? f.getFollowing().getUsername() : "")
            .filter(u -> !u.isBlank())
            .toList();
    community.put("following", followingUsernames);

    List<CommunityFollowEntity> followers = communityFollowRepository.findByFollowingId(userId);
    List<String> followerUsernames =
        followers.stream()
            .map(f -> f.getFollower() != null ? f.getFollower().getUsername() : "")
            .filter(u -> !u.isBlank())
            .toList();
    community.put("followers", followerUsernames);

    export.put("communityActivity", community);

    // 19. Professional Verification Submissions
    List<ProfessionalVerificationEntity> verifications =
        professionalVerificationRepository.findByUserId(userId);
    List<Map<String, Object>> verifList =
        verifications.stream()
            .map(
                v -> {
                  Map<String, Object> vm = new LinkedHashMap<>();
                  vm.put("professionalCategory", v.getProfessionalCategory());
                  vm.put("credentialsSummary", v.getCredentialsSummary());
                  vm.put("licenseNumber", v.getLicenseNumber());
                  vm.put("status", v.getStatus());
                  vm.put("reviewerNotes", v.getReviewerNotes() != null ? v.getReviewerNotes() : "");
                  vm.put(
                      "reviewedAt", v.getReviewedAt() != null ? v.getReviewedAt().toString() : "");
                  vm.put("createdAt", v.getCreatedAt().toString());
                  return vm;
                })
            .toList();
    export.put("professionalVerifications", verifList);

    // 20. AI Chat Conversations (Includes FULL Messages Transcript)
    List<AiConversationEntity> aiConversations =
        aiConversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    List<Map<String, Object>> aiList = new ArrayList<>();
    for (AiConversationEntity conv : aiConversations) {
      Map<String, Object> cm = new LinkedHashMap<>();
      cm.put("title", conv.getTitle());
      cm.put("createdAt", conv.getCreatedAt().toString());
      cm.put("updatedAt", conv.getUpdatedAt().toString());

      List<AiMessageEntity> messages =
          aiMessageRepository.findByConversationIdOrderByCreatedAtAsc(conv.getId());
      List<Map<String, Object>> messageList = new ArrayList<>();
      for (AiMessageEntity m : messages) {
        Map<String, Object> mm = new LinkedHashMap<>();
        mm.put("role", m.getRole());
        mm.put("content", m.getContent());
        mm.put("executedTools", m.getExecutedTools());
        mm.put("safetyVerdict", m.getSafetyVerdict());
        mm.put("createdAt", m.getCreatedAt().toString());
        messageList.add(mm);
      }
      cm.put("messages", messageList);
      aiList.add(cm);
    }
    export.put("aiConversations", aiList);

    // Technical Notice & Disclaimer
    export.put(
        "technicalNotice",
        "Personal data archive generated via automated data portability technical controls. Technical implementation exports user-provided and generated records; formal statutory compliance (DPDP Act 2023 / GDPR) requires independent jurisdictional legal assessment.");
    export.put("nutritionDataSourceAttribution", "ICMR-NIN IFCT 2017 & USDA FoodData Central.");
    export.put("exportTimestamp", Instant.now().toString());
    export.put("formatVersion", "EASYNUTRI_EXPORT_V2");

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
    diaryEntryRepository.deleteAll(diaryEntryRepository.findByUserId(userId));
    waterLogRepository.deleteAll(waterLogRepository.findByUserIdOrderByLoggedAtDesc(userId));
    workoutLogRepository.deleteAll(
        workoutLogRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
            userId, LocalDate.MIN, LocalDate.MAX));
    stepRecordRepository.deleteAll(
        stepRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
            userId, LocalDate.MIN, LocalDate.MAX));
    weightRepository.deleteAll(
        weightRepository.findByUserIdAndMeasurementDateBetweenOrderByMeasurementDateAsc(
            userId, LocalDate.MIN, LocalDate.MAX));
    hydrationPreferenceRepository.deleteByUserId(userId);
    weeklyInsightRepository.deleteByUserId(userId);

    // 2. Delete Pantry, Grocery & Food Planning
    pantryRepository.deleteAll(pantryRepository.findByUserId(userId));
    groceryRepository.deleteAll(
        groceryRepository.findByUserIdOrderByCheckedAscCreatedAtDesc(userId));
    mealPlanItemRepository.deleteByUserId(userId);
    cookingSessionRepository.deleteByUserId(userId);
    savedRecipeRepository.deleteAll(savedRecipeRepository.findByUserIdOrderByCreatedAtDesc(userId));
    recipeCollectionRepository.deleteByUserId(userId);
    recipeAdaptationRepository.deleteByUserId(userId);

    // 3. Delete Streaks, Badges & Challenges
    streakRepository.deleteAll(streakRepository.findByUserId(userId));
    badgeRepository.deleteAll(badgeRepository.findByUserIdOrderByAwardedAtDesc(userId));
    challengeParticipationRepository.deleteByUserId(userId);

    // 4. Delete AI Interactions (Cascades AI messages and tool executions)
    List<AiConversationEntity> aiConvs =
        aiConversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    aiConversationRepository.deleteAll(aiConvs);

    // 5. Delete Sync Operations
    syncOperationRepository.deleteByUserId(userId);

    // 6. Delete Social, Moderation & Community Interactions
    communityFollowRepository.deleteByFollowerId(userId);
    communityFollowRepository.deleteByFollowingId(userId);
    communityLikeRepository.deleteByUserId(userId);
    communityCommentRepository.deleteByAuthorId(userId);
    contentReportRepository.deleteByReporterId(userId);
    professionalVerificationRepository.deleteByUserId(userId);

    // Clean up community recipes authored by user
    List<CommunityRecipeEntity> authoredRecipes =
        communityRecipeRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
    for (CommunityRecipeEntity cr : authoredRecipes) {
      communityLikeRepository.deleteByCommunityRecipeId(cr.getId());
      communityCommentRepository.deleteByCommunityRecipeId(cr.getId());
      communityRecipeRepository.delete(cr);
    }

    // 7. Detach from Households safely
    List<HouseholdMemberEntity> memberships = householdMemberRepository.findByUserId(userId);
    for (HouseholdMemberEntity member : memberships) {
      HouseholdEntity household = member.getHousehold();
      householdMemberRepository.delete(member);

      // If user was household owner:
      if (household.getOwner().getId().equals(userId)) {
        List<HouseholdMemberEntity> remaining =
            householdMemberRepository.findByHouseholdId(household.getId());
        if (remaining.isEmpty()) {
          householdMealPlanRepository.deleteByHouseholdId(household.getId());
          householdRepository.delete(household);
        } else {
          // Promote next remaining member to owner
          household.setOwner(remaining.get(0).getUser());
          householdRepository.save(household);
        }
      }
    }

    // 8. Delete User Notifications
    userNotificationRepository.deleteByUserId(userId);

    // 9. Finally delete UserEntity
    userRepository.delete(user);
    log.info("User {} successfully erased from system.", user.getUsername());
  }
}
