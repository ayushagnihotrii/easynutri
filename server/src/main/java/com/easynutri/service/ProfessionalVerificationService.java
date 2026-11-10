package com.easynutri.service;

import com.easynutri.dto.ProfessionalVerificationRequest;
import com.easynutri.entity.ProfessionalVerificationEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.ProfessionalVerificationRepository;
import com.easynutri.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProfessionalVerificationService {

  private final ProfessionalVerificationRepository verificationRepository;
  private final UserRepository userRepository;

  public ProfessionalVerificationService(
      ProfessionalVerificationRepository verificationRepository, UserRepository userRepository) {
    this.verificationRepository = verificationRepository;
    this.userRepository = userRepository;
  }

  public ProfessionalVerificationEntity submitApplication(
      Long userId, ProfessionalVerificationRequest req) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    ProfessionalVerificationEntity application =
        new ProfessionalVerificationEntity(
            user,
            req.professionalCategory(),
            req.credentialsSummary(),
            req.licenseNumber(),
            req.documentFilename());

    return verificationRepository.save(application);
  }

  @Transactional(readOnly = true)
  public List<ProfessionalVerificationEntity> getPendingApplications() {
    return verificationRepository.findByStatusOrderByCreatedAtDesc("PENDING");
  }

  @Transactional(readOnly = true)
  public Optional<ProfessionalVerificationEntity> getLatestApplicationForUser(Long userId) {
    return verificationRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
  }

  public ProfessionalVerificationEntity reviewApplication(
      Long moderatorId, Long applicationId, boolean approve, String notes) {
    UserEntity moderator =
        userRepository
            .findById(moderatorId)
            .orElseThrow(() -> new IllegalArgumentException("Moderator not found: " + moderatorId));

    ProfessionalVerificationEntity application =
        verificationRepository
            .findById(applicationId)
            .orElseThrow(
                () -> new IllegalArgumentException("Application not found: " + applicationId));

    application.setStatus(approve ? "APPROVED" : "REJECTED");
    application.setReviewedBy(moderator);
    application.setReviewerNotes(notes);
    application.setReviewedAt(Instant.now());

    if (approve) {
      UserEntity applicant = application.getUser();
      applicant.setVerifiedPro(true);
      applicant.setProCategory(application.getProfessionalCategory());
      userRepository.save(applicant);
    }

    return verificationRepository.save(application);
  }
}
