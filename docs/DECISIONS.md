# EasyNutri — Architecture Decision Records (ADR)

## ADR-001: Multi-Module Architecture & Java 17 Toolchain

### Status
Accepted

### Context
EasyNutri requires a scalable, maintainable, and testable foundation supporting all 34 clinical, nutrition, and pantry features across shared pure Java domain logic, backend REST services, and dataset import CLI utilities. The syllabus and evaluation enforce standard Java OOP principles and zero Kotlin.

### Decision
We partition the active repository into a 3-module Gradle project:
- `:domain`: Pure Java 17 business logic module with zero framework dependencies.
- `:server`: Spring Boot 4.1.2 full-stack web application (Thymeleaf UI + REST API) on Java 17.
- `:tools`: Java command-line utilities for dataset validation and import.
- `data/`: Version-controlled JSON schemas and nutrition datasets.
- `archive/android-prototype/`: Archived Android prototype safely preserved for reference.

All modules standardize on Java 17 via Gradle toolchains resolved by Foojay Convention Plugin.

---

## ADR-002: Android Tech Stack — Java, XML Layouts, ViewBinding, MVVM (Archived)

### Status
Superseded by ADR-006

### Context
Initial Phase 0 prototype evaluated native Android development.

### Decision
The Android application has been safely moved to `archive/android-prototype/` and tagged on branch `android-phase0-backup`. All primary development is shifted to the responsive Spring Boot web application.

---

## ADR-003: Spring Boot 4.1.2 Backend Foundation with Independent Docker Build Context

### Status
Accepted

### Context
The backend serves both browser HTML views and REST APIs. It must support high-throughput cloud deployment and modern Spring ecosystem standards while remaining independently buildable inside minimal Docker containers.

### Decision
- Standardized backend on Spring Boot 4.1.2 and Springdoc OpenAPI 3.2.0.
- Implemented `docker-compose.yml` with `context: ..` pointing to the repository root.
- Multi-stage `Dockerfile` compiles `server:bootJar` in an Eclipse Temurin 17 container.
- Real Spring Security authentication with BCrypt hashing and session-based auth.

---

## ADR-004: Relational Persistence with PostgreSQL & H2 Offline Development

### Status
Accepted

### Context
EasyNutri prioritizes data integrity and health data sovereignty. All user profiles, meal plans, pantry items, and logs must persist across application restarts.

### Decision
- Configured PostgreSQL 16 for production and H2 in-memory mode for rapid local development/testing.
- Versioned Flyway migrations track all relational schema changes under `db/migration/`.
- Strict JPA/Hibernate entities mapped for users, profiles, pantry, and meal plans.

---

## ADR-005: Type-Safe Sealed Domain Safety Candidates and Strict Domain Isolation

### Status
Accepted

### Context
The nutrition rules engine must validate diverse entities (individual food items, compiled recipes, custom pantry meals) against clinical rules and user health profiles without resorting to untyped generic `Object` parameters or loose maps.

### Decision
- Defined a sealed Java 17 interface: `SafetyCandidate permits FoodCandidate, RecipeCandidate`.
- Enforced immutability via Java records (`ProfileContext`, `FoodCandidate`, `RecipeCandidate`, `RuleVerdict`, `SafetyResult`).
- Defensive copies created for all collection fields (`List.copyOf`, `Set.copyOf`).
- Pure Java domain interfaces (`SafetyGate`, `RulesEngine`, `NutritionCalculator`, `RecipeMatcher`, `MealPlanner`, `GroceryBuilder`, `DeficiencyService`, `BurnCalculator`, `InsightEngine`, `RecipeAdapter`).

---

## ADR-006: Pivot from Native Android Application to Full-Stack Responsive Web Application

### Status
Accepted

### Context
The user directive mandates making EasyNutri universally accessible across Windows, macOS, Linux, Android, iPhone, and tablets through modern web browsers, with Progressive Web App (PWA) installation capabilities, rather than constraining users to an Android-only binary.

