package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.dto.ProgressReportDto;
import com.easynutri.dto.WeightMeasurementDto;
import com.easynutri.service.PdfReportService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PdfReportServiceTest {

  private final PdfReportService pdfReportService = new PdfReportService();

  @Test
  @DisplayName("Should generate valid non-empty PDF bytes with PDF magic header")
  void shouldGenerateValidPdfDocument() {
    ProgressReportDto report =
        new ProgressReportDto(
            LocalDate.now().minusDays(6),
            LocalDate.now(),
            7,
            5,
            4,
            85.0,
            List.of("Nov 4", "Nov 5", "Nov 6", "Nov 7", "Nov 8", "Nov 9", "Nov 10"),
            List.of(1800.0, 1950.0, 1850.0, 2100.0, 1900.0, 0.0, 0.0),
            List.of(70.0, 68.0, 75.0, 80.0, 65.0, 0.0, 0.0),
            List.of(210.0, 230.0, 200.0, 240.0, 220.0, 0.0, 0.0),
            List.of(50.0, 55.0, 48.0, 52.0, 45.0, 0.0, 0.0),
            List.of(8000, 9500, 10200, 7800, 11000, 6000, 7500),
            List.of(2200, 2500, 2400, 2100, 2600, 1800, 2000),
            List.of(
                new WeightMeasurementDto(
                    1L, LocalDate.now(), 68.0, 22.5, "Good", "68.0 kg", "Normal")),
            1920.0,
            71.6,
            220.0,
            50.0,
            8571,
            2228,
            "Demo User",
            28,
            "MALE",
            174.0,
            68.0,
            22.5,
            "Normal",
            "MAINTAIN",
            "VEGETARIAN",
            false,
            List.of(
                "Adequate protein intake achieved across 5 logged days.",
                "Optimal daily hydration maintained."));

    byte[] pdf = pdfReportService.generateProgressPdf(report);

    assertNotNull(pdf);
    assertTrue(pdf.length > 500);
    assertEquals("%PDF-", new String(pdf, 0, 5));
  }

  @Test
  @DisplayName("Should generate sensitive-mode protected PDF when sensitiveMode is active")
  void shouldGenerateSensitiveModeProtectedPdf() {
    ProgressReportDto sensitiveReport =
        new ProgressReportDto(
            LocalDate.now().minusDays(6),
            LocalDate.now(),
            7,
            5,
            4,
            85.0,
            List.of("Nov 4", "Nov 5", "Nov 6", "Nov 7", "Nov 8", "Nov 9", "Nov 10"),
            List.of(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            List.of(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            List.of(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            List.of(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            List.of(8000, 9500, 10200, 7800, 11000, 6000, 7500),
            List.of(2200, 2500, 2400, 2100, 2600, 1800, 2000),
            List.of(),
            1920.0,
            71.6,
            220.0,
            50.0,
            8571,
            2228,
            "Demo User",
            28,
            "MALE",
            174.0,
            68.0,
            22.5,
            "Normal",
            "MAINTAIN",
            "VEGETARIAN",
            true, // SENSITIVE MODE ACTIVE
            List.of("Nourishing meals logged with consistent habit maintenance."));

    byte[] pdf = pdfReportService.generateProgressPdf(sensitiveReport);

    assertNotNull(pdf);
    assertTrue(pdf.length > 500);
    assertEquals("%PDF-", new String(pdf, 0, 5));
  }
}
