package com.easynutri.controller.web;

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
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ModerationWebController {

  private final ModerationService moderationService;
  private final ProfessionalVerificationService verificationService;
  private final UserRepository userRepository;

  public ModerationWebController(
      ModerationService moderationService,
      ProfessionalVerificationService verificationService,
      UserRepository userRepository) {
    this.moderationService = moderationService;
    this.verificationService = verificationService;
    this.userRepository = userRepository;
  }

  @GetMapping("/admin/moderation")
  public String moderationDashboard(Model model, Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    List<ContentReportEntity> reports = moderationService.getPendingReports();
    List<ProfessionalVerificationEntity> verifications =
        verificationService.getPendingApplications();
    List<ModerationActionEntity> auditLog = moderationService.getAuditHistory();

    model.addAttribute("reports", reports);
    model.addAttribute("verifications", verifications);
    model.addAttribute("auditLog", auditLog);
    model.addAttribute("username", username);
    model.addAttribute("currentUserId", currentUserId);
    model.addAttribute("activeNav", "admin");
    model.addAttribute("pageTitle", "Clinical & Community Moderation Console");

    return "admin/moderation";
  }

  @PostMapping("/admin/moderation/action")
  public String takeAction(
      @RequestParam("targetType") String targetType,
      @RequestParam("targetId") Long targetId,
      @RequestParam("actionTaken") String actionTaken,
      @RequestParam(value = "moderatorNotes", required = false) String notes,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    try {
      moderationService.takeAction(
          currentUserId,
          new ModerationDecisionRequest(
              targetType,
              targetId,
              actionTaken,
              notes != null ? notes : "Action applied by admin"));
      redirectAttributes.addFlashAttribute(
          "flashMessage",
          "Moderation decision applied: " + actionTaken + " on " + targetType + " #" + targetId);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Failed to apply moderation action: " + e.getMessage());
    }

    return "redirect:/admin/moderation";
  }

  @PostMapping("/admin/moderation/verify")
  public String reviewVerification(
      @RequestParam("applicationId") Long applicationId,
      @RequestParam("approved") boolean approved,
      @RequestParam(value = "notes", required = false) String notes,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    try {
      verificationService.reviewApplication(
          currentUserId, applicationId, approved, notes != null ? notes : "Review completed");
      redirectAttributes.addFlashAttribute(
          "flashMessage",
          "Verification application "
              + (approved ? "APPROVED — Pro badge granted" : "REJECTED")
              + ".");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Failed to review verification: " + e.getMessage());
    }

    return "redirect:/admin/moderation";
  }

  @PostMapping("/community/report")
  public String reportContent(
      @RequestParam("targetType") String targetType,
      @RequestParam("targetId") Long targetId,
      @RequestParam("reasonCategory") String reasonCategory,
      @RequestParam(value = "description", required = false) String description,
      @RequestParam(value = "returnUrl", required = false) String returnUrl,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    try {
      moderationService.reportContent(
          currentUserId,
          new ContentReportRequest(
              targetType, targetId, reasonCategory, description != null ? description : ""));
      redirectAttributes.addFlashAttribute(
          "flashMessage", "Thank you. Your report has been submitted to clinical moderation.");
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("flashError", e.getMessage());
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Could not submit report: " + e.getMessage());
    }

    if (returnUrl != null && !returnUrl.isBlank()) {
      return "redirect:" + returnUrl;
    }
    return "redirect:/community";
  }

  @PostMapping("/community/verify/apply")
  public String applyForVerification(
      @RequestParam("professionalCategory") String professionalCategory,
      @RequestParam("credentialsSummary") String credentialsSummary,
      @RequestParam("licenseNumber") String licenseNumber,
      @RequestParam(value = "documentFilename", required = false) String documentFilename,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long currentUserId = getUserId(username);

    try {
      verificationService.submitApplication(
          currentUserId,
          new ProfessionalVerificationRequest(
              professionalCategory,
              credentialsSummary,
              licenseNumber,
              documentFilename != null ? documentFilename : "license_doc.pdf"));
      redirectAttributes.addFlashAttribute(
          "flashMessage",
          "Professional credential verification submitted. Our administrative team will review your credentials.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Failed to submit verification application: " + e.getMessage());
    }

    return "redirect:/community/users/" + username;
  }

  private Long getUserId(String username) {
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