### Decision
1. Backed up the Android implementation to Git branch & tag `android-phase0-backup`.
2. Moved Android source files to `archive/android-prototype/`.
3. Standardized active Gradle configuration to `:domain`, `:server`, and `:tools`.
4. Transformed `:server` into the unified full-stack application hosting responsive web interfaces, REST endpoints, and security.

---

## ADR-007: UI/UX Architecture — Thymeleaf, Vanilla JavaScript, Lucide Icons, and Chart.js

### Status
Accepted

### Context
To maintain strict Java ecosystem purity, zero Node.js backend dependencies, and fast server-side rendering, we selected a lightweight, robust web stack.

### Decision
- **Template Engine:** Thymeleaf 3.x with component fragments (`layout/main.html`, `fragments/sidebar.html`, `fragments/topbar.html`).
- **Styling:** Custom CSS3 design system (`main.css`) using CSS variables, 8pt spacing grid, responsive CSS Grid/Flexbox, rounded cards, and light/dark modes.
- **Client Scripting:** Modern vanilla JavaScript (`app.js`, `pwa.js`) for interactive drawer toggles, toast notifications, sensitive-mode blurring, and offline detection.
- **Iconography:** Lucide-style embedded SVGs providing lightweight, scalable icons without heavy icon webfont dependencies.
- **Charts:** Chart.js for responsive macro distributions and calorie trend analytics.

---

## ADR-008: Progressive Web App (PWA) Offline Strategy & IndexedDB Sync Queue

### Status
Accepted

### Context
EasyNutri must maintain core offline utility when network connectivity is lost (e.g. checking pantry items or reviewing planned recipes).

### Decision
- Configured Web App Manifest (`manifest.webmanifest`) enabling PWA installation ("Add to Home Screen").
- Service Worker (`sw.js`) caches critical static assets and provides an offline fallback page (`offline.html`).
- Dynamic status badge in the top navigation bar displays live network status ("Online" vs "Offline Mode").
- Browser IndexedDB holds pending diary logs and offline meal plan modifications, queueing them for automatic REST synchronization upon network reconnection.


---

## ADR-009: Phase 1 Relational Data Model & Pure Java Clinical Rules Architecture

### Status
Accepted

### Context
Phase 1 introduces user profile persistence, health setups, clinical calorie calculations, canonical food/recipe data models, dynamic serving scalers, and smart pantry recommendations. All business logic must remain in pure Java with zero web/Spring coupling, while relational tables must support clean schema migrations and user data privacy.

### Decision
1. **Relational Schema Evolution (`V2__phase1_schema.sql`):** Added `user_profiles`, `user_allergies`, `user_health_conditions`, `foods`, `recipes`, `recipe_ingredients`, and `pantry_items` tables with foreign keys and cascade deletions.
2. **Pure Java Domain Calculations:** Implemented `BmiCalculator` (WHO criteria), `NutritionGoalCalculator` (Mifflin-St Jeor BMR + TDEE + clinical floors of 1200/1500 kcal and adolescent/pregnancy safety guards), `DefaultNutritionCalculator` (exact per-100g scaling), `DefaultRecipeAdapter` (serving scaler and safe allergen substitution), and `DefaultRulesEngine` (hard allergen blocks with Indian/international synonyms, dietary checks, and condition limit audits).
3. **Canonical Data Sourcing:** Populated genuine Indian food items and classic recipes with source attribution (`IFCT` 2017 & `USDA FoodData Central`), and marked demo records explicitly with `verified: false`.
4. **Onboarding & Dynamic Dashboard:** Created a 3-step responsive onboarding wizard (`/onboarding`) redirecting new users before dashboard access, and integrated real database-backed clinical targets into `/dashboard`.


---

## ADR-010: Multi-Channel Ingredient Discovery, Leftover Microbiological Safety, and Micronutrient Deficiency Architecture

### Status
Accepted

