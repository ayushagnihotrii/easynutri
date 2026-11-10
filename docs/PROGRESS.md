# EasyNutri — Development Progress Tracker (Web Platform)

## Status Overview

| Phase | Description | Status | Verification Status |
|---|---|---|---|
| **Phase 0** | **Web Application Architecture Transition & Scaffolding** | **COMPLETE** | **All tests passing, bootJar verified** |
| **Phase 1** | **Authentication, Onboarding, Profiles & Nutrition Foundation** | **COMPLETE** | **All unit, repo & web MVC tests passing** |
| **Phase 2** | **Food Discovery, Multi-Input Finder & Deficiency Assistant** | **COMPLETE** | **All pure Java domain, DB & MVC integration tests passing** |
| **Phase 3** | **Meal Planning, Smart Pantry Tracking & Cooking Management** | **COMPLETE** | **All pure Java domain, DB & MVC integration tests passing** |
| **Phase 4** | **Food Diary, Activity Tracking, Hydration & Progress Analytics** | **COMPLETE** | **All pure Java domain, DB & MVC integration tests passing (57/57)** |
| **Phase 5** | **AI Assistant, Tool Execution, Recipe Adaptation & Safety Gate** | **COMPLETE** | **All pure Java safety, tool execution, DB & MVC tests passing (66/66)** |
| Phase 6 | Community Recipe Blog, Moderation & Badges | Next Up | Scaffolded view at `/community` |
| Phase 7 | Family Profiles, Offline PWA Sync & Sensitive Mode | Planned | Core sensitive mode active; offline PWA sync next |
| Phase 8 | Comprehensive Testing, Security Audit & Production Release | Planned | Continuous verification active |

---

## Phase 5 Implementation Summary (AI Nutrition Intelligence, Safety Gate & Personalisation Complete)

### 1. Pure Java Safety Architecture (`:domain`)
- **`EmergencyDetector` (`com.easynutri.domain.safety`):**
  - Evaluates user queries for acute life-threatening medical emergencies (Anaphylaxis, Cardiac Distress/Chest Pain, Acute Stroke/FAST, Metabolic/Diabetic Crisis, Acute Poisoning).
  - Overrides AI conversational output with immediate, urgent clinical instructions (call 112 / 911 / emergency services immediately).
  - Strictly forbids providing home remedies, nutritional fixes, or lifestyle suggestions during acute medical events.
- **Clinical Hydration Target Auditing (`com.easynutri.domain.hydration`):**
  - Audited `DefaultHydrationTargetProvider` to ensure kidney and heart patients are not constrained to an arbitrary 1,500 ml fluid limit without explicit clinician instruction. Requires clinician review with provisional advisory notes.
- **Sensitive-User Mode Auditing (`com.easynutri.domain.policy`):**
  - Updated `DisplayPolicy` to eliminate distressing deficit terminology, replacing "Active Deficit" with "Gentle Energy Balance".

### 2. Provider-Independent AI Architecture & Tool Execution (`:server`)
- **13 Controlled Java Application Tools (`com.easynutri.ai.tool`):**
  1. `search_foods`: Searches canonical food database with macronutrient and micronutrient density.
  2. `get_nutrition_facts`: Returns verified nutritional facts per 100g from canonical database rows.
  3. `find_recipes`: Searches verified recipes filtered by meal slot and keywords.
  4. `match_user_ingredients`: Matches available home ingredients against recipe database with readiness scoring.
  5. `get_pantry_inventory`: Inspects user's active pantry inventory and storage locations.
  6. `get_expiring_items`: Identifies pantry items nearing expiration to prevent food waste.
  7. `get_meal_plan`: Retrieves active meal plan for today or specific dates.
  8. `get_food_diary_summary`: Audits daily logged meals, calories, macros, and targets.
  9. `find_nutrient_food_sources`: Identifies whole food sources high in specific nutrients (protein, iron, fiber, calcium, potassium) filtered by user allergens.
  10. `check_food_safety`: Pure Java evaluation of food items against profile allergies and health conditions via `SafetyGate`.
  11. `get_hydration_summary`: Retrieves fluid intake, target amount, and clinical restrictions.
  12. `get_activity_summary`: Evaluates step count, calories burned, and energy expenditure.
  13. `adapt_recipe`: Pure Java dietary recipe adaptation (VEGAN, JAIN, DAIRY_FREE, LOW_SODIUM, CUSTOM) with recalculated nutrition.
