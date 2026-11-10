package com.easynutri.service;

import com.easynutri.entity.HouseholdMemberEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.HouseholdMemberRepository;
import com.easynutri.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ActiveProfileContextService {

  public static final String SESSION_ACTIVE_PROFILE_USER_ID = "ACTIVE_PROFILE_USER_ID";

  private final UserRepository userRepository;
  private final HouseholdMemberRepository memberRepository;

  public ActiveProfileContextService(
      UserRepository userRepository, HouseholdMemberRepository memberRepository) {
    this.userRepository = userRepository;
    this.memberRepository = memberRepository;
  }

  /** Resolves the authenticated user ID from principal. */
  public Long getAuthenticatedUserId(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }

  /**
   * Resolves the active profile ID from session with server-side authorization validation. If no
   * session profile exists or access is unauthorized, defaults to authenticated user.
   */
  public Long getActiveProfileId(Principal principal, HttpSession session) {
    Long authUserId = getAuthenticatedUserId(principal);
    if (session == null) {
      return authUserId;
    }

    Object sessionProfileId = session.getAttribute(SESSION_ACTIVE_PROFILE_USER_ID);
    if (sessionProfileId instanceof Long targetProfileId) {
      if (canAccessProfile(authUserId, targetProfileId)) {
        return targetProfileId;
      } else {
        // Reset unauthorized session profile
        session.setAttribute(SESSION_ACTIVE_PROFILE_USER_ID, authUserId);
      }
    }

    return authUserId;
  }

  /** Validates whether an authenticated user is permitted to view or manage a target profile. */
  public boolean canAccessProfile(Long authUserId, Long targetProfileId) {
    if (authUserId.equals(targetProfileId)) {
      return true; // Self access is always permitted
    }

    // Check if both users share a household and authUser has management rights
    List<HouseholdMemberEntity> authMemberships = memberRepository.findByUserId(authUserId);
    for (HouseholdMemberEntity authMem : authMemberships) {
      Long householdId = authMem.getHousehold().getId();
      var targetMemOpt = memberRepository.findByHouseholdIdAndUserId(householdId, targetProfileId);
      if (targetMemOpt.isPresent()) {
        HouseholdMemberEntity targetMem = targetMemOpt.get();
        // Authorized adult managing dependent or owner/admin of household
        if (targetMem.isDependent() && authMem.isOwnerOrAdmin()) {
          return true;
        }
        if (targetMem.getManagedBy() != null
            && targetMem.getManagedBy().getId().equals(authUserId)) {
          return true;
        }
        if (authMem.isOwnerOrAdmin()) {
          return true;
        }
      }
    }

    return false;
  }

  /** Switches active profile in session after strict server-side ownership verification. */
  public void switchActiveProfile(Principal principal, HttpSession session, Long targetProfileId) {
    Long authUserId = getAuthenticatedUserId(principal);
    if (!canAccessProfile(authUserId, targetProfileId)) {
      throw new SecurityException("Unauthorized access to profile #" + targetProfileId);
    }
    if (session != null) {
      session.setAttribute(SESSION_ACTIVE_PROFILE_USER_ID, targetProfileId);
    }
  }
}
