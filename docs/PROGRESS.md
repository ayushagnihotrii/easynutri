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
| **Phase 6** | **Community Recipe Blog, Moderation & Trust, Streaks & Badges** | **COMPLETE** | **All pure Java safety, gamification, moderation & MVC tests passing (81/81)** |
| **Phase 7** | **Family Profiles, Offline PWA Sync & Sensitive-Mode Hardening** | **COMPLETE** | **All pure Java domain, PWA sync, household isolation & MVC tests passing (99/99)** |
| **Phase 8** | **Final Integration, Security Audit, Performance Optimization & Release Verification** | **COMPLETE** | **206/206 tests passing, Spotless clean, bootJar packaged, legacy credential remediation verified, complete data portability & erasure audited** |

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

---

## Phase 6 Implementation Summary (Community Recipe Platform, Trust & Moderation, Gamification Complete)

### 1. Pure Java Domain Business Logic (`:domain`)
- **`ContentScreener` (`com.easynutri.domain.moderation`):**
  - Pure Java automated clinical rule screening for community content and recipe descriptions.
  - Intercepts and blocks hazardous medical claims ("cures diabetes", "replaces insulin", "cures cancer", "treats hypertension", "throw away medication").
  - Intercepts and blocks starvation / eating disorder promotion ("fasting 7 days", "starve yourself", "purge", "extreme calorie restriction").
  - Evaluates commercial spam, offensive text, and flags borderline items for mandatory human clinical review (`BLOCK`, `REVIEW_REQUIRED`, `PASS`).
- **`StreakCalculator` (`com.easynutri.domain.gamification`):**
  - Compassionate habit streak calculation algorithm with a 1-day forgiveness grace period.
  - Prevents emotional abandonment and user demoralization when a single day is missed.
  - Computes `currentStreak`, `longestStreak`, `loggedToday`, and `gracePeriodActive` across arbitrary logging timestamps.
- **`DietaryTagClassifier` (`com.easynutri.domain.recipe`):**
  - Objectively validates dietary tags based on verified ingredient profiles and nutritional thresholds per serving.
  - Evidence-backed classifications: `VEGETARIAN`, `VEGAN`, `JAIN` (no root vegetables/alliums), `DAIRY_FREE`, `GLUTEN_FREE`, `HIGH_PROTEIN` ($\ge 15\text{g}$), `HIGH_FIBER` ($\ge 5\text{g}$), `LOW_SODIUM` ($\le 140\text{mg}$), `IRON_SOURCE`, `CALCIUM_SOURCE`.

### 2. Database Schema & Persistence (`:server`)
- **`V7__phase6_schema.sql`:**
  - Extended `users` table with `bio`, `avatar_url`, `is_verified_pro`, and `pro_category`.
  - Added tables: `community_recipes`, `community_recipe_ingredients`, `community_likes`, `community_comments`, `community_follows`.
  - Added moderation tables: `content_reports`, `moderation_actions`, `professional_verifications`.
  - Added gamification tables: `user_streaks`, `badge_definitions`, `user_badges`, `challenges`, `challenge_participations`.
  - Unique constraints prevent duplicate likes, duplicate badge awards, self-following, and duplicate challenge entries.

### 3. Application Services & Web Presentation (`:server`)
- **`CommunityService` & `CommunityWebController` (`/community`):**
  - Social recipe feeds (`latest`, `popular`, `following`, search).
  - Dynamic recipe composer (`/community/create`) with canonical food ingredient mapping, automatic pure Java macro calculation per serving, and automated pre-publish safety screening.
  - Detailed recipe views (`/community/recipes/{id}`) with verified nutritional facts strip, evidence-backed dietary tags, ingredients checklist, likes toggle, and comment threads.
  - Public creator profiles (`/community/users/{username}`) with follower counts and verified health professional badges.
- **`ModerationService` & `ModerationWebController` (`/admin/moderation`):**
  - Clinical moderation queue for reported recipes and comments.
  - Professional credential verification workflow for Registered Dietitians, Nutritionists, and Medical Doctors with human-only badge grants.
  - Detailed audit trail recording moderator actions (`APPROVE`, `REJECT`, `HIDE`, `RESTORE`, `REMOVE`, `SUSPEND_USER`).
