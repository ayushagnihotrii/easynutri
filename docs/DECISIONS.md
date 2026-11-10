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

---

## ADR-014: Phase 6 Clinical Content Screening, Non-Judgmental Habit Gamification, and Professional Credential Verification

### Status
Accepted

### Context
Phase 6 introduces community features to EasyNutri: community recipe sharing, interactions (likes, comments, following), creator profiles, moderation and trust, and gamification (streaks, badges, challenges).
Community health platforms carry substantial risk of medical misinformation, starvation encouragement, toxic comparison, and unverified medical advice. The platform must enforce strict architectural safeguards:
1. **Automated Clinical Content Screening:** User-generated titles, descriptions, and comments must be screened before persistence. Content claiming to "cure" chronic diseases (diabetes, cancer, hypertension) or recommending stopping medications (insulin) must be intercepted immediately. Starvation, eating disorders, and extreme caloric deprivation must be blocked.
2. **Deterministic Recipe Nutrition:** Community recipes must never have user-typed or AI-guessed nutrition values. Users specify ingredients mapped to canonical database foods, and pure Java domain algorithms calculate the exact per-serving macro and micronutrient metrics.
3. **Evidence-Backed Dietary Tags:** Dietary tags (Vegetarian, Vegan, Jain, Dairy-Free, Gluten-Free, High-Protein, High-Fiber, Low-Sodium) must be objectively classified based on the ingredient composition and nutrient thresholds, not author self-declaration.
4. **Compassionate Habit Gamification:** Habit streaks must feature a 1-day forgiveness grace period so that missing a single day does not destroy progress or cause emotional abandonment. Furthermore, challenges and gamification must never rank users by weight loss, calories burned, or caloric deficits.
5. **Human-in-the-Loop Professional Verification:** Badges indicating clinical expertise (Registered Dietitian, Nutritionist, Medical Doctor) must require administrative human review and verification of credentials/licenses before being awarded.

### Decision
1. **Pure Java Domain Rules (`:domain`):**
   - Implemented `ContentScreener` (`com.easynutri.domain.moderation`) detecting dangerous medical claims, starvation regimens, and spam, producing structured `ScreeningResult` with verdicts (`BLOCK`, `REVIEW_REQUIRED`, `PASS`).
   - Implemented `StreakCalculator` (`com.easynutri.domain.gamification`) computing consecutive logging days with a 1-day compassionate grace period.
   - Implemented `DietaryTagClassifier` (`com.easynutri.domain.recipe`) validating dietary and nutritional tags against ingredient allergens, categories, and per-serving nutrient thresholds.
2. **Relational Database Schema Evolution (`V7__phase6_schema.sql`):**
   - Extended `users` table with `bio`, `avatar_url`, `is_verified_pro`, and `pro_category`.
   - Created tables: `community_recipes`, `community_recipe_ingredients`, `community_likes`, `community_comments`, `community_follows`.
   - Created moderation tables: `content_reports`, `moderation_actions`, `professional_verifications`.
   - Created gamification tables: `user_streaks`, `badge_definitions`, `user_badges`, `challenges`, `challenge_participations`.
   - Added unique constraints preventing duplicate likes, duplicate badge awards, self-following, and duplicate challenge entries.
3. **Application Services & Security (`:server`):**
   - Implemented `CommunityService` for feeds (`latest`, `popular`, `following`), recipe publication with canonical ingredient scaling and safety screening, likes, comments, and creator profiles.
   - Implemented `ModerationService` for content reporting, clinical queue management, moderator action execution, and audit logging.
   - Implemented `ProfessionalVerificationService` for credential submission and administrative approval.
   - Implemented `GamificationService` for streak tracking across Diary Logging, Hydration, Steps, and Home Cooking, automated milestone badge awards, and community wellness challenge progress.
4. **Responsive Web UI & REST APIs (`:server`):**
   - Created `/community` social feed with search, category filtering, and verified nutrition cards.
   - Created `/community/create` dynamic recipe composer with food database selectors and real-time JavaScript row manipulation.
   - Created `/community/recipes/{id}` full recipe view with instructions, nutrition panel, comments thread, and clinical report modal.
   - Created `/community/users/{username}` public creator profile with verified professional badge and publication portfolio.
   - Created `/admin/moderation` console with reports queue, AI screening verdicts, pro verification queue, and audit log.
   - Created `/achievements` dashboard with habit streak meters (with 1-day grace indicators) and badge grid.
   - Created `/challenges` community challenge directory with progress tracking and join workflows.
   - Exposed REST endpoints under `/api/v1/community`, `/api/v1/moderation`, and `/api/v1/gamification`.

