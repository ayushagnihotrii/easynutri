package com.easynutri.service;

import com.easynutri.domain.calculation.NutritionGoalCalculator;
import com.easynutri.domain.calculation.NutritionGoalCalculator.ActivityLevel;
import com.easynutri.domain.calculation.NutritionGoalCalculator.Goal;
import com.easynutri.domain.model.ProfileContext;
import com.easynutri.dto.OnboardingRequest;
import com.easynutri.dto.ProfileUpdateRequest;
import com.easynutri.entity.UserEntity;
import com.easynutri.entity.UserProfileEntity;
import com.easynutri.repository.UserProfileRepository;
import com.easynutri.repository.UserRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

  private final UserProfileRepository profileRepository;
  private final UserRepository userRepository;

  public UserProfileService(
      UserProfileRepository profileRepository, UserRepository userRepository) {
    this.profileRepository = profileRepository;
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public Optional<UserProfileEntity> findByUsername(String username) {
    return profileRepository.findByUserUsername(username);
  }

  @Transactional(readOnly = true)
  public boolean hasCompletedOnboarding(String username) {
    return profileRepository
        .findByUserUsername(username)
        .map(UserProfileEntity::isOnboardingCompleted)
        .orElse(false);
  }

  @Transactional
  public UserProfileEntity saveOnboardingProfile(String username, OnboardingRequest request) {
    UserEntity user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

    UserProfileEntity profile =
        profileRepository
            .findByUserUsername(username)
            .orElse(
                new UserProfileEntity(
                    user,
                    request.age(),
                    request.biologicalSex(),
                    request.heightCm(),
                    request.weightKg()));

    profile.setAge(request.age());
    profile.setBiologicalSex(request.biologicalSex());
    profile.setHeightCm(request.heightCm());
    profile.setWeightKg(request.weightKg());
    profile.setActivityLevel(request.activityLevel());
    profile.setHealthGoal(request.healthGoal());
    profile.setDietaryPreference(request.dietaryPreference());
    profile.setPregnant(request.isPregnant());
    profile.setSensitiveMode(request.sensitiveMode());
    profile.setAllergies(new HashSet<>(request.allergies()));
    profile.setHealthConditions(new HashSet<>(request.healthConditions()));
    profile.setOnboardingCompleted(true);
    profile.setUpdatedAt(Instant.now());

    applyCalculatedTargets(profile);
    return profileRepository.save(profile);
  }

  @Transactional
  public UserProfileEntity updateProfile(String username, ProfileUpdateRequest request) {
    UserProfileEntity profile =
        profileRepository
            .findByUserUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for: " + username));

    profile.setAge(request.age());
    profile.setBiologicalSex(request.biologicalSex());
    profile.setHeightCm(request.heightCm());
    profile.setWeightKg(request.weightKg());
    profile.setActivityLevel(request.activityLevel());
    profile.setHealthGoal(request.healthGoal());
    profile.setDietaryPreference(request.dietaryPreference());
    profile.setPregnant(request.isPregnant());
    profile.setSensitiveMode(request.sensitiveMode());
    profile.setAllergies(new HashSet<>(request.allergies()));
    profile.setHealthConditions(new HashSet<>(request.healthConditions()));
    profile.setUpdatedAt(Instant.now());

    applyCalculatedTargets(profile);
    return profileRepository.save(profile);
  }

  public ProfileContext toProfileContext(UserProfileEntity entity) {
    return new ProfileContext(
        String.valueOf(entity.getUser().getId()),
        entity.getAge(),
        entity.getBiologicalSex(),
        entity.getHeightCm(),
        entity.getWeightKg(),
        entity.getDietaryPreference(),
        List.copyOf(entity.getAllergies()),
        List.copyOf(entity.getHealthConditions()));
  }

  private void applyCalculatedTargets(UserProfileEntity profile) {
    boolean isMale = !"FEMALE".equalsIgnoreCase(profile.getBiologicalSex());
    ActivityLevel activity;
    try {
      activity = ActivityLevel.valueOf(profile.getActivityLevel().toUpperCase());
    } catch (Exception e) {
      activity = ActivityLevel.MODERATE;
    }

    Goal goal;
    try {
      goal = Goal.valueOf(profile.getHealthGoal().toUpperCase());
    } catch (Exception e) {
      goal = Goal.MAINTAIN;
    }

    var targets =
        NutritionGoalCalculator.calculateTargets(
            profile.getAge(),
            isMale,
            profile.getHeightCm(),
            profile.getWeightKg(),
            activity,
            goal,
            profile.isPregnant());

    profile.setTargetCalories(targets.targetCaloriesKcal());
    profile.setTargetProteinG(targets.proteinGrams());
    profile.setTargetCarbG(targets.carbGrams());
    profile.setTargetFatG(targets.fatGrams());
    profile.setTargetWaterL(targets.waterLiters());
  }
}
