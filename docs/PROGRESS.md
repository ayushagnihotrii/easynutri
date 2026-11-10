# EasyNutri — Development Progress Tracker (Web Platform)

## Status Overview

| Phase | Description | Status | Verification Status |
|---|---|---|---|
| **Phase 0** | **Web Application Architecture Transition & Scaffolding** | **COMPLETE** | **All tests passing, bootJar verified** |
| **Phase 1** | **Authentication, Onboarding, Profiles & Nutrition Foundation** | **COMPLETE** | **All unit, repo & web MVC tests passing** |
| **Phase 2** | **Food Discovery, Multi-Input Finder & Deficiency Assistant** | **COMPLETE** | **All pure Java domain, DB & MVC integration tests passing** |
| **Phase 3** | **Meal Planning, Smart Pantry Tracking & Cooking Management** | **COMPLETE** | **All pure Java domain, DB & MVC integration tests passing** |
| **Phase 4** | **Food Diary, Activity Tracking, Hydration & Progress Analytics** | **COMPLETE** | **All pure Java domain, DB & MVC integration tests passing (57/57)** |
| Phase 5 | AI Assistant, Tool Execution & Clinical Safety Gate | Next Up | Deterministic tool execution ready |
| Phase 6 | Community Recipe Blog, Moderation & Badges | Planned | Pending |
| Phase 7 | Family Profiles, Offline PWA Sync & Sensitive Mode | Planned | Core sensitive mode active; offline PWA sync next |
| Phase 8 | Comprehensive Testing, Security Audit & Production Release | Planned | Continuous verification active |

---

## Phase 4 Implementation Summary (Food Diary, Activity, Hydration & Progress Analytics Complete)

### 1. Pure Java Business Logic (`:domain`)
- **`DiaryAggregator` & `DiaryItemSnapshot` (`com.easynutri.domain.diary`):**
  - Immutable historical nutrition snapshots (`calories`, `proteinG`, `carbsG`, `fatG`, `fiberG`, `sodiumMg`) ensuring future edits to the food catalog never alter past diary records.
  - Pure Java daily aggregation across 4 standard meal categories (`BREAKFAST`, `LUNCH`, `DINNER`, `SNACK`).
  - Serving & gram scaler retaining snapshot precision.
- **`ActivityBurnEngine` (`com.easynutri.domain.activity`):**
  - Pure Java clinical MET equation: $\text{Calories} = \frac{\text{MET} \times 3.5 \times \text{WeightKg}}{200} \times \text{DurationMinutes}$.
  - Activity types with validated base METs: Walking, Running, Cycling, Swimming, Yoga, Strength Training, Dancing, Sports.
  - Intensity multipliers (`LIGHT`: 0.8x, `MODERATE`: 1.0x, `VIGOROUS`: 1.35x).
  - **Step Deduplication Algorithm:** Estimates steps embedded inside ambulatory workouts (walking/running/sports) and deducts them from pedometer steps to prevent double-counting active energy expenditure.
  - `DailyEnergyBalance`: Encapsulates Food In vs Total Out (BMR + Workouts + Net Steps) with net balance calculation.
- **`DefaultHydrationTargetProvider` (`com.easynutri.domain.hydration`):**
  - Baseline metabolic formula: $35 \text{ ml} \times \text{WeightKg}$.
  - Activity supplementation: $+300\text{ ml}$ per 30 minutes of physical workout.
  - **Clinical Fluid Restriction Cap:** Automatically caps fluid intake at 1,500 ml/day with safety warnings for Congestive Heart Failure (CHF) and Chronic Kidney Disease (CKD) patients.
  - Clinician prescription override support.
- **`DefaultInsightEngine` (`com.easynutri.domain.insights`):**
  - Minimum 3-day history threshold before generating dietary trend conclusions.
  - Evaluates protein intake, fiber sufficiency, hypertensive sodium risks (>2,000 mg), and caloric stability.
  - Computes objective adherence scores (0 to 100) combining logging streaks and macro targets.