- **`GamificationService` & `AchievementsWebController` (`/achievements` & `/challenges`):**
  - Compassionate streaks dashboard tracking Diary Logging, Hydration, Step Targets, and Home Cooking with 1-day grace indicators.
  - Health badge milestones collection with automated qualification (`FIRST_MEAL_LOGGED`, `SEVEN_DAY_DIARY`, `HYDRATION_HERO`, `STEP_EXPLORER`, `HOME_CHEF`, `ZERO_PANTRY_WASTE`, `COMMUNITY_CREATOR`).
  - Community wellness challenges with live progress tracking and join capabilities.
  - Strict compliance with Sensitive-User Mode: suppresses competitive rankings, weight loss metrics, or deficit comparisons.

### 4. Verification & Testing
- **100% Test Pass Rate (81/81 server tests + all domain tests):**
  - `CommunityWebControllerTest`: verifies feeds, recipe publishing with exact macros, safety screening blocking medical claims, like toggle, comment addition/deletion, creator profile.
  - `ModerationWebControllerTest`: verifies content reporting, automated AI verdict, admin moderation queue, decision execution, and pro verification workflow.
  - `AchievementsWebControllerTest`: verifies compassionate habit streaks, 1-day grace period, automated badge awarding, and community challenges.
- **Spotless Code Style Applied Cleanly.**
- **Production `bootJar` Executable Built Successfully.**

---

## Phase 7 Implementation Summary (Family Profiles, Offline-First PWA Synchronization, Global Safety Filters & Sensitive-Mode Hardening Complete)

### 1. Pure Java Domain Layer (`:domain`)
- **`HouseholdDietaryAggregator` (`com.easynutri.domain.household`):**
  - Pure Java diet hierarchy resolution: Jain > Vegan > Vegetarian > Non-Veg.
  - Union of allergies across all participating household members.
  - Recipe safety evaluation (`SafetyGate` integration) blocking unsafe meals for family dinners.
  - Portion calculation & dynamic scaling based on member age and daily energy targets (adult vs child proportions).
- **`DisplayPolicy` Hardening (`com.easynutri.domain.policy`):**
  - Added `formatBmi`, `formatWeightChange`, and `getCheckInMessage`.
  - Suppresses all raw weights (`-- kg (Masked)`), BMI numbers (`Healthy Balance (Masked)`), and caloric deficits (`Gentle Energy Balance`, `Balanced`, `Well Balanced`, `Sustained Energy`).
  - Unit verified with 100% coverage in `DisplayPolicyTest` and `HouseholdDietaryAggregatorTest`.

### 2. Database Schema Migration (`:server`)
- **`V8__phase7_schema.sql`:**
  - Added `households` table (owner reference, household name, timestamps).
  - Added `household_members` table (composite unique `household_id, user_id`, relationship, role `HOUSEHOLD_OWNER`, `HOUSEHOLD_ADMIN`, `MEMBER`, `DEPENDENT`, supervisor user reference, avatar color).
  - Added `household_meal_plans` table (scheduled family meals with recipe, date, and meal slot).
  - Added `sync_operations` table (idempotent audit ledger with unique `idempotency_key`, action type, payload, status, and client timestamp).

### 3. Application Services & Web Presentation (`:server`)
- **`ActiveProfileContextService`:**
  - Manages session-scoped active profile context (`ACTIVE_PROFILE_ID`).
  - Enforces strict server-side verification: users can only switch to profiles belonging to their household.
- **`HouseholdService` & `HouseholdWebController` (`/family`):**
  - Household creation and multi-member management.
  - Member addition creates managed user accounts with computed BMR, TDEE, and macro targets.
  - Household dashboard displaying member cards, dietary tags, allergies, and active profile indicator.
  - Quick profile switcher in session.
- **`HouseholdMealPlanService` & `/family/meal-planner`:**
  - Multi-member meal selection with live combined safety screening.
  - Automated individual portion scaling (grams, calories, protein, carbs, fat per member).
  - One-click scheduling synchronizing individual member meal plans (`meal_plan_items`).