### Context
Phase 2 mandates comprehensive food discovery and ingredient-based meal matching:
1. Users need to discover recipes from ingredients currently at home via 5 multi-input channels (Typed with autocomplete and Hindi/Hinglish aliases, Voice input via Web Speech API, Photo scan, Barcode scanning, and Smart OCR for grocery receipts/labels).
2. Cooked leftovers (rice, dal, sabzi) require strict food-safety enforcement aligned with ICMR-NIN and FSSAI microbiological limits (temperature and duration).
3. Users need targeted micronutrient guidance across 10 essential vitamins and minerals with evidence-based absorption optimization protocols.
4. Recipe result actions must connect to real backend persistence (saving recipes, batch adding missing ingredients to a persistent grocery checklist, scheduling into meal plans) with zero dead buttons.

### Decision
1. **Multi-Input Domain Model (`IngredientCandidate` & `IngredientResolver`):**
   - Canonicalized all input modalities into an immutable Java 17 record `IngredientCandidate` preserving food IDs, bilingual names, categories, quantities, household units, and leftover metadata.
   - Built `DefaultIngredientResolver` with an Indian alias dictionary (English, Hindi Devanagari, Hinglish), regex-based quantity/unit extraction (`katori`, `cup`, `tbsp`, `g`, `kg`), and conversational phrase splitting.
   - Built `DefaultFoodRecognitionProvider` (assisted confirmation fallback preventing AI hallucinations) and `DefaultOcrProvider`.
2. **Enhanced Recipe Matcher & Categorization (`RecipeMatcher` & `DefaultRecipeMatcher`):**
   - Implemented detailed match classification labels: `Ready Now` (100% matched), `Ready With a Swap` (substitutions available, e.g. tofu for paneer), `Missing 1 Ingredient`, `Missing 2 Ingredients`, and `Missing 3+ Ingredients`.
   - Prioritized recipes utilizing leftovers with bonus scoring.
   - Guarded all match results with `SafetyGate`; recipes violating active user allergies or severe health restrictions receive an absolute `BLOCK` and are omitted from user results.
3. **Leftover Microbiological Safety (`LeftoverSafetyEvaluator`):**
   - Evaluates storage method (`ROOM_TEMP`, `REFRIGERATED`, `FROZEN`) and hours elapsed against ICMR and FSSAI microbiological guidelines.
   - Enforces `BLOCK` for room temperature food > 4 hours (due to heat-stable bacterial enterotoxins such as *Bacillus cereus*) and refrigerated food > 72 hours (> 3 days).
   - Issues clinical `WARN` requiring reheating to >= 75°C for 2-4 hours room temp or 48-72 hours refrigerated food.
4. **Micronutrient Deficiency Assistant (`DefaultDeficiencyService`):**
   - Encapsulated ICMR-NIN 2024 daily reference intakes for 10 nutrients: Iron (19mg M / 29mg F), Vitamin B12 (2.2mcg), Vitamin D (15mcg), Calcium (1000mg), Protein (54g M / 46g F), Dietary Fibre (35g M / 30g F), Folate (300mcg), Zinc (13mg M / 11mg F), Vitamin C (80mg M / 65mg F), and Magnesium (385mg M / 325mg F).
   - Implemented dynamic % RDA calculation and evidence-based absorption pairing tips (e.g. Vitamin C with plant iron, avoiding tea/coffee within 2 hours of iron meals, healthy fats with Vitamin D).
5. **Relational Schema Evolution (`V3__phase2_schema.sql`):**
   - Created persistent tables `saved_recipes`, `grocery_items`, `meal_plan_items`, and `food_portions` with proper foreign keys and constraints.
   - Developed `GroceryService`, `SavedRecipeService`, `MealPlanService`, and `BarcodeLookupService` backing interactive UI buttons.


---

## ADR-011: Phase 3 Intelligent Meal Planning, Smart Pantry Tracking, Plan-to-Pantry Reconciliation, and Interactive Cooking Management

### Status
Accepted