- **`DisplayPolicy` (`com.easynutri.domain.policy`):**
  - Centralized server-side enforcement of eating-disorder safety and sensitive-user mode. Redacts raw weights, exact calorie figures, and obsessive numbers into qualitative wellbeing indicators.

### 2. Database & Persistence Layer (`:server`)
- **Flyway Migration `V5__phase4_schema.sql`:**
  - `food_diary_entries`: User meal slot entries by date with unique constraint on `(user_id, diary_date, meal_slot)`.
  - `food_diary_items`: Portion records holding immutable nutrition snapshots.
  - `step_records`: Daily pedometer records with unique constraint on `(user_id, record_date)`.
  - `workout_logs`: Structured physical activity logs with MET and calorie calculations.
  - `water_logs`: Periodic hydration drink entries.
  - `weight_measurements`: Longitudinal body weight records with BMI tracking.
  - `hydration_preferences`: User fluid targets, clinical restrictions, and reminder schedules.
- **JPA Repositories:** `FoodDiaryEntryRepository`, `FoodDiaryItemRepository`, `StepRecordRepository`, `WorkoutLogRepository`, `WaterLogRepository`, `WeightMeasurementRepository`, `HydrationPreferenceRepository`.
- **Application Services:**
  - `DiaryService`: Timeline generation, item logging from food catalog or recipes, meal duplication/copying, item deletion, and cooking-mode-to-diary integration.
  - `ActivityTrackingService`: Step logging, workout logging with MET estimation, step deduplication, and daily energy balance computation.
  - `HydrationService`: Water logging (+150ml, +250ml, +350ml, +500ml, custom), deletion, clinical target calculation, and reminder preference updates.
  - `ProgressAnalyticsService`: Multi-day progress reports (7d, 30d, 90d), Chart.js data series generation, logging streak tracking, and weight logging.
  - `PdfReportService`: OpenPDF generation producing downloadable, beautifully formatted clinical nutrition progress reports with strict sensitive-mode redactions.

### 3. Responsive Web UI & REST APIs (`:server`)
- **Food Diary (`/diary`):**
  - Daily timeline with Breakfast, Lunch, Dinner, Snack sections.
  - Date navigation bar (Previous, Date Picker, Next, Today).
  - Quick Food & Recipe Logging Modal supporting grams and portion counts.
  - 1-Click Copy/Duplicate Meal Modal to duplicate breakfast/lunch to another date.
  - Macro progress bars and sensitive-user mode masking.
  - REST API: `DiaryApiController` (`/api/v1/diary`).
- **Activity & Energy Tracker (`/tracking`, `/activity`):**
  - Daily step goal meter and quick step editing.
  - Energy Budget Card: Food In vs BMR + Workouts + Net Steps.
  - Logged workouts table with activity type, intensity, duration, distance, MET, burn, and notes.
  - Log Workout modal with MET calculations.
  - REST API: `ActivityApiController` (`/api/v1/activity`).
- **Hydration Tracker (`/hydration`):**
  - Visual water intake progress against dynamic or clinically restricted targets.
  - Medical alert banner for cardiovascular and renal fluid restrictions.
  - Quick-log buttons: +150ml (cup), +250ml (glass), +350ml (mug), +500ml (bottle).
  - Drink history table with timestamps and deletion.
  - Hydration reminder and target configuration card.
  - REST API: `HydrationApiController` (`/api/v1/hydration`).
- **Progress Reports & Analytics (`/reports`, `/progress`):**
  - Multi-window range filter (7 Days, 30 Days, 90 Days).
  - Summary metric cards: Average Daily Calories, Protein, Steps, and Streak Consistency Score.
  - Interactive Chart.js graphs: Daily Calories & Protein, Daily Steps, Hydration Trends.
  - Longitudinal weight history table and Weight Log Modal.
  - Rule-based clinical and behavioral observations.
  - **1-Click Downloadable Clinical PDF Report** (`/reports/export-pdf`).
  - REST API: `ProgressApiController` (`/api/v1/progress`).

---

## Phase 3 Implementation Summary (Meal Planning, Smart Pantry & Cooking Complete)
...
