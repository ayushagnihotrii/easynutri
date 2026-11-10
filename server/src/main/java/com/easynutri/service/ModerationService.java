package com.easynutri.service;

import com.easynutri.domain.moderation.ContentScreener;
import com.easynutri.domain.moderation.ScreeningResult;
import com.easynutri.dto.ContentReportRequest;
import com.easynutri.dto.ModerationDecisionRequest;
import com.easynutri.entity.*;
import com.easynutri.repository.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ModerationService {

  private final ContentReportRepository reportRepository;
  private final ModerationActionRepository actionRepository;
  private final CommunityRecipeRepository recipeRepository;
  private final CommunityCommentRepository commentRepository;
  private final UserRepository userRepository;

  public ModerationService(
      ContentReportRepository reportRepository,
      ModerationActionRepository actionRepository,
      CommunityRecipeRepository recipeRepository,
      CommunityCommentRepository commentRepository,
      UserRepository userRepository) {
    this.reportRepository = reportRepository;
    this.actionRepository = actionRepository;
    this.recipeRepository = recipeRepository;
    this.commentRepository = commentRepository;
    this.userRepository = userRepository;
  }

  public ContentReportEntity reportContent(Long reporterId, ContentReportRequest req) {
    UserEntity reporter =
        userRepository
            .findById(reporterId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + reporterId));

    if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
        reporterId, req.targetType(), req.targetId())) {
      throw new IllegalArgumentException("You have already submitted a report for this item.");
    }

    ScreeningResult screenResult = ContentScreener.screen(req.reasonCategory(), req.description());

    ContentReportEntity report =
        new ContentReportEntity(
            reporter,
            req.targetType(),
            req.targetId(),
            req.reasonCategory(),
            req.description(),
            screenResult.verdict().name(),
            String.join("; ", screenResult.reasons()));

    return reportRepository.save(report);
  }

  @Transactional(readOnly = true)
  public List<ContentReportEntity> getPendingReports() {
    return reportRepository.findByStatusOrderByCreatedAtDesc("PENDING");
  }

  @Transactional(readOnly = true)
  public List<ModerationActionEntity> getAuditHistory() {
    return actionRepository.findAllByOrderByCreatedAtDesc();
  }

  public ModerationActionEntity takeAction(Long moderatorId, ModerationDecisionRequest req) {
    UserEntity moderator =
        userRepository
            .findById(moderatorId)
            .orElseThrow(() -> new IllegalArgumentException("Moderator not found: " + moderatorId));

    String action = req.actionTaken() != null ? req.actionTaken().toUpperCase() : "APPROVE";
    String targetType = req.targetType();
    Long targetId = req.targetId();

    if ("RECIPE".equalsIgnoreCase(targetType)) {
      recipeRepository
          .findById(targetId)
          .ifPresent(
              recipe -> {
                switch (action) {
                  case "APPROVE" -> recipe.setStatus("APPROVED");
                  case "REJECT" -> recipe.setStatus("REJECTED");
                  case "HIDE" -> recipe.setStatus("HIDDEN");
                  case "RESTORE" -> recipe.setStatus("APPROVED");
                }
                recipeRepository.save(recipe);
              });
    } else if ("COMMENT".equalsIgnoreCase(targetType)) {
      commentRepository
          .findById(targetId)
          .ifPresent(
              comment -> {
                switch (action) {
                  case "REMOVE" -> comment.setStatus("REMOVED");
                  case "APPROVE" -> comment.setStatus("APPROVED");
                }
                commentRepository.save(comment);
              });
    } else if ("USER".equalsIgnoreCase(targetType)) {
      userRepository
          .findById(targetId)
          .ifPresent(
              user -> {
                if ("SUSPEND_USER".equalsIgnoreCase(action)) {
                  user.setEnabled(false);
                  userRepository.save(user);
                }
              });
    }

    // Mark pending reports as actioned
    List<ContentReportEntity> reports =
        reportRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    for (ContentReportEntity r : reports) {
      if (r.getTargetType().equalsIgnoreCase(targetType) && r.getTargetId().equals(targetId)) {
        r.setStatus("RESOLVED_ACTIONED");
        reportRepository.save(r);
      }
    }

    ModerationActionEntity actionRecord =
        new ModerationActionEntity(moderator, targetType, targetId, action, req.moderatorNotes());
    return actionRepository.save(actionRecord);
  }
}