### Context
Phase 3 mandates delivering a fully integrated, clinically guided meal planning, pantry tracking, automatic grocery generation, and distraction-free cooking execution workflow:
1. **Intelligent Meal Planning:** The weekly planner must generate balanced 7-day schedules conforming to caloric/protein targets, user diet, allergies, and health conditions, while prioritizing existing pantry stock, awarding bonuses to expiring ingredients, enforcing 3-day meal variety, and preserving locked meals during plan regeneration. Users must also be able to swap any meal with safe alternatives via a 1-tap modal.
2. **Bookmarks & Collections:** Users must be able to organize recipes into customizable collections (e.g. "Favorites", "High Protein", "Diabetic Friendly") and record personal recipe adjustments/notes with instant search filtering.
3. **Smart Pantry Tracking & Waste Prevention:** Inventory must track quantity, unit, storage location (Cupboard, Refrigerator, Freezer), purchase date, and expiration date, calculating live shelf-life metrics and providing "Cook Before It Expires" waste-prevention recipe recommendations.
4. **Plan-to-Pantry Grocery Reconciliation:** The grocery list must compute exact deficit quantities ($\text{Recipe Demand} - \text{In Pantry Stock} = \text{To Buy}$) across all planned meals and servings, while strictly preserving manual user additions and checked items during auto-reconciliation. Purchased items must be directly transferable into pantry inventory.
5. **Interactive Cooking Mode:** Users require a full-screen, distraction-free cooking view with scaled ingredient checklists, automatic regex-based instruction timer detection, Web Speech API voice commands & text-to-speech, and idempotent pantry ingredient deduction upon cooking completion (preventing double-deduction).

### Decision
1. **Pure Java Domain Engines (`:domain`):**
   - Implemented `DefaultMealPlanner` integrating `SafetyGate` audits, pantry/expiry scoring weights, locked slot preservation, and 1-tap swap generation.
   - Implemented `DefaultGroceryBuilder` calculating multi-recipe demands, subtracting pantry inventory, and grouping into Indian categories (`VEGETABLES`, `PULSES`, `GRAINS`, `DAIRY`, `SPICES`, `GENERAL`).
   - Introduced domain records: `MealSlotProposal`, `DailyMealPlan`, `WeeklyMealPlan`, `NutrientGapProvider`, and `EmptyNutrientGapProvider`.
2. **Relational Schema Evolution (`V4__phase3_schema.sql`):**
   - Extended `meal_plan_items` (`locked`, `completed`, `selection_reason`, `notes`).
   - Extended `pantry_items` (`storage_location`, `unit`, `purchase_date`, `notes`).
   - Extended `grocery_items` (`is_auto_generated`).
   - Created tables `recipe_collections`, `collection_recipes`, `cooking_sessions`, and `user_notifications`.
3. **Application Services & Idempotency (`:server`):**
   - Built `MealPlanService`, `PantryService`, `GroceryService`, `RecipeCollectionService`, and `CookingService`.
   - Prevented double-deduction in `CookingService` using persistent boolean flag `CookingSessionEntity.pantry_deducted`.
   - Implemented `PantryExpiryScheduler` running a daily `@Scheduled` job generating in-app warning notifications for items within 2 days of expiration.
4. **Thymeleaf UI & Web Speech Integration (`:server`):**
   - Created dedicated responsive views: `/planner` (7-day calendar), `/saved-recipes` (collections & notes), `/pantry` (inventory & waste prevention), and `/cook/{id}` (distraction-free cooking with Web Speech voice recognition and timer countdowns).


---

## ADR-012: Phase 4 Food Diary, Activity Tracking, Hydration Management, and Progress Analytics

### Status
Accepted

### Context
Phase 4 requires implementing the clinical self-monitoring core of EasyNutri:
1. **Food Diary & Macro Tracking (`/diary`):** Daily timeline across Breakfast, Lunch, Dinner, and Snack slots. Critical requirement: historical nutritional snapshots must be immutable at log time so future updates to the canonical food catalog never retroactively alter past diaries.
2. **Step Counter & Pedometer Integration (`/tracking`):** Explicitly forbids fabricated live sensor readings; relies on verified manual entry or wearable imports.
3. **MET-Based Energy Expenditure & Step Deduplication (`/tracking`):** Workouts must be calculated using clinical Metabolic Equivalent of Task (MET) multiplied by body weight. Crucially, ambulatory workouts (walking, running, sports) must be deduplicated from daily step counts to prevent double-counting active energy expenditure.
4. **Hydration Tracking & Clinical Fluid Restrictions (`/hydration`):** Fluid target baseline ($35\text{ ml/kg} + \text{workout supplement}$) must enforce strict clinical safety caps (1,500 ml/day) and prominent warnings for Congestive Heart Failure (CHF) and Chronic Kidney Disease (CKD) patients.
5. **Progress Analytics & Downloadable PDF Reports (`/reports`):** Multi-day trend charts (Chart.js), rule-based insights requiring $\ge 3$ days of data, longitudinal weight history, and open-source PDF report generation (OpenPDF) strictly adhering to eating-disorder sensitive-user privacy masking.