- **`SyncService` & `SyncApiController` (`/api/v1/sync/batch`):**
  - Offline-first idempotent synchronization queue.
  - Client UUID idempotency keys prevent duplicate operations on network retries.
  - Operations supported: `LOG_DIARY_ITEM`, `LOG_WATER`, `LOG_STEPS`, `LOG_WORKOUT`, `UPDATE_GROCERY`, `UPDATE_PANTRY`, `DELETE_DIARY`.
  - Enforces active profile security: sync actions targeting unauthorized user IDs are immediately rejected (`SECURITY_REJECTED`).

### 4. PWA Frontend & Localization (`:server`)
- **Service Worker v2 (`sw.js`):**
  - Updated cache version to `easynutri-v2`.
  - Strictly excludes private authenticated endpoints (`/api/v1/**`, `/reports/export-pdf`) from browser cache to protect personal health data.
- **IndexedDB Client Queue (`static/js/offline-db.js` & `static/js/offline-sync.js`):**
  - Queues write operations when browser is offline.
  - Automatically flushes queued actions with CSRF token when network connectivity is restored (`window.addEventListener('online')`).
  - Topbar dynamic status pill (`#networkSyncBadge`) reflecting live state (Online, Offline Mode, Syncing..., All changes synced).
- **English & Hindi Localization:**
  - Configured `LocaleConfig` with `CookieLocaleResolver` and `LocaleChangeInterceptor` (`?lang=en`, `?lang=hi`).
  - Created `messages.properties` and `messages_hi.properties` bundles covering navigation, sync alerts, compassionate mode, and household planning.
  - Topbar one-click language selector (`EN | हिं`).

### 5. Automated Verification & Test Coverage
- **100% Test Pass Rate across all 99 automated tests in `:server` and `:domain`:**
  - `HouseholdWebControllerTest`: verifies family dashboard, household creation, member addition, active profile switching, and cross-household isolation security.
  - `HouseholdMealPlanningTest`: verifies multi-member allergen union filtering, diet hierarchy enforcement, individual portion scaling, and meal plan synchronization.
  - `SyncApiControllerTest`: verifies batch sync, idempotency duplicate prevention, and unauthorized profile access rejection.
  - `SensitiveModeHardeningTest`: verifies complete masking of weights, BMI, calories, deficits, and non-judgmental check-ins.
  - `LocalizationAndAccessibilityTest`: verifies English & Hindi message resolution, cookie persistence, and topbar accessibility.
- **Spotless Code Formatting Applied Cleanly (`spotlessApply`).**
- **Production `bootJar` Built Successfully.**

---

## Phase 8 Implementation Summary (Final Integration, Security Audit, Performance Optimization & Release Readiness Complete)

### 1. Security Hardening & Authorization Controls
- **BOLA / IDOR Mitigation (`HouseholdApiController.java` & `HouseholdService.java`):**
  - Added strict household membership verification (`isMemberOfHousehold(Long userId, Long householdId)`).
  - Cross-household recommendation queries, meal plans, or profile adjustments reject unauthorized callers with HTTP 403 Forbidden.
- **REST Exception Handling (`GlobalRestExceptionHandler.java`):**
  - Mapped `SecurityException` -> HTTP 403 Forbidden.
  - Mapped `IllegalArgumentException` -> HTTP 400 Bad Request.
- **HTTP Security Headers & Protection (`SecurityConfig.java`):**
  - Content Security Policy (CSP): `default-src 'self'; script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; img-src 'self' data: https:; connect-src 'self'; frame-ancestors 'self'; base-uri 'self'; form-action 'self'; object-src 'none';`.
  - `X-Content-Type-Options: nosniff`.
  - `X-Frame-Options: SAMEORIGIN` (prevents clickjacking).
  - `Referrer-Policy: strict-origin-when-cross-origin`.
  - Session fixation protection: `sessionManagement().sessionFixation().migrateSession()`.
  - Enforced `ROLE_ADMIN` authorization on all `/admin/**` routes.