- **`AiToolExecutor`:**
  - Invokes application tools, measures exact execution latency in milliseconds, and records execution audit trails in `ai_tool_executions`.
- **`AiContextBuilder`:**
  - Aggregates privacy-safe user context (profile, sensitive mode status, allergies, health conditions, diary totals, hydration status, step counts, expiring pantry items).
- **`AiResponseValidator`:**
  - Post-execution clinical validator screening responses against `EmergencyDetector`, `SafetyGate`, and enforcing sensitive-user mode (censor raw calories/deficit terminology).
- **`AiOrchestrator`:**
  - Master pipeline coordinating ContextBuilder, EmergencyDetector, LlmClient, ToolExecutor, and ResponseValidator.

### 3. Recipe Adaptation & Substitutions (`Feature 23`)
- **`RecipeAdaptationService`:**
  - Evaluates recipe modifications across dietary constraints (`VEGAN`, `JAIN`, `DAIRY_FREE`, `LOW_SODIUM`, `CUSTOM`).
  - Recalculates nutrition strictly using canonical `FoodEntity` nutrient values and pure Java domain algorithms. Never trusts AI numbers.
  - Side-by-side comparison (Original vs Adapted) with ingredient diffs, portion scaling, and safety verdicts.
  - Leaves the original `RecipeEntity` database record completely untouched.
  - Persists adapted variants in `recipe_adaptations` table.

### 4. Personalised Weekly Check-ins & Insights (`Feature 24`)
- **`WeeklyInsightService`:**
  - Analyzes longitudinal logs (7-day food diary, hydration consistency, step records, expiring pantry items).
  - Categorizes insights into `PROTEIN`, `HYDRATION`, `ACTIVITY`, `PANTRY`, `CONSISTENCY`.
  - Non-judgmental, encouraging tone with contextual actionable links (`/recipes`, `/hydration`, `/activity`, `/ingredients`).
  - Persists weekly check-ins in `weekly_insights` table.

### 5. Web & REST Interface
- **Web Controllers:**
  - `AssistantWebController` (`/assistant`): Full chat UI, sidebar conversation history, suggested prompt chips, voice dictation.
  - `RecipeAdaptationWebController` (`/recipes/adapt/{id}` and `/recipes/adaptations`): Side-by-side comparison, adaptation form, variant library.
  - `InsightWebController` (`/insights`): Weekly check-in overview, categorized observation cards, refresh trigger.
- **REST APIs:**
  - `AssistantApiController` (`/api/v1/assistant/chat`, `/api/v1/assistant/conversations/chat`)
  - `RecipeAdaptationApiController` (`/api/v1/recipes/adaptations/preview`, `/save`, `/`)
  - `InsightApiController` (`/api/v1/insights`, `/regenerate`)

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
  - Clinician prescription override support.
- **`DefaultInsightEngine` (`com.easynutri.domain.insights`):**
  - Minimum 3-day history threshold before generating dietary trend conclusions.
  - Evaluates protein intake, fiber sufficiency, hypertensive sodium risks (>2,000 mg), and caloric stability.
  - Computes objective adherence scores (0 to 100) combining logging streaks and macro targets.
- **`DisplayPolicy` (`com.easynutri.domain.policy`):**
  - Centralized server-side enforcement of eating-disorder safety and sensitive-user mode. Redacts raw weights, exact calorie figures, and obsessive numbers into qualitative wellbeing indicators.