### Decision
1. **Pure Java Domain Implementations (`:domain`):**
   - Implemented `DiaryAggregator` and `DiaryItemSnapshot` encapsulating immutable nutrient snapshots (`calories`, `proteinG`, `carbsG`, `fatG`, `fiberG`, `sodiumMg`) and daily slot summaries.
   - Implemented `ActivityBurnEngine` implementing clinical MET formulas, workout step estimation, step deduplication against total pedometer counts, and `DailyEnergyBalance` computation.
   - Implemented `DefaultHydrationTargetProvider` calculating body-mass fluid baselines with mandatory clinical fluid restriction caps for heart failure and renal disease.
   - Implemented `DefaultInsightEngine` requiring $\ge 3$ days of historical logging before drawing dietary observations (protein adequacy, fiber sufficiency, hypertensive sodium risks, and habit streaks).
   - Implemented `DisplayPolicy` centralizing sensitive-user mode redactions across HTML views, DTOs, and generated PDFs.
2. **Relational Schema Evolution (`V5__phase4_schema.sql`):**
   - Created `food_diary_entries` and `food_diary_items` with immutable nutritional snapshot columns.
   - Created `step_records` with uniqueness on `(user_id, record_date)`.
   - Created `workout_logs` with MET and calorie calculations.
   - Created `water_logs` for timestamped hydration portions.
   - Created `weight_measurements` with BMI tracking.
   - Created `hydration_preferences` storing user fluid allowances, clinical restrictions, and reminder schedules.
3. **Application Services & Security (`:server`):**
   - Developed `DiaryService`, `ActivityTrackingService`, `HydrationService`, and `ProgressAnalyticsService`.
   - Implemented `PdfReportService` using OpenPDF to compile clinical nutrition summaries, habit consistency scores, and disclaimer notices.
   - Connected `CookingService.finishCooking()` directly to `DiaryService.logCookingSessionMeal()` with double-logging prevention via `CookingSessionEntity.diary_logged`.
4. **Responsive Web UI & REST APIs (`:server`):**
   - Created `/diary` with date navigation, quick-add food/recipe modal, and 1-click meal duplication.
   - Created `/tracking` with step goal ring, daily energy budget breakdown, and workout logger.
   - Created `/hydration` with visual progress, quick-add buttons (+150ml, +250ml, +350ml, +500ml), and clinical restriction alert banner.
   - Created `/reports` with 7d/30d/90d filters, Chart.js trends, weight logger, and 1-click PDF download (`/reports/export-pdf`).
   - Exposed matching REST controllers under `/api/v1/diary`, `/api/v1/activity`, `/api/v1/hydration`, and `/api/v1/progress`.

---

## ADR-013: Phase 5 AI Nutrition Intelligence, Pure Java Rules Verification, Recipe Adaptation, and Weekly Check-ins

### Status
Accepted

