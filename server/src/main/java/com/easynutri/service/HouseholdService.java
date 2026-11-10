package com.easynutri.service;

import com.easynutri.domain.calculation.NutritionGoalCalculator;
import com.easynutri.domain.calculation.NutritionGoalCalculator.ActivityLevel;
import com.easynutri.domain.calculation.NutritionGoalCalculator.Goal;
import com.easynutri.domain.household.HouseholdDietaryAggregator.MemberDietaryProfile;
import com.easynutri.dto.AddHouseholdMemberRequest;
import com.easynutri.dto.HouseholdDto;
import com.easynutri.dto.HouseholdMemberDto;
import com.easynutri.entity.*;
import com.easynutri.repository.*;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HouseholdService {

  private final HouseholdRepository householdRepository;
  private final HouseholdMemberRepository memberRepository;
  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;
  private final PasswordEncoder passwordEncoder;

  public HouseholdService(
      HouseholdRepository householdRepository,
      HouseholdMemberRepository memberRepository,
      UserRepository userRepository,
      UserProfileRepository userProfileRepository,
      PasswordEncoder passwordEncoder) {
    this.householdRepository = householdRepository;
    this.memberRepository = memberRepository;
    this.userRepository = userRepository;
    this.userProfileRepository = userProfileRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public HouseholdDto createHousehold(Long ownerUserId, String name) {
    UserEntity owner =
        userRepository
            .findById(ownerUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + ownerUserId));

    HouseholdEntity household =
        new HouseholdEntity(
            name != null && !name.isBlank() ? name : owner.getFullName() + "'s Family", owner);
    HouseholdEntity savedHousehold = householdRepository.save(household);

    // Automatically add owner as HOUSEHOLD_OWNER
    String initials =
        getInitials(owner.getFullName() != null ? owner.getFullName() : owner.getUsername());
    HouseholdMemberEntity ownerMember =
        new HouseholdMemberEntity(
            savedHousehold,
            owner,
            owner.getFullName() != null ? owner.getFullName() : owner.getUsername(),
            "SELF",
            "HOUSEHOLD_OWNER",
            "#237A57",
            initials,
            null,
            true);
    memberRepository.save(ownerMember);

    return mapToHouseholdDto(savedHousehold, ownerUserId);
  }

  public HouseholdMemberDto addMember(
      Long requesterUserId, Long householdId, AddHouseholdMemberRequest req) {
    HouseholdEntity household =
        householdRepository
            .findById(householdId)
            .orElseThrow(() -> new IllegalArgumentException("Household not found: " + householdId));

    // Verify requester has permission (OWNER or ADMIN)
    HouseholdMemberEntity requesterMember =
        memberRepository
            .findByHouseholdIdAndUserId(householdId, requesterUserId)
            .orElseThrow(() -> new SecurityException("User does not belong to this household"));

    if (!requesterMember.isOwnerOrAdmin()) {
      throw new SecurityException("Insufficient permissions to add members to household");
    }

    // Create managed user account for the family member
    String uniqueUsername =
        "fam_"
            + householdId
            + "_"
            + System.currentTimeMillis()
            + "_"
            + Math.abs(req.displayName().hashCode() % 1000);
    String dummyEmail = uniqueUsername + "@family.easynutri.local";
    UserEntity managedUser =
        new UserEntity(
            uniqueUsername,
            dummyEmail,
            passwordEncoder.encode(UUID.randomUUID().toString()),
            req.displayName());
    managedUser = userRepository.save(managedUser);

    // Create Profile for member
    int age = req.age() > 0 ? req.age() : 25;
    double height = req.heightCm() != null && req.heightCm() > 0 ? req.heightCm() : 165.0;
    double weight = req.weightKg() != null && req.weightKg() > 0 ? req.weightKg() : 65.0;
    String bioSex = req.biologicalSex() != null ? req.biologicalSex().toUpperCase() : "FEMALE";

    UserProfileEntity profile = new UserProfileEntity(managedUser, age, bioSex, height, weight);
    profile.setActivityLevel(req.activityLevel());
    profile.setHealthGoal(req.healthGoal());
    profile.setDietaryPreference(req.dietaryPreference());
    profile.setSensitiveMode(req.sensitiveMode());
    profile.setOnboardingCompleted(true);
    profile.setAllergies(new HashSet<>(req.allergies()));
    profile.setHealthConditions(new HashSet<>(req.healthConditions()));

    calculateTargets(profile);
    userProfileRepository.save(profile);

    // Create Household Membership
    String initials = getInitials(req.displayName());
    boolean isDependent = "DEPENDENT".equalsIgnoreCase(req.role());
    HouseholdMemberEntity member =
        new HouseholdMemberEntity(
            household,
            managedUser,
            req.displayName(),
            req.relationship(),
            req.role(),
            req.avatarColor(),
            initials,
            isDependent ? requesterMember.getUser() : null,
            "HOUSEHOLD_ADMIN".equalsIgnoreCase(req.role()));

    HouseholdMemberEntity savedMember = memberRepository.save(member);
    return mapToMemberDto(savedMember, requesterUserId);
  }

  @Transactional(readOnly = true)
  public Optional<HouseholdDto> getHouseholdForUser(Long userId) {
    List<HouseholdMemberEntity> memberships = memberRepository.findByUserId(userId);
    if (memberships.isEmpty()) {
      return Optional.empty();
    }
    HouseholdEntity household = memberships.get(0).getHousehold();
    return Optional.of(mapToHouseholdDto(household, userId));
  }

  @Transactional(readOnly = true)
  public List<HouseholdMemberDto> getHouseholdMembers(Long householdId, Long requesterUserId) {
    if (!memberRepository.existsByHouseholdIdAndUserId(householdId, requesterUserId)) {
      throw new SecurityException("User does not belong to this household");
    }
    return memberRepository.findByHouseholdId(householdId).stream()
        .map(m -> mapToMemberDto(m, requesterUserId))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<MemberDietaryProfile> getMemberDietaryProfiles(
      Long householdId, List<Long> memberUserIds) {
    List<HouseholdMemberEntity> members = memberRepository.findByHouseholdId(householdId);
    List<MemberDietaryProfile> profiles = new ArrayList<>();

    for (HouseholdMemberEntity m : members) {
      if (memberUserIds == null
          || memberUserIds.isEmpty()
          || memberUserIds.contains(m.getUser().getId())) {
        var profOpt = userProfileRepository.findByUserId(m.getUser().getId());
        if (profOpt.isPresent()) {
          UserProfileEntity p = profOpt.get();
          profiles.add(
              new MemberDietaryProfile(
                  m.getUser().getId(),
                  m.getDisplayName(),
                  p.getAge(),
                  p.getDietaryPreference(),
                  p.getAllergies(),
                  p.getHealthConditions(),
                  p.getTargetCalories() != null ? p.getTargetCalories() : 2000));
        } else {
          profiles.add(
              new MemberDietaryProfile(
                  m.getUser().getId(),
                  m.getDisplayName(),
                  30,
                  "NON_VEG",
                  Set.of(),
                  Set.of(),
                  2000));
        }
      }
    }

    return profiles;
  }

  private void calculateTargets(UserProfileEntity profile) {
    boolean isMale = "MALE".equalsIgnoreCase(profile.getBiologicalSex());
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

  private HouseholdDto mapToHouseholdDto(HouseholdEntity h, Long currentUserId) {
    List<HouseholdMemberDto> memberDtos =
        memberRepository.findByHouseholdId(h.getId()).stream()
            .map(m -> mapToMemberDto(m, currentUserId))
            .toList();

    return new HouseholdDto(
        h.getId(),
        h.getName(),
        h.getOwner().getId(),
        h.getOwner().getUsername(),
        memberDtos.size(),
        memberDtos);
  }

  public HouseholdMemberDto mapToMemberDto(HouseholdMemberEntity m, Long currentUserId) {
    UserProfileEntity p = userProfileRepository.findByUserId(m.getUser().getId()).orElse(null);
    boolean sensitive = p != null && p.isSensitiveMode();
    int age = p != null ? p.getAge() : 0;
    String bioSex = p != null ? p.getBiologicalSex() : "UNSPECIFIED";
    String diet = p != null ? p.getDietaryPreference() : "NON_VEG";
    List<String> allergies =
        p != null && p.getAllergies() != null ? List.copyOf(p.getAllergies()) : List.of();
    List<String> conditions =
        p != null && p.getHealthConditions() != null
            ? List.copyOf(p.getHealthConditions())
            : List.of();
    Integer targetCal = p != null ? p.getTargetCalories() : 2000;

    boolean isCurrent = currentUserId != null && currentUserId.equals(m.getUser().getId());

    return new HouseholdMemberDto(
        m.getId(),
        m.getHousehold().getId(),
        m.getUser().getId(),
        m.getDisplayName(),
        m.getRelationship(),
        m.getRole(),
        m.getAvatarColor(),
        m.getAvatarInitials(),
        age,
        bioSex,
        diet,
        allergies,
        conditions,
        sensitive,
        targetCal,
        m.isDependent(),
        m.isCanManageHousehold(),
        isCurrent);
  }

  private String getInitials(String name) {
    if (name == null || name.isBlank()) return "FM";
    String[] parts = name.trim().split("\\s+");
    if (parts.length == 1) {
      return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
    }
    return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
  }
}