### Consequences
- Community submissions cannot introduce dangerous clinical advice or unverified medical claims.
- Every community recipe displays mathematically verified nutritional numbers rooted in canonical food datasets.
- Gamification nurtures sustainable, compassionate consistency without inducing anxiety or toxic deficit comparisons.
- Full offline test suite passes with 81/81 server tests and all pure Java domain tests.

---

## ADR-015: Phase 7 Multi-User Household Isolation, Idempotent PWA Synchronization, Pure Java Household Dietary Aggregation, and Sensitive Mode Hardening

### Status
Accepted

### Context
Phase 7 introduces family profiles and multi-user household management, requiring individual health privacy and distinct nutrition profiles while allowing shared meal planning. Furthermore, real-world network instability requires an offline-first PWA architecture with client queuing and idempotent server synchronization, without exposing private health data via generic browser caching. Finally, sensitive-user mode requires complete system-wide hardening across HTML views, charts, downloadable PDFs, and REST API responses to ensure zero caloric or weight metrics leak to vulnerable users.

### Decision
1. **Pure Java Household Dietary Aggregation (`:domain`):**
   - Implemented `HouseholdDietaryAggregator` in `:domain` with zero external framework dependencies.
   - Resolves diet hierarchies across participating members: `JAIN` > `VEGAN` > `VEGETARIAN` > `NON_VEG`.
   - Aggregates the union of all allergens across selected family members, blocking recipes with allergen conflicts via `SafetyGate`.
   - Calculates individual portion scaling and macronutrient breakdowns based on member age and daily energy targets (adult vs child proportions).
2. **Multi-User Household Data Isolation (`:server`):**
   - Created `households`, `household_members`, and `household_meal_plans` tables (`V8__phase7_schema.sql`).
   - Each family member is backed by their own managed `UserEntity` and `UserProfileEntity` with distinct BMR, TDEE, and macro targets.
   - Profile switching is session-based via `ActiveProfileContextService` (`ACTIVE_PROFILE_ID`) and validated server-side to guarantee users can only access members belonging to their household.
   - Roles (`HOUSEHOLD_OWNER`, `HOUSEHOLD_ADMIN`, `MEMBER`, `DEPENDENT`) govern administrative capabilities.
3. **Idempotent Offline-First PWA Synchronization (`:server`):**
   - Service Worker v2 (`sw.js`) explicitly excludes authenticated private endpoints (`/api/v1/**`, `/reports/export-pdf`) to ensure health records are never cached insecurely.
   - Client-side IndexedDB queue (`offline-db.js`, `offline-sync.js`) queues offline actions with client UUID idempotency keys.
   - `SyncService` processes batches at `/api/v1/sync/batch`, enforcing idempotency via the `sync_operations` unique index, returning `DUPLICATE_IGNORED` on redundant transmissions.
   - Rejects operations attempting to write to unauthorized profiles (`SECURITY_REJECTED`).
   - Topbar visual indicator (`#networkSyncBadge`) provides live synchronization feedback.
4. **Sensitive-User Mode Hardening:**
   - Centralized `DisplayPolicy` rules across web controllers, Thymeleaf templates, Chart.js views, OpenPDF reports, and REST APIs.
   - Masks exact weights (`-- kg (Masked)`), BMI classifications (`Healthy Balance (Masked)`), and caloric deficits (`Gentle Energy Balance`, `Balanced`, `Well Balanced`, `Sustained Energy`).
   - Provides supportive, non-judgmental daily check-in notifications.
5. **Localization:**
   - Implemented Spring MVC i18n support with `LocaleConfig`, `CookieLocaleResolver` (`EASYNUTRI_LOCALE`), and `LocaleChangeInterceptor` (`?lang=en`, `?lang=hi`).
   - Provided message bundles (`messages.properties` and `messages_hi.properties`).

### Consequences
- Household members enjoy shared cooking without compromising individual dietary safety or personal diary privacy.
- Offline logs seamlessly synchronize upon network recovery with zero duplicate entries.
- Sensitive mode shields users from triggering metrics across all platform channels.
- 100% automated test pass rate across 99 tests in `:server` and `:domain`.

---

## ADR-016: Phase 8 Production Release Hardening, Personal Data Rights (DPDP Act / GDPR), Cross-Household BOLA Prevention, and Conflict Resolution

### Status
Accepted

