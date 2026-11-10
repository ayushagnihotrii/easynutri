package com.easynutri.service;

import com.easynutri.domain.gamification.StreakCalculationResult;
import com.easynutri.domain.gamification.StreakCalculator;
import com.easynutri.dto.BadgeDto;
import com.easynutri.dto.ChallengeDto;
import com.easynutri.dto.UserStreakDto;
import com.easynutri.entity.*;
import com.easynutri.repository.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GamificationService {

  private final UserStreakRepository streakRepository;
  private final BadgeDefinitionRepository badgeDefinitionRepository;
  private final UserBadgeRepository userBadgeRepository;
  private final ChallengeRepository challengeRepository;
  private final ChallengeParticipationRepository participationRepository;
  private final UserRepository userRepository;
  private final FoodDiaryEntryRepository diaryEntryRepository;
  private final WaterLogRepository waterLogRepository;
  private final StepRecordRepository stepRecordRepository;
  private final CookingSessionRepository cookingSessionRepository;
  private final CommunityRecipeRepository communityRecipeRepository;

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MMM dd, yyyy").withZone(ZoneId.systemDefault());

  public GamificationService(
      UserStreakRepository streakRepository,
      BadgeDefinitionRepository badgeDefinitionRepository,
      UserBadgeRepository userBadgeRepository,
      ChallengeRepository challengeRepository,
      ChallengeParticipationRepository participationRepository,
      UserRepository userRepository,
      FoodDiaryEntryRepository diaryEntryRepository,
      WaterLogRepository waterLogRepository,
      StepRecordRepository stepRecordRepository,
      CookingSessionRepository cookingSessionRepository,
      CommunityRecipeRepository communityRecipeRepository) {
    this.streakRepository = streakRepository;
    this.badgeDefinitionRepository = badgeDefinitionRepository;
    this.userBadgeRepository = userBadgeRepository;
    this.challengeRepository = challengeRepository;
    this.participationRepository = participationRepository;
    this.userRepository = userRepository;
    this.diaryEntryRepository = diaryEntryRepository;
    this.waterLogRepository = waterLogRepository;
    this.stepRecordRepository = stepRecordRepository;
    this.cookingSessionRepository = cookingSessionRepository;
    this.communityRecipeRepository = communityRecipeRepository;
  }

  public List<UserStreakDto> getUserStreaks(Long userId) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    LocalDate today = LocalDate.now();
    List<UserStreakDto> dtos = new ArrayList<>();

    // 1. Food Diary Logging Streak
    List<LocalDate> diaryDates = diaryEntryRepository.findDistinctLoggedDates(userId);
    StreakCalculationResult diaryStreak = StreakCalculator.computeStreak(diaryDates, today);
    saveOrUpdateStreak(user, "DIARY_LOGGING", diaryStreak, today);
    dtos.add(
        new UserStreakDto(
            "DIARY_LOGGING",
            "Food Diary Streak",
            "🥗",
            diaryStreak.currentStreak(),
            diaryStreak.longestStreak(),
            today,
            diaryStreak.loggedToday(),
            diaryStreak.gracePeriodActive()));

    // 2. Hydration Streak
    List<WaterLogEntity> waterLogs =
        waterLogRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAscLoggedAtAsc(
            userId, today.minusDays(60), today);
    Set<LocalDate> waterDates =
        waterLogs.stream().map(WaterLogEntity::getRecordDate).collect(Collectors.toSet());
    StreakCalculationResult hydrationStreak = StreakCalculator.computeStreak(waterDates, today);
    saveOrUpdateStreak(user, "HYDRATION", hydrationStreak, today);
    dtos.add(
        new UserStreakDto(
            "HYDRATION",
            "Hydration Streak",
            "💧",
            hydrationStreak.currentStreak(),
            hydrationStreak.longestStreak(),
            today,
            hydrationStreak.loggedToday(),
            hydrationStreak.gracePeriodActive()));

    // 3. Step Goal Streak
    List<StepRecordEntity> stepRecords =
        stepRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
            userId, today.minusDays(60), today);
    Set<LocalDate> stepDates =
        stepRecords.stream()
            .filter(s -> s.getStepCount() >= s.getGoalSteps())
            .map(StepRecordEntity::getRecordDate)
            .collect(Collectors.toSet());
    StreakCalculationResult stepStreak = StreakCalculator.computeStreak(stepDates, today);
    saveOrUpdateStreak(user, "STEP_GOAL", stepStreak, today);
    dtos.add(
        new UserStreakDto(
            "STEP_GOAL",
            "Step Target Streak",
            "🚶",
            stepStreak.currentStreak(),
            stepStreak.longestStreak(),
            today,
            stepStreak.loggedToday(),
            stepStreak.gracePeriodActive()));

    // 4. Home Cooking Streak
    List<CookingSessionEntity> sessions =
        cookingSessionRepository.findByUserIdOrderByStartedAtDesc(userId);
    Set<LocalDate> cookingDates =
        sessions.stream()
            .filter(s -> s.getCompletedAt() != null)
            .map(s -> s.getStartedAt().atZone(ZoneId.systemDefault()).toLocalDate())
            .collect(Collectors.toSet());
    StreakCalculationResult cookingStreak = StreakCalculator.computeStreak(cookingDates, today);
    saveOrUpdateStreak(user, "COOKING", cookingStreak, today);
    dtos.add(
        new UserStreakDto(
            "COOKING",
            "Home Cooking Streak",
            "🍳",
            cookingStreak.currentStreak(),
            cookingStreak.longestStreak(),
            today,
            cookingStreak.loggedToday(),
            cookingStreak.gracePeriodActive()));

    return dtos;
  }

  public List<BadgeDto> getUserBadges(Long userId) {
    ensureInitialData();
    checkAndAwardBadges(userId);

    Map<String, UserBadgeEntity> awardedMap =
        userBadgeRepository.findByUserIdOrderByAwardedAtDesc(userId).stream()
            .collect(Collectors.toMap(b -> b.getBadge().getId(), b -> b, (a, b) -> a));

    return badgeDefinitionRepository.findAll().stream()
        .map(
            def -> {
              boolean unlocked = awardedMap.containsKey(def.getId());
              UserBadgeEntity ub = awardedMap.get(def.getId());
              return new BadgeDto(
                  def.getId(),
                  def.getName(),
                  def.getDescription(),
                  def.getIcon(),
                  def.getCategory(),
                  unlocked,
                  unlocked ? ub.getAwardedAt() : null,
                  unlocked ? DATE_FORMATTER.format(ub.getAwardedAt()) : "Not unlocked yet");
            })
        .toList();
  }

  public void checkAndAwardBadges(Long userId) {
    ensureInitialData();
    UserEntity user = userRepository.findById(userId).orElseThrow();

    // 1. FIRST_MEAL_LOGGED
    if (!userBadgeRepository.existsByUserIdAndBadgeId(userId, "FIRST_MEAL_LOGGED")) {
      if (!diaryEntryRepository.findDistinctLoggedDates(userId).isEmpty()) {
        awardBadge(user, "FIRST_MEAL_LOGGED");
      }
    }

    // 2. SEVEN_DAY_DIARY
    if (!userBadgeRepository.existsByUserIdAndBadgeId(userId, "SEVEN_DAY_DIARY")) {
      List<LocalDate> diaryDates = diaryEntryRepository.findDistinctLoggedDates(userId);
      StreakCalculationResult s = StreakCalculator.computeStreak(diaryDates, LocalDate.now());
      if (s.currentStreak() >= 7 || s.longestStreak() >= 7) {
        awardBadge(user, "SEVEN_DAY_DIARY");
      }
    }

    // 3. HYDRATION_HERO
    if (!userBadgeRepository.existsByUserIdAndBadgeId(userId, "HYDRATION_HERO")) {
      List<WaterLogEntity> logs =
          waterLogRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAscLoggedAtAsc(
              userId, LocalDate.now().minusDays(30), LocalDate.now());
      Set<LocalDate> dates =
          logs.stream().map(WaterLogEntity::getRecordDate).collect(Collectors.toSet());
      StreakCalculationResult s = StreakCalculator.computeStreak(dates, LocalDate.now());
      if (s.currentStreak() >= 7 || s.longestStreak() >= 7) {
        awardBadge(user, "HYDRATION_HERO");
      }
    }

    // 4. STEP_EXPLORER
    if (!userBadgeRepository.existsByUserIdAndBadgeId(userId, "STEP_EXPLORER")) {
      List<StepRecordEntity> stepRecords =
          stepRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
              userId, LocalDate.now().minusDays(60), LocalDate.now());
      long metGoalDays =
          stepRecords.stream().filter(r -> r.getStepCount() >= r.getGoalSteps()).count();
      if (metGoalDays >= 5) {
        awardBadge(user, "STEP_EXPLORER");
      }
    }

    // 5. HOME_CHEF
    if (!userBadgeRepository.existsByUserIdAndBadgeId(userId, "HOME_CHEF")) {
      long cookedCount =
          cookingSessionRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
              .filter(s -> s.getCompletedAt() != null)
              .count();
      if (cookedCount >= 3) {
        awardBadge(user, "HOME_CHEF");
      }
    }

    // 6. COMMUNITY_CREATOR
    if (!userBadgeRepository.existsByUserIdAndBadgeId(userId, "COMMUNITY_CREATOR")) {
      long approvedCommunity =
          communityRecipeRepository.countByAuthorIdAndStatus(userId, "APPROVED");
      if (approvedCommunity >= 1) {
        awardBadge(user, "COMMUNITY_CREATOR");
      }
    }
  }

  public List<ChallengeDto> getChallenges(Long userId) {
    ensureInitialData();
    List<ChallengeEntity> allChallenges = challengeRepository.findByActiveTrue();
    Map<String, ChallengeParticipationEntity> userParts =
        participationRepository.findByUserIdOrderByJoinedAtDesc(userId).stream()
            .collect(Collectors.toMap(p -> p.getChallenge().getId(), p -> p, (a, b) -> a));

    return allChallenges.stream()
        .map(
            c -> {
              boolean joined = userParts.containsKey(c.getId());
              ChallengeParticipationEntity part = userParts.get(c.getId());
              int prog = joined ? part.getProgress() : 0;
              boolean comp = joined && part.isCompleted();
              int percent =
                  c.getTargetCount() > 0
                      ? Math.min(100, (int) Math.round((prog * 100.0) / c.getTargetCount()))
                      : 0;

              long participants = participationRepository.countByChallengeId(c.getId());

              return new ChallengeDto(
                  c.getId(),
                  c.getTitle(),
                  c.getDescription(),
                  c.getIcon(),
                  c.getCategory(),
                  c.getTargetCount(),
                  c.getDurationDays(),
                  prog,
                  percent,
                  joined,
                  comp,
                  joined ? part.getCompletedAt() : null,
                  participants);
            })
        .toList();
  }

  public ChallengeParticipationEntity joinChallenge(Long userId, String challengeId) {
    ensureInitialData();
    UserEntity user = userRepository.findById(userId).orElseThrow();
    ChallengeEntity challenge = challengeRepository.findById(challengeId).orElseThrow();

    Optional<ChallengeParticipationEntity> existing =
        participationRepository.findByUserIdAndChallengeId(userId, challengeId);
    if (existing.isPresent()) {
      return existing.get();
    }

    ChallengeParticipationEntity part = new ChallengeParticipationEntity(user, challenge);
    return participationRepository.save(part);
  }

  public void updateChallengeProgress(Long userId, String category, int increment) {
    List<ChallengeParticipationEntity> participations =
        participationRepository.findByUserIdOrderByJoinedAtDesc(userId);

    for (ChallengeParticipationEntity p : participations) {
      if (!p.isCompleted() && p.getChallenge().getCategory().equalsIgnoreCase(category)) {
        int newProgress = p.getProgress() + increment;
        p.setProgress(newProgress);
        if (newProgress >= p.getChallenge().getTargetCount()) {
          p.setCompleted(true);
        }
        participationRepository.save(p);
      }
    }
  }

  private void saveOrUpdateStreak(
      UserEntity user, String type, StreakCalculationResult res, LocalDate today) {
    Optional<UserStreakEntity> opt = streakRepository.findByUserIdAndStreakType(user.getId(), type);
    UserStreakEntity entity;
    if (opt.isPresent()) {
      entity = opt.get();
      entity.setCurrentStreakDays(res.currentStreak());
      entity.setLongestStreakDays(res.longestStreak());
      entity.setLastActivityDate(today);
    } else {
      entity = new UserStreakEntity(user, type, res.currentStreak(), res.longestStreak(), today);
    }
    streakRepository.save(entity);
  }

  private void awardBadge(UserEntity user, String badgeId) {
    badgeDefinitionRepository
        .findById(badgeId)
        .ifPresent(
            def -> {
              UserBadgeEntity ub = new UserBadgeEntity(user, def);
              userBadgeRepository.save(ub);
            });
  }

  private void ensureInitialData() {
    if (badgeDefinitionRepository.count() == 0) {
      badgeDefinitionRepository.saveAll(
          List.of(
              new BadgeDefinitionEntity(
                  "FIRST_MEAL_LOGGED",
                  "First Meal Logged",
                  "Started your journey by logging your first meal in the food diary.",
                  "🥗",
                  "NUTRITION",
                  1),
              new BadgeDefinitionEntity(
                  "SEVEN_DAY_DIARY",
                  "7-Day Diary Habit",
                  "Logged meals for 7 consecutive days in your food diary.",
                  "📅",
                  "NUTRITION",
                  7),
              new BadgeDefinitionEntity(
                  "HYDRATION_HERO",
                  "Hydration Hero",
                  "Met your daily fluid intake goal 7 days in a row.",
                  "💧",
                  "HYDRATION",
                  7),
              new BadgeDefinitionEntity(
                  "STEP_EXPLORER",
                  "Walking Vitality",
                  "Reached your daily step target on 5 distinct days.",
                  "🚶",
                  "ACTIVITY",
                  5),
              new BadgeDefinitionEntity(
                  "HOME_CHEF",
                  "Home Chef",
                  "Prepared 3 home-cooked meals using EasyNutri interactive cooking mode.",
                  "🍳",
                  "COOKING",
                  3),
              new BadgeDefinitionEntity(
                  "ZERO_PANTRY_WASTE",
                  "Zero Food Waste",
                  "Cooked meals utilizing pantry items approaching expiration.",
                  "🥫",
                  "COOKING",
                  3),
              new BadgeDefinitionEntity(
                  "COMMUNITY_CREATOR",
                  "Community Contributor",
                  "Shared your first original healthy recipe with the EasyNutri community.",
                  "🌱",
                  "COMMUNITY",
                  1)));
    }

    if (challengeRepository.count() == 0) {
      challengeRepository.saveAll(
          List.of(
              new ChallengeEntity(
                  "BREAKFAST_HABIT_7D",
                  "7-Day Nourishing Breakfast",
                  "Start every morning with a balanced, nutrient-rich breakfast for 7 days.",
                  "🥞",
                  "NUTRITION",
                  7,
                  7,
                  true),
              new ChallengeEntity(
                  "HOME_COOKING_5M",
                  "5 Home-Cooked Meals Challenge",
                  "Prepare at least 5 wholesome meals at home using whole ingredients.",
                  "🍳",
                  "COOKING",
                  5,
                  7,
                  true),
              new ChallengeEntity(
                  "PANTRY_WASTE_REDUCTION",
                  "Pantry Waste Saver",
                  "Cook 3 meals that make use of ingredients in your pantry nearing expiration.",
                  "🥫",
                  "PANTRY",
                  3,
                  7,
                  true),
              new ChallengeEntity(
                  "HYDRATION_CONSISTENCY_7D",
                  "7-Day Hydration Habit",
                  "Consistently hit your personalized fluid targets every day for a week.",
                  "💧",
                  "HABIT",
                  7,
                  7,
                  true)));
    }
  }
}
