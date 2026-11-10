package com.easynutri.controller.api;

import com.easynutri.dto.ContentReportRequest;
import com.easynutri.dto.ModerationDecisionRequest;
import com.easynutri.dto.ProfessionalVerificationRequest;
import com.easynutri.entity.ContentReportEntity;
import com.easynutri.entity.ModerationActionEntity;
import com.easynutri.entity.ProfessionalVerificationEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.ModerationService;
import com.easynutri.service.ProfessionalVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/moderation")
@Tag(
    name = "Moderation & Verification",
    description =
        "Endpoints for content reporting, clinical safety actions, and pro credential verification")
public class ModerationApiController {

  private final ModerationService moderationService;
  private final ProfessionalVerificationService verificationService;
  private final UserRepository userRepository;

  public ModerationApiController(
      ModerationService moderationService,
      ProfessionalVerificationService verificationService,
      UserRepository userRepository) {
    this.moderationService = moderationService;
    this.verificationService = verificationService;
    this.userRepository = userRepository;
  }

  @PostMapping("/report")
  @Operation(summary = "Report user generated recipe or comment for safety violations")
  public ResponseEntity<Map<String, Object>> submitReport(
      @RequestBody ContentReportRequest request, Principal principal) {
    Long userId = getUserId(principal);
    ContentReportEntity report = moderationService.reportContent(userId, request);
    return ResponseEntity.ok(
        Map.of(
            "reportId",
            report.getId(),
            "status",
            report.getStatus(),
            "verdict",
            report.getAiScreeningVerdict()));
  }

  @GetMapping("/reports")
  @Operation(summary = "List pending content reports (Admin only)")
  public ResponseEntity<List<ContentReportEntity>> getPendingReports() {
    return ResponseEntity.ok(moderationService.getPendingReports());
  }

  @PostMapping("/action")
  @Operation(summary = "Execute moderation action on reported content (Admin only)")
  public ResponseEntity<ModerationActionEntity> takeAction(
      @RequestBody ModerationDecisionRequest request, Principal principal) {
    Long moderatorId = getUserId(principal);
    return ResponseEntity.ok(moderationService.takeAction(moderatorId, request));
  }

  @PostMapping("/verify/apply")
  @Operation(summary = "Submit professional credentials for verification badge")
  public ResponseEntity<Map<String, Object>> applyVerification(
      @RequestBody ProfessionalVerificationRequest request, Principal principal) {
    Long userId = getUserId(principal);
    ProfessionalVerificationEntity app = verificationService.submitApplication(userId, request);
    return ResponseEntity.ok(Map.of("applicationId", app.getId(), "status", app.getStatus()));
  }

  @GetMapping("/verify/pending")
  @Operation(summary = "List pending professional verification applications (Admin only)")
  public ResponseEntity<List<ProfessionalVerificationEntity>> getPendingVerifications() {
    return ResponseEntity.ok(verificationService.getPendingApplications());
  }

  @PostMapping("/verify/review")
  @Operation(summary = "Review and approve/reject professional verification (Admin only)")
  public ResponseEntity<ProfessionalVerificationEntity> reviewVerification(
      @RequestParam("applicationId") Long applicationId,
      @RequestParam("approved") boolean approved,
      @RequestParam(value = "notes", required = false) String notes,
      Principal principal) {
    Long moderatorId = getUserId(principal);
    return ResponseEntity.ok(
        verificationService.reviewApplication(
            moderatorId, applicationId, approved, notes != null ? notes : ""));
  }

  private Long getUserId(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