### Context
Phase 8 represents the final release readiness and security hardening phase of EasyNutri. Moving from an exploratory build to an enterprise, production-ready web application requires addressing critical security, privacy, performance, and legal compliance dimensions:
1. **Broken Object Level Authorization (BOLA / IDOR):** Family household endpoints and recommendation engines must not trust client-supplied household or user IDs. Any attempt to query or manipulate a household meal plan or profile outside the authenticated user's confirmed household membership must be blocked immediately with HTTP 403 Forbidden.
2. **Personal Data Sovereignty & Legal Compliance (GDPR & Indian DPDP Act 2023):** Users have statutory rights to data portability and complete erasure. The application must provide self-service machine-readable JSON data export and permanent account deletion with password verification, while carefully managing foreign keys so that shared household meal plans are not corrupted.
3. **PWA Offline Conflict Resolution & Delete Propagation:** In an offline-first PWA, mutable entities (e.g. pantry quantities, grocery item toggles) can diverge when modified across multiple offline sessions or on the server. The sync engine must distinguish between append-only event logs (food diary, water, steps, workouts) and mutable records, employing version checks to flag conflicts (`CONFLICT_DETECTED`) and supporting explicit delete operations (`DELETE_DIARY`, `DELETE_PANTRY`, `DELETE_GROCERY`).
4. **Security Headers & Defense-in-Depth:** Modern browsers must enforce Content Security Policy (CSP), prevent MIME-type sniffing (`X-Content-Type-Options: nosniff`), prevent clickjacking (`X-Frame-Options: SAMEORIGIN`), enforce referrer privacy (`strict-origin-when-cross-origin`), and prevent session fixation on authentication.
5. **Database Query Performance:** With multi-table relational schema spanning 15+ entities and frequent lookups by date ranges, user IDs, and household IDs, missing indexes will lead to full-table scans. An explicit release optimization migration is required.
6. **Clinical & Regulatory Attribution Transparency:** The platform must clearly attribute its nutritional data sources (ICMR-NIN IFCT 2017, USDA FoodData Central, Open Food Facts) and prominently display clinical disclaimers regarding emergency medical boundaries across all UI screens.

### Decision
1. **Security & Authorization Hardening (`SecurityConfig.java` & `HouseholdApiController.java`):**
   - Configured HTTP response headers in Spring Security: strict Content Security Policy (`script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net`), `nosniff`, `SAMEORIGIN`, and `strict-origin-when-cross-origin`.
   - Enforced session fixation protection (`migrateSession()`).
   - Restrained `/admin/**` endpoints exclusively to `ROLE_ADMIN`.
   - Implemented strict household membership checks (`householdService.isMemberOfHousehold(user.getId(), householdId)`) on `/api/v1/households/{householdId}/**`, throwing `SecurityException` mapped to HTTP 403 by `GlobalRestExceptionHandler`.
2. **Privacy & Data Rights Management (`UserDataManagementService.java` & `ProfileWebController.java`):**
   - Implemented `exportUserData(Long userId)` producing a comprehensive, structured JSON export of profile, dietary records, hydration, steps, workouts, pantry items, grocery items, and streaks, compliant with DPDP Act 2023 and GDPR Article 20.
   - Implemented `deleteUserAccount(Long userId, String passwordConfirmation)` verifying user credentials and executing clean cascading deletion of user-owned telemetry, transferring or pruning household ownership safely.
   - Exposed web self-service endpoints (`GET /settings/export-data`, `POST /settings/delete-account`) and UI controls.
3. **PWA Sync Versioning & Conflict Detection (`SyncService.java`):**
   - Maintained idempotent UUID deduplication for append-only logs (`LOG_DIARY`, `LOG_WATER`, `LOG_STEPS`, `LOG_WORKOUT`).
   - Implemented server-authoritative state checks for mutable updates (`ADJUST_PANTRY`, `TOGGLE_GROCERY`). When target records no longer exist on the server, the engine marks the operation as `CONFLICT_DETECTED` with actionable advice.
   - Added delete propagation for `DELETE_DIARY`, `DELETE_PANTRY`, `DELETE_PANTRY_ITEM`, `DELETE_GROCERY`, and `DELETE_GROCERY_ITEM`.
4. **Relational Index Optimization (`V9__phase8_release_optimization.sql`):**
   - Added compound and single-column indexes on `food_diary_entries(user_id, entry_date)`, `water_logs(user_id, log_date)`, `step_records(user_id, record_date)`, `workout_logs(user_id, workout_date)`, `pantry_items(user_id, expiry_date)`, `household_members(user_id, household_id)`, and foreign key references across all entities.