### Context
Phase 5 introduces conversational AI assistance and personalized dietary intelligence into EasyNutri. A paramount architectural principle of the platform is **"AI proposes, Java rules verify."**
Under no circumstance may an AI model invent nutritional numbers, hallucinate serving quantities, or bypass medical constraints. Furthermore:
1. **Clinical Safety Boundary & Emergency Screener:** The platform must distinguish between lifestyle nutritional advice and acute, life-threatening medical emergencies (e.g. anaphylaxis, myocardial infarction, acute stroke, metabolic shock, poisoning). During acute distress, the assistant must immediately cease all nutritional commentary and mandate urgent emergency services dispatch (112/911).
2. **Provider-Independent AI Architecture:** The system must function identically in offline/deterministic environments (without third-party API keys) and when remote LLMs (Gemini, OpenAI) are connected.
3. **Controlled Java Tool Execution:** AI interactions must execute through 13 explicit, sandboxed Java tools that inspect real database records (foods, recipes, pantry inventory, meal plans, diary logs, hydration, activity).
4. **Recipe Adaptation Invariance:** Adapting recipes for vegan, Jain, dairy-free, or low-sodium diets must recalculate nutritional values strictly using canonical food database entries and pure Java domain algorithms, leaving base recipe records completely unmodified.
5. **Eating-Disorder Sensitivity:** When `sensitive_mode` is enabled, the AI assistant and weekly check-ins must suppress raw calorie counts and weight numbers, replacing aggressive deficit terminology with "Gentle Energy Balance".

### Decision
1. **Pure Java Safety Screening (`:domain`):**
   - Implemented `EmergencyDetector` (`com.easynutri.domain.safety`) with deterministic keyword screening for acute crises (cardiac distress, anaphylaxis, stroke FAST symptoms, diabetic emergencies, poisoning), returning mandatory emergency referral directives.
   - Updated `DisplayPolicy` to eliminate distressing terminology ("Active Deficit" replaced with "Gentle Energy Balance").
   - Audited `DefaultHydrationTargetProvider` to require clinician-reviewed fluid restriction targets for heart/kidney conditions rather than assuming arbitrary global caps.
2. **Database Schema Evolution (`V6__phase5_schema.sql`):**
   - Created `ai_conversations` and `ai_messages` for multi-turn conversational session persistence.
   - Created `ai_tool_executions` recording tool names, JSON arguments, results, and execution latency.
   - Created `recipe_adaptations` storing user-specific recipe modifications and recalculated nutritional metrics without mutating base recipes.
   - Created `weekly_insights` capturing evidence-backed weekly check-in observations.
3. **Provider-Independent Tool Execution & Orchestration (`:server`):**
   - Implemented `AiToolRegistry` and 13 application tools (`SearchFoodsTool`, `GetNutritionFactsTool`, `FindRecipesTool`, `MatchUserIngredientsTool`, `GetPantryInventoryTool`, `GetExpiringItemsTool`, `GetMealPlanTool`, `GetFoodDiarySummaryTool`, `FindNutrientFoodSourcesTool`, `CheckFoodSafetyTool`, `GetHydrationSummaryTool`, `GetActivitySummaryTool`, `AdaptRecipeTool`).
   - Implemented `AiToolExecutor` auditing every tool call into `ai_tool_executions`.
   - Built `AiContextBuilder` assembling privacy-safe user context across active logs.
   - Built `AiResponseValidator` enforcing post-generation clinical and sensitive-mode compliance.
   - Created `AiOrchestrator` tying the pipeline into a cohesive, deterministic conversational engine.
   - Implemented `ExternalLlmClient` with `@Primary` fall-through to `DeterministicLlmClient`.
4. **Recipe Adaptation & Weekly Check-ins (`:server`):**
   - Implemented `RecipeAdaptationService` providing preview and save workflows with pure Java nutrition recalculation and side-by-side diffing.
   - Implemented `WeeklyInsightService` synthesizing 7-day diary, hydration, activity, and pantry logs into constructive check-in cards.
5. **Frontend Web Controllers & UI Templates (`:server`):**
   - Built `/assistant` with sidebar conversation history, suggested prompt chips, markdown rendering, tool execution tags, and Web Speech API voice dictation.
   - Built `/recipes/adapt/{id}` side-by-side comparison UI and `/recipes/adaptations` variant library.
   - Built `/insights` weekly check-in layout with categorized cards and contextual action shortcuts.

### Consequences
- Zero AI hallucinations: All nutrient metrics, ingredient proportions, and safety decisions are computed directly from verified PostgreSQL/H2 entities by pure Java domain algorithms.
- Full offline testability: Complete test suite runs without external network access or paid API keys.
- Complete regulatory and clinical safety compliance: Emergency medical conditions are immediately intercepted and directed to professional healthcare providers.
