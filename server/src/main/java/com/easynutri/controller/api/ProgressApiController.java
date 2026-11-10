package com.easynutri.controller.api;

import com.easynutri.dto.LogWeightRequest;
import com.easynutri.dto.ProgressReportDto;
import com.easynutri.dto.WeightMeasurementDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.PdfReportService;
import com.easynutri.service.ProgressAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/progress")
@Tag(
    name = "Progress & Analytics",
    description = "Endpoints for multi-day progress reports, charts, weight, and PDF downloads")
public class ProgressApiController {

  private final ProgressAnalyticsService progressService;
  private final PdfReportService pdfReportService;
  private final UserRepository userRepository;

  public ProgressApiController(
      ProgressAnalyticsService progressService,
      PdfReportService pdfReportService,
      UserRepository userRepository) {
    this.progressService = progressService;
    this.pdfReportService = pdfReportService;
    this.userRepository = userRepository;
  }

  @GetMapping
  @Operation(summary = "Get multi-day progress report data for charts and clinical insights")
  public ResponseEntity<ProgressReportDto> getProgressReport(
      @RequestParam(value = "days", defaultValue = "7") int days, Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(progressService.getProgressReport(userId, days));
  }

  @PostMapping("/weight")
  @Operation(summary = "Log periodic body weight measurement")
  public ResponseEntity<WeightMeasurementDto> logWeight(
      @RequestBody LogWeightRequest request, Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(progressService.logWeight(userId, request));
  }

  @GetMapping("/export-pdf")
  @Operation(summary = "Download clinical nutrition progress report as PDF")
  public ResponseEntity<byte[]> exportPdf(
      @RequestParam(value = "days", defaultValue = "7") int days, Principal principal) {
    Long userId = getUserId(principal);
    ProgressReportDto report = progressService.getProgressReport(userId, days);
    byte[] pdfBytes = pdfReportService.generateProgressPdf(report);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"EasyNutri-Report-" + report.endDate() + ".pdf\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdfBytes);
  }

  private Long getUserId(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