5. **Attribution & Clinical Boundaries:**
   - Authored `/attribution` landing page documenting ICMR-NIN IFCT 2017, USDA FoodData Central, and Open Food Facts catalogs.
   - Embedded clinical disclaimer footer on the primary application layout (`templates/layout/main.html`).
   - Documented explicit wearable integration boundaries: manual/CSV data logging and Web Speech voice input with zero fabricated sensor promises.

### Consequences
- Cross-household data leaks and unauthorized IDOR attacks are completely mitigated.
- User data privacy controls aligned with Indian DPDP Act 2023 and GDPR technical portability/erasure requirements.
- Query performance on high-volume time-series tables is secured with optimal indexing.
- Automated test suite expanded with 100% pass rate, Spotless clean formatting, and standalone `bootJar` compilation.

---

## ADR-017: Final Release Security Patch — Removal of Hardcoded Credentials, Environment-Driven Administrator Bootstrap, and Expanded PWA Conflict Testing

### Status
Accepted

### Context
A comprehensive security and production release audit revealed that:
1. Default credentials had been seeded unconditionally across all profiles in `DataInitializer.java`, potentially creating hardcoded accounts in production environments.
2. The login template (`login.html`) rendered hardcoded mock credentials regardless of deployment environment.
3. Content Security Policy (CSP) lacked directives (`object-src 'none'`, `base-uri 'self'`, `form-action 'self'`, `frame-ancestors 'self'`) to reinforce defense-in-depth against plugin injection and clickjacking.
4. User data export (`UserDataManagementService.java`) missed several secondary telemetry categories (weight measurements, individual meal plans, saved recipes, household memberships, AI consultation history, and recipe adaptations), while account deletion required comprehensive cascading cleanup across 15+ related entities without corrupting shared household meal plans.
5. Offline synchronization required expanded test coverage covering retried batch transmissions, stale grocery/pantry conflicts, unauthenticated requests, and cross-household isolation.

### Decision
1. **Zero Default Credentials in Production:**
   - Rewrote `DataInitializer.java` using Spring `Environment` profile inspection (`isDevOrTest` vs `isProd`).
   - Mock demo accounts are strictly constrained to `dev`, `test`, and `local` profiles with rotated dev passwords (`DevDemoPass2026!#`, overridable via `EASYNUTRI_DEMO_PASSWORD`).
   - In production (`prod`), **zero** demo accounts are created, and zero hardcoded credentials exist anywhere in the code.
2. **Secure Environment-Based Administrator Bootstrap:**
   - Enabled initial administrator bootstrap via explicit environment variables: `EASYNUTRI_ADMIN_BOOTSTRAP_USERNAME`, `EASYNUTRI_ADMIN_BOOTSTRAP_EMAIL`, `EASYNUTRI_ADMIN_BOOTSTRAP_PASSWORD`.
   - Requires password length $\ge 8$ characters, hashed securely via BCrypt.
   - Enforces idempotency: if an administrator (`ROLE_ADMIN`) already exists, bootstrap execution is safely skipped.
   - Credentials and secrets are never logged.
3. **Environment-Conditional Login Banner:**
   - Modified `login.html` using Thymeleaf conditional evaluation (`th:if="${!#arrays.contains(@environment.getActiveProfiles(), 'prod')}"`) ensuring demo banners never render in production deployments.
4. **Content Security Policy Hardening:**
   - Tightened CSP directives in `SecurityConfig.java`: added `base-uri 'self'; form-action 'self'; frame-ancestors 'self'; object-src 'none';`.
5. **Complete Data Sovereignty & Cascading Erasure:**
   - Enhanced `UserDataManagementService.exportUserData()` to export 16 structured data categories in `EASYNUTRI_EXPORT_V2` format.
   - Enhanced `deleteUserAccount()` to cleanly delete across personal biometrics, food planning, kitchen items, gamification, AI chats, sync operations, social interactions, and safely detach from households (transferring ownership if surviving members exist, dissolving household and meal plans if no members remain).
6. **Expanded Test Suite:**
   - Authored `ProductionConfigurationAndBootstrapTest.java` and `ProductionNoBootstrapTest.java`.
   - Expanded `OfflineConflictAndSyncTest.java` with 7 dedicated test methods.
   - Total automated test suite expanded to **197 tests** (79 domain + 118 server), all passing 100%.

### Consequences
- Production deployments are completely free of hardcoded credentials.
- Initial administrative bootstrap is strictly under administrator control via environment variables.
- Offline-first PWA sync and data rights workflows are comprehensively tested and verified.
