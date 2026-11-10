package com.easynutri.controller.web;

import com.easynutri.dto.LogWeightRequest;
import com.easynutri.dto.ProgressReportDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.PdfReportService;
import com.easynutri.service.ProgressAnalyticsService;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/reports", "/progress"})
public class ProgressWebController {

  private final ProgressAnalyticsService progressService;
  private final PdfReportService pdfReportService;
  private final UserRepository userRepository;

  public ProgressWebController(
      ProgressAnalyticsService progressService,
      PdfReportService pdfReportService,
      UserRepository userRepository) {
    this.progressService = progressService;
    this.pdfReportService = pdfReportService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public String progressPage(
      @RequestParam(value = "days", defaultValue = "7") int days,
      Model model,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    ProgressReportDto report = progressService.getProgressReport(userId, days);

    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Progress Analytics & Reports (" + days + " Days)");
    model.addAttribute("activeNav", "reports");
    model.addAttribute("report", report);
    model.addAttribute("selectedDays", days);

    return "reports/index";
  }

  @PostMapping("/weight")
  public String logWeight(
      @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam("weightKg") double weightKg,
      @RequestParam(value = "notes", required = false) String notes,
      @RequestParam(value = "days", defaultValue = "7") int days,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    try {
      LogWeightRequest req = new LogWeightRequest(date, weightKg, notes);
      progressService.logWeight(userId, req);
      redirectAttributes.addFlashAttribute(
          "flashMessage", "Weight measurement recorded (" + weightKg + " kg)!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Failed to record weight: " + e.getMessage());
    }

    return "redirect:/reports?days=" + days;
  }

  @GetMapping("/export-pdf")
  public ResponseEntity<byte[]> exportPdf(
      @RequestParam(value = "days", defaultValue = "7") int days, Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    ProgressReportDto report = progressService.getProgressReport(userId, days);
    byte[] pdfBytes = pdfReportService.generateProgressPdf(report);

    String filename = "EasyNutri-Report-" + report.endDate() + ".pdf";

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdfBytes);
  }

  private Long getUserId(String username) {
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