- **Production Credential Security & Administrator Bootstrap (`DataInitializer.java`):**
  - **Zero Default Accounts in Production:** In `prod` profile, no demo or default accounts are created.
  - **Environment-Driven Administrator Bootstrap & Legacy Remediation:** Administrator provisioning requires explicit environment variables (`EASYNUTRI_ADMIN_BOOTSTRAP_USERNAME`, `EASYNUTRI_ADMIN_BOOTSTRAP_EMAIL`, `EASYNUTRI_ADMIN_BOOTSTRAP_PASSWORD`). Password length is enforced ($\ge 8$ characters) and hashed via BCrypt. If deployed against an existing database where an administrator account contains the deprecated default credential (`AdminSecure2026!`), it is automatically rotated if a new password is provided or automatically disabled (`enabled = false`) if not. `EASYNUTRI_ADMIN_FORCE_PASSWORD_RESET=true` enables non-destructive credential rotation.
  - **Development Mode Constraint:** Mock `demo` user is strictly restricted to `dev`, `test`, and `local` profiles with rotated dev password. In `prod`, legacy `demo` accounts are automatically disabled. Login page credentials helper conditionally suppresses mock credentials in production.

### 2. Privacy & Data Rights Controls (DPDP Act & GDPR Technical Architecture)
- **`UserDataManagementService.java`:**
  - `exportUserData(Long userId)`: Generates full structured JSON export containing 20+ comprehensive data categories:
    1. Account credentials metadata (zero passwords/hashes leaked)
    2. Biometrics profile
    3. Food diary entries (100% historical snapshot items)
    4. Water logs
    5. Hydration preferences
    6. Workout logs
    7. Step records
    8. Weight history
    9. Pantry inventory
    10. Grocery checklist items
    11. Meal plans
    12. Recipe collections
    13. Recipe adaptations
    14. Cooking sessions
    15. Habit streaks and earned badges
    16. Challenge participations
    17. User notifications
    18. Community activity (authored recipes, comments, likes, follower/following relationships)
    19. Professional verification requests and admin review statuses
    20. AI Conversations with **full message transcripts** (`role`, `content`, `executedTools`, `safetyVerdict`, `createdAt`)
  - `deleteUserAccount(Long userId, String passwordConfirmation)`: Verifies user password and performs clean cascading erasure of personal data tables, AI messages, sync operations, notifications, community comments, likes, and follows. Safely transfers or dissolves household associations without corrupting shared household meal plans.
  - *Statutory Notice:* Technical mechanisms provide data portability and erasure controls; formal statutory compliance requires independent jurisdictional legal assessment.
- **Web Interface (`ProfileWebController.java` & `templates/settings/index.html`):**
  - `GET /settings/export-data`: Downloads `easynutri-export-{username}-{date}.json`.
  - `POST /settings/delete-account`: Secure deletion with modal confirmation, password challenge, and session invalidation.

### 3. PWA Offline Conflict Detection & Delete Propagation
- **Conflict Resolution (`SyncService.java`):**
  - Maintains append-only idempotent deduplication via UUID keys for telemetry (`LOG_DIARY`, `LOG_WATER`, `LOG_STEPS`, `LOG_WORKOUT`).
  - Added server-authoritative state validation for mutable actions (`ADJUST_PANTRY`, `TOGGLE_GROCERY`). If the target entity was deleted or modified externally, marks the operation as `CONFLICT_DETECTED` with advisory instructions.
  - Added delete propagation for `DELETE_DIARY`, `DELETE_PANTRY`, `DELETE_PANTRY_ITEM`, `DELETE_GROCERY`, and `DELETE_GROCERY_ITEM`.
  - Session expiration and profile isolation safeguards block unauthorized sync operations.

