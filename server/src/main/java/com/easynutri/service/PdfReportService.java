package com.easynutri.service;

import com.easynutri.domain.policy.DisplayPolicy;
import com.easynutri.dto.ProgressReportDto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class PdfReportService {

  private static final Color PRIMARY_COLOR = new Color(20, 83, 45); // Emerald-900
  private static final Color ACCENT_COLOR = new Color(22, 163, 74); // Emerald-600
  private static final Color BG_LIGHT = new Color(248, 250, 252); // Slate-50
  private static final Color TEXT_DARK = new Color(30, 41, 59); // Slate-800
  private static final Color TEXT_MUTED = new Color(100, 116, 139); // Slate-500
  private static final Color BORDER_COLOR = new Color(226, 232, 240); // Slate-200

  public byte[] generateProgressPdf(ProgressReportDto report) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Document document = new Document(PageSize.A4, 40, 40, 40, 40);

    try {
      PdfWriter.getInstance(document, out);
      document.open();

      Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, PRIMARY_COLOR);
      Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_MUTED);
      Font h2Font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, PRIMARY_COLOR);
      Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_DARK);
      Font boldBodyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_DARK);
      Font disclaimerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, TEXT_MUTED);

      // 1. Header Banner
      Paragraph title =
          new Paragraph("🌱 EasyNutri Clinical & Nutrition Progress Report", titleFont);
      title.setAlignment(Element.ALIGN_LEFT);
      document.add(title);

      DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US);
      Paragraph sub =
          new Paragraph(
              "Generated on "
                  + LocalDate.now().format(dateFmt)
                  + " | Reporting Period: "
                  + report.startDate().format(dateFmt)
                  + " to "
                  + report.endDate().format(dateFmt)
                  + " ("
                  + report.daysInRange()
                  + " days)",
              subTitleFont);
      sub.setSpacingAfter(15);
      document.add(sub);

      // 2. Member Profile Box
      Paragraph profileHeader = new Paragraph("1. Member Profile & Clinical Baseline", h2Font);
      profileHeader.setSpacingAfter(6);
      document.add(profileHeader);

      PdfPTable profileTable = new PdfPTable(2);
      profileTable.setWidthPercentage(100);
      profileTable.setSpacingAfter(15);

      addProfileRow(
          profileTable, "Full Name / Account:", report.username(), boldBodyFont, bodyFont);
      addProfileRow(
          profileTable,
          "Biometrics:",
          report.age() + " yrs | " + report.biologicalSex() + " | " + report.heightCm() + " cm",
          boldBodyFont,
          bodyFont);

      String weightDisplay =
          report.sensitiveMode()
              ? "[Masked under Sensitive-User Privacy Mode]"
              : DisplayPolicy.formatWeight(report.currentWeightKg(), false)
                  + " ("
                  + report.bmiCategory()
                  + ")";
      addProfileRow(profileTable, "Body Weight & BMI:", weightDisplay, boldBodyFont, bodyFont);

      addProfileRow(
          profileTable,
          "Health Goal & Diet:",
          report.healthGoal() + " | " + report.dietaryPreference(),
          boldBodyFont,
          bodyFont);

      document.add(profileTable);

      // 3. Period Performance Summary Table
      Paragraph summaryHeader = new Paragraph("2. Period Performance Summary", h2Font);
      summaryHeader.setSpacingAfter(6);
      document.add(summaryHeader);

      PdfPTable summaryTable = new PdfPTable(4);
      summaryTable.setWidthPercentage(100);
      summaryTable.setSpacingAfter(15);

      addTableHeaderCell(summaryTable, "Metric", boldBodyFont);
      addTableHeaderCell(summaryTable, "Daily Average", boldBodyFont);
      addTableHeaderCell(summaryTable, "Target / Benchmark", boldBodyFont);
      addTableHeaderCell(summaryTable, "Adherence Status", boldBodyFont);

      String calDisplay =
          report.sensitiveMode()
              ? "[Sensitive Mode]"
              : (Math.round(report.avgDailyCalories()) + " kcal");
      addTableRow(
          summaryTable, "Daily Energy Intake", calDisplay, "Goal Target", "Consistent", bodyFont);

      String protDisplay =
          report.sensitiveMode() ? "Nourishing" : (Math.round(report.avgDailyProteinG()) + " g");
      addTableRow(summaryTable, "Protein Intake", protDisplay, "60-80 g", "Adequate", bodyFont);

      String carbDisplay =
          report.sensitiveMode() ? "Balanced" : (Math.round(report.avgDailyCarbsG()) + " g");
      addTableRow(summaryTable, "Carbohydrates", carbDisplay, "150-250 g", "Normal", bodyFont);

      String fatDisplay =
          report.sensitiveMode() ? "Balanced" : (Math.round(report.avgDailyFatG()) + " g");
      addTableRow(summaryTable, "Healthy Fats", fatDisplay, "40-65 g", "Normal", bodyFont);

      addTableRow(
          summaryTable,
          "Water Intake",
          report.avgDailyWaterMl() + " ml",
          "2,500 ml",
          report.avgDailyWaterMl() >= 2000 ? "On Target" : "Improve",
          bodyFont);

      addTableRow(
          summaryTable,
          "Daily Steps",
          String.format(Locale.US, "%,d steps", report.avgDailySteps()),
          "10,000 steps",
          report.avgDailySteps() >= 8000 ? "High Activity" : "Moderate",
          bodyFont);

      document.add(summaryTable);

      // 4. Streak & Habit Consistency
      Paragraph streakHeader = new Paragraph("3. Lifestyle & Logging Consistency", h2Font);
      streakHeader.setSpacingAfter(6);
      document.add(streakHeader);

      PdfPTable habitTable = new PdfPTable(3);
      habitTable.setWidthPercentage(100);
      habitTable.setSpacingAfter(15);

      addHabitMetric(
          habitTable,
          "Logging Streak",
          report.currentStreakDays() + " Days",
          boldBodyFont,
          subTitleFont);
      addHabitMetric(
          habitTable,
          "Days Logged",
          report.loggedDaysCount() + " of " + report.daysInRange() + " Days",
          boldBodyFont,
          subTitleFont);
      addHabitMetric(
          habitTable,
          "Consistency Score",
          Math.round(report.adherenceScore()) + "/100",
          boldBodyFont,
          subTitleFont);

      document.add(habitTable);

      // 5. Clinical & Behavioral Insights
      Paragraph insightHeader = new Paragraph("4. Clinical & Behavioral Insights", h2Font);
      insightHeader.setSpacingAfter(6);
      document.add(insightHeader);

      if (report.insights() != null && !report.insights().isEmpty()) {
        com.lowagie.text.List bulletList =
            new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
        bulletList.setListSymbol("• ");
        for (String insight : report.insights()) {
          ListItem item = new ListItem(insight, bodyFont);
          item.setSpacingAfter(3);
          bulletList.add(item);
        }
        document.add(bulletList);
      } else {
        document.add(
            new Paragraph(
                "No sufficient data logged for this period to generate insights.", bodyFont));
      }

      // 6. Clinical Disclaimer Footer
      Paragraph spacer = new Paragraph(" ");
      spacer.setSpacingAfter(15);
      document.add(spacer);

      PdfPTable disclaimerBox = new PdfPTable(1);
      disclaimerBox.setWidthPercentage(100);
      PdfPCell cell =
          new PdfPCell(
              new Phrase(
                  "Clinical Disclaimer: This report is generated by EasyNutri for nutritional self-monitoring and lifestyle habit tracking. It does not constitute medical diagnosis, clinical prescription, or formal healthcare evaluation. Users with cardiovascular, kidney, or metabolic diseases should follow advice from their primary physician and licensed dietitian.",
                  disclaimerFont));
      cell.setBackgroundColor(BG_LIGHT);
      cell.setPadding(8);
      cell.setBorderColor(BORDER_COLOR);
      disclaimerBox.addCell(cell);
      document.add(disclaimerBox);

      document.close();
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate PDF progress report: " + e.getMessage(), e);
    }

    return out.toByteArray();
  }

  private void addProfileRow(
      PdfPTable table, String label, String value, Font labelFont, Font valFont) {
    PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
    c1.setBackgroundColor(BG_LIGHT);
    c1.setPadding(5);
    c1.setBorderColor(BORDER_COLOR);

    PdfPCell c2 = new PdfPCell(new Phrase(value, valFont));
    c2.setBackgroundColor(Color.WHITE);
    c2.setPadding(5);
    c2.setBorderColor(BORDER_COLOR);

    table.addCell(c1);
    table.addCell(c2);
  }

  private void addTableHeaderCell(PdfPTable table, String text, Font font) {
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setBackgroundColor(BG_LIGHT);
    cell.setPadding(6);
    cell.setBorderColor(BORDER_COLOR);
    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
    table.addCell(cell);
  }

  private void addTableRow(
      PdfPTable table, String m, String avg, String tgt, String status, Font font) {
    PdfPCell c1 = new PdfPCell(new Phrase(m, font));
    PdfPCell c2 = new PdfPCell(new Phrase(avg, font));
    PdfPCell c3 = new PdfPCell(new Phrase(tgt, font));
    PdfPCell c4 = new PdfPCell(new Phrase(status, font));

    for (PdfPCell c : new PdfPCell[] {c1, c2, c3, c4}) {
      c.setBackgroundColor(Color.WHITE);
      c.setPadding(5);
      c.setBorderColor(BORDER_COLOR);
      table.addCell(c);
    }
  }

  private void addHabitMetric(
      PdfPTable table, String label, String value, Font valFont, Font labelFont) {
    PdfPCell cell = new PdfPCell();
    cell.setBackgroundColor(BG_LIGHT);
    cell.setPadding(8);
    cell.setBorderColor(BORDER_COLOR);
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);

    Paragraph v = new Paragraph(value, valFont);
    v.setAlignment(Element.ALIGN_CENTER);
    Paragraph l = new Paragraph(label, labelFont);
    l.setAlignment(Element.ALIGN_CENTER);

    cell.addElement(v);
    cell.addElement(l);
    table.addCell(cell);
  }
}