### 4. Database Query & Performance Optimization
- **`V9__phase8_release_optimization.sql`:**
  - Added 25+ database indexes on high-frequency query paths and foreign keys:
    - `idx_users_username`, `idx_users_email`
    - `idx_profiles_user`
    - `idx_diary_user_date`, `idx_diary_items_entry`
    - `idx_water_user_date`
    - `idx_steps_user_date`
    - `idx_workout_user_date`
    - `idx_pantry_user_expiry`
    - `idx_grocery_user_checked`
    - `idx_meal_plan_user_date`
    - `idx_community_recipes_user_created`, `idx_community_comments_recipe`
    - `idx_content_reports_status`
    - `idx_household_members_lookup`, `idx_household_meal_plans_lookup`
    - `idx_sync_ops_user_status`

### 5. Nutrition Sourcing, Clinical Disclaimer & Localization
- **Attribution Page (`/attribution` & `templates/attribution/index.html`):**
  - Full documentation of primary food composition sources:
    - ICMR-National Institute of Nutrition (IFCT 2017)
    - USDA FoodData Central (SR Legacy & Foundation Foods)
    - Open Food Facts (FSSAI packaging & barcode catalog)
  - Emergency medical boundary callout (mandating 112 / 911 for acute crises).
- **Clinical Disclaimer Footer (`templates/layout/main.html`):**
  - Embedded disclaimer on all views: *"EasyNutri provides nutritional estimates and dietary suggestions for general wellness purposes only and is not a substitute for clinical medical diagnosis, treatment, or prescription. For acute medical symptoms, call 112 / 911 immediately."*
- **100% Localization Coverage (`sidebar.html`, `mobile-nav.html`):**
  - Fully translated navigation links and accessibility tags in English and Hindi.
- **Honest Integration Transparency:**
  - Documented manual & CSV entry limitations for wearable health tracking with zero simulated Bluetooth/Health Connect daemons.
  - Documented Web Speech API browser availability boundaries.

### 6. Automated Verification & Release Deliverables
- **206/206 Automated Tests Passing (100% Pass Rate, 0 Failures, 0 Skipped):**
  - Exact reconciliation based directly on Gradle JUnit XML execution reports (`build/test-results/test`):
    - `:domain` Module: **79 tests** across 26 test classes.
    - `:server` Module: **120 tests** across 37 test classes.
    - `:tools` Module: **7 tests** across 2 test classes (`CliSmokeTest` = 1, `SchemaValidationTest` = 6).
    - **Total Tests Executed:** **206 tests** (206 Passed, 0 Failed, 0 Skipped).
    *(Note: An earlier narrative summary reported sub-totals under thematic categories that overlapped, creating an apparent mismatch; reading the exact raw JUnit XML testsuite counters establishes the definitive project total as 206).*
  - Comprehensive Release & Security Test Suites:
    - `LegacyCompromisedAccountRemediationTest.java`: verifies automatic disabling of legacy compromised accounts when no new credentials are provided, automatic rotation when `EASYNUTRI_ADMIN_BOOTSTRAP_PASSWORD` is supplied, and explicit rotation via `EASYNUTRI_ADMIN_FORCE_PASSWORD_RESET`.
    - `SecurityAndAuthorizationTest.java`: verifies security headers, BOLA prevention, admin role enforcement, unauthenticated redirection.
    - `ProductionConfigurationAndBootstrapTest.java`: verifies zero demo users in production, BCrypt-hashed admin bootstrap via environment variables.
    - `ProductionNoBootstrapTest.java`: verifies completely clean database start when bootstrap variables are absent.
    - `OfflineConflictAndSyncTest.java`: verifies append-only idempotency, retried batches, stale grocery/pantry conflicts, delete propagation, session expiration, and profile isolation.
    - `UserDataManagementTest.java`: verifies 20+ category JSON data export including AI chat messages, notifications, community activity, password confirmation, clean cascading erasure, and safe household ownership transfer.
    - `NutritionAttributionAndSafetyTest.java`: verifies attribution routing, emergency safety gate, clinical disclaimer presence.
- **Code Formatting:** Spotless clean (`spotlessCheck` / `spotlessApply` 100% compliant with Google Java Format).
- **Executable Package:** Standalone production executable JAR built at `server/build/libs/server.jar` (71.9 MB).
- **Overall Project Completion:** **100% (All 8 Phases, Final Security Patch, Deployment Verification & 34 Features Complete and Production Ready).**
