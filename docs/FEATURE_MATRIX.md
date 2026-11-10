# EasyNutri — Full-Stack Web Application Feature Matrix

This document provides the definitive mapping of all 34 EasyNutri features (33 numbered + Smart Pantry Suggestions), defining their full-stack web and PWA architecture, browser capabilities, fallback mechanisms, implementation phases, and current verification status.

---

## Group A: Foundation

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase | Status | Verification Evidence |
|---|---|---|---|---|---|:---:|---|
| **1** | **User Profile & Health Setup** | 3-step interactive onboarding wizard (`/onboarding`) and settings editor (`/settings`). | `UserProfileEntity`, `UserProfileService`, `ProfileContext`. | Form draft preservation; syncs with server. | Phase 1 | **COMPLETE** | Tested in `OnboardingWebControllerTest` and `AuthWebControllerTest`. |
| **2** | **BMI, BMR & Calorie Goals** | Dynamic dashboard metric cards with WHO classification and clinical guidance. | `BmiCalculator`, `DefaultBurnCalculator`, `NutritionGoalCalculator`. | Pure Java calculations evaluated on server, rendered on client. | Phase 1 | **COMPLETE** | Unit tested in `BmiCalculatorTest` and `NutritionGoalCalculatorTest`. |
| **3** | **Food & Nutrition Database** | Searchable UI (`/foods`) with category filters (Grains, Pulses, Dairy, Veggies, Fruits). | JPA `FoodRepository` + IFCT 2017 & USDA FoodData Central catalog. | Cached static catalog in browser storage / Service Worker. | Phase 1 | **COMPLETE** | Tested in `FoodWebControllerTest`. Sourced from IFCT & USDA. |
| **4** | **Recipes with Serving Scaler** | Interactive recipe detail card (`/recipes/{id}`) with dynamic `+` / `-` portion scaler. | `RecipeService.computeScaledNutrition()`, `DefaultRecipeAdapter`. | Scaled amounts and nutrition recalculated directly from DB rows. | Phase 1 | **COMPLETE** | Tested in `DefaultRecipeAdapterTest` and `FoodWebControllerTest`. |
| **5** | **Meal Categories** | Breakfast, Lunch, Dinner, Snack filter pills and meal roles (Main, Side, Salad). | Enumerated meal slots in `RecipeEntity` and `RecipeCandidate`. | Cached filter metadata. | Phase 1 | **COMPLETE** | Tested in `FoodWebControllerTest` (`findByMealSlot`). |
| **6** | **Health Condition Planner** | Clinical condition checklists (Hypertension, Type 2 Diabetes, CKD, GERD). | `DefaultRulesEngine`, `RuleVerdict`, `SafetyResult`. | Strict server-side rule evaluation; safe defaults cached locally. | Phase 1 | **COMPLETE** | Unit tested in `DefaultRulesEngineTest`. |
| **—** | **Smart Pantry Suggestions** | Recommended staple checklist grouped by category with clinical rationales. | `SmartPantryRecommender.recommendStaples()`. | Filtered by user allergies and conditions before display. | Phase 1 | **COMPLETE** | Unit tested in `SmartPantryRecommenderTest`; rendered on Dashboard. |

---

## Group B: Finding Food

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase | Status | Verification Evidence |
|---|---|---|---|---|---|:---:|---|
| **7** | **Ingredient-Based Finder** | Dedicated `/ingredients` UI with autocomplete, chips, match %, readiness badges (`Ready Now`, `Ready With a Swap`, `Missing 1`, `Missing 2`, `Missing 3+`), and SafetyGate filtering. | `DefaultRecipeMatcher.matchRecipesDetailed()`, `IngredientService`. | Client-side chip manager and fallback form submit. | Phase 2 | **COMPLETE** | Verified in `EnhancedRecipeMatcherTest` and `IngredientWebControllerTest`. |
| **8** | **Multi-Input Ingredient Entry** | 5 input channels: Typed, Web Speech API Voice, Barcode lookup, Photo scan, and Smart OCR. | `IngredientResolver`, `DefaultFoodRecognitionProvider`, `DefaultOcrProvider`, `BarcodeLookupService`. | **Typed input is always the primary fallback** with zero AI hallucinations. | Phase 2 | **COMPLETE** | Verified in `DefaultIngredientResolverTest` and `IngredientApiControllerTest`. |
| **9** | **Leftover Mode** | Interactive leftover toggle with live ICMR/FSSAI microbiological safety checks (>4h room temp / >72h fridge BLOCK). | `LeftoverSafetyEvaluator`, `RecipeMatcher` leftover bonus scoring. | Real-time client & server safety validation preventing hazardous leftover consumption. | Phase 2 | **COMPLETE** | Verified in `LeftoverSafetyEvaluatorTest` and `IngredientWebControllerTest`. |
| **10** | **Deficiency Helper** | Dedicated `/deficiency-helper` page covering 10 nutrients (Iron, B12, D, Calcium, Protein, Fibre, Folate, Zinc, C, Magnesium) with ICMR 2024 % RDA and absorption pairing tips. | `DefaultDeficiencyService`, `NutrientInfo`. | Clinical food sources ranked by nutrient density per 100g. | Phase 2 | **COMPLETE** | Verified in `DefaultDeficiencyServiceTest` and `DeficiencyWebControllerTest`. |

---

## Group C: Planning

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase | Status | Verification Evidence |
|---|---|---|---|---|---|:---:|---|
| **11** | **Personalized Weekly Planner** | 7-day responsive interactive planner (`/planner`) with previous/next week navigation, 1-tap safe meal swap modal, servings scaling, lock toggle, and complete toggle. | `DefaultMealPlanner`, `MealPlanItemEntity`, `MealPlanService`, `MealPlannerWebController`. | Database persistence; locked items preserved during plan generation; calendar view cached. | Phase 3 | **COMPLETE** | Verified in `DefaultMealPlannerTest` and `MealPlannerWebControllerTest`. |
| **12** | **Bookmarks & Collections** | Dedicated collections interface (`/saved-recipes`) with custom collection tabs, personal notes editor, search, and meal-slot filters. | `SavedRecipeEntity`, `RecipeCollectionEntity`, `SavedRecipeService`, `RecipeCollectionService`. | Relational database persistence with instant AJAX state toggling. | Phase 3 | **COMPLETE** | Verified in `SavedRecipesWebControllerTest` and `SavedRecipeApiController`. |
| **13** | **Pantry Tracker & Expiry Alerts** | Full CRUD inventory manager (`/pantry`), storage location filtering (Fridge/Freezer/Cupboard), quick `+`/`-` gram adjustments, and "Cook Before It Expires" waste prevention recommendations. | `PantryItemEntity`, `PantryService`, `PantryWebController`, `PantryExpiryScheduler`. | Scheduled in-app notifications and real-time database expiry metrics. | Phase 3 | **COMPLETE** | Verified in `PantryWebControllerTest` and `PantryService`. |
| **14** | **Automatic Grocery List** | Dedicated `/grocery` checklist with plan-to-pantry reconciliation (`Required - In Pantry = To Buy`), category grouping, and direct "Move to Pantry" transfer. | `DefaultGroceryBuilder`, `GroceryItemEntity`, `GroceryService`, `GroceryWebController`. | Preserves manual additions and checked items during auto-reconciliation. | Phase 3 | **COMPLETE** | Verified in `DefaultGroceryBuilderTest`, `GroceryServiceTest`, and `PlanPantryReconciliationTest`. |
| **15** | **Cooking Mode** | Dedicated distraction-free cooking interface (`/cook/{id}`), scaled ingredients checklist, auto-detected step timers, Web Speech API voice control & TTS, and idempotent pantry deduction. | `CookingSessionEntity`, `CookingService`, `CookingWebController`, `PantryService.deductIngredients()`. | Fully offline functional in browser; prevents duplicate pantry inventory deduction via `pantry_deducted` flag. | Phase 3 | **COMPLETE** | Verified in `CookingWebControllerTest` and `CookingService`. |

---

## Group D: Tracking

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase | Status | Verification Evidence |
|---|---|---|---|---|---|:---:|---|
| **16** | **Food Diary & Macro Tracking** | Daily timeline (`/diary`) with Breakfast/Lunch/Dinner/Snack categories, date picker, quick logging modal, 1-click meal duplication, and macro progress bars. | `DiaryAggregator`, `DiaryItemSnapshot`, `FoodDiaryEntryEntity`, `FoodDiaryItemEntity`, `DiaryService`, `DiaryWebController`. | Preserves immutable historical nutritional snapshots evaluated at log time. Protected under sensitive-user mode. | Phase 4 | **COMPLETE** | Verified in `DiaryAggregatorTest`, `DiaryWebControllerTest`, and `DiaryApiController`. |
| **17** | **Step Counter** | Daily step tracker (`/tracking`), daily step goal progress meter, and quick step editor. Zero fabricated live sensor data. | `StepRecordEntity`, `ActivityBurnEngine`, `ActivityTrackingService`, `ActivityWebController`. | Manual entry & wearable logging; verified against database constraints without fake readings. | Phase 4 | **COMPLETE** | Verified in `ActivityBurnEngineTest` and `ActivityWebControllerTest`. |
| **18** | **Calories Burned Counter** | MET activity burn logger (`/tracking`), activity type & intensity selectors, and daily energy balance budget (Food In vs BMR + Workouts + Net Steps). | `ActivityBurnEngine`, `DefaultBurnCalculator`, `WorkoutLogEntity`, `ActivityTrackingService`. | Mathematical clinical MET equations with intelligent step deduplication preventing double-counted ambulatory burn. | Phase 4 | **COMPLETE** | Verified in `ActivityBurnEngineTest` and `ActivityWebControllerTest`. |
| **19** | **Water Intake Tracker** | Hydration tracking (`/hydration`) with visual bottle progress, quick portions (+150ml, +250ml, +350ml, +500ml), custom ml entry, drink history, and reminder preferences. | `DefaultHydrationTargetProvider`, `HydrationPreferenceEntity`, `WaterLogEntity`, `HydrationService`, `HydrationWebController`. | Clinician-reviewed targets with provisional notes for CHF and CKD conditions. | Phase 4 | **COMPLETE** | Verified in `HydrationTargetProviderTest` and `HydrationWebControllerTest`. |
| **20** | **Progress Charts & Reports** | Multi-day analytics (`/reports`) with 7d/30d/90d ranges, interactive Chart.js graphs, longitudinal weight history, rule-based clinical insights, and 1-click downloadable PDF reports. | `DefaultInsightEngine`, `PdfReportService` (OpenPDF), `ProgressAnalyticsService`, `ProgressWebController`. | Requires ≥3 days of data before drawing conclusions; redacts weights and calories when sensitive-user mode is active. | Phase 4 | **COMPLETE** | Verified in `DefaultInsightEngineTest`, `ProgressWebControllerTest`, and `PdfReportServiceTest`. |

---

## Group E: Intelligence — AI & Safety

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase | Status | Verification Evidence |
|---|---|---|---|---|---|:---:|---|
| **21** | **AI Nutrition Chat Assistant** | Multi-turn chat interface (`/assistant`) with sidebar history, suggested prompt chips, voice dictation, and tool execution audit cards. | `AiOrchestrator`, `AiToolExecutor`, `AiContextBuilder`, `AiToolRegistry` (13 controlled tools). | Server-side key isolation. **Deterministic clinical demo mode** when no external key is configured. Zero hallucinated numbers. | Phase 5 | **COMPLETE** | Verified in `AssistantWebControllerTest` and `AssistantApiController`. |
| **22** | **AI Proposes, Java Rules Verify & Safety Gate** | Hard-boundary clinical interceptor for all AI proposals and emergency screener. | Pure Java `SafetyGate`, `EmergencyDetector`, `AiResponseValidator`. | **Zero AI bypass.** Emergency detector catches acute medical crises (anaphylaxis, cardiac, stroke, poisoning) and mandates 112/911 dispatch. | Phase 5 | **COMPLETE** | Verified in `SafetyGatePipelineTest` and `EmergencyDetectorTest`. |
| **23** | **AI Recipe Adaptation & Substitutions** | Interactive recipe adaptation interface (`/recipes/adapt/{id}`) with dietary constraint selector (`VEGAN`, `JAIN`, `DAIRY_FREE`, `LOW_SODIUM`, `CUSTOM`), ingredient diffs, and side-by-side comparison. | `RecipeAdaptationService`, `RecipeAdaptationEntity`, `RecipeAdaptationRepository`. | Pure Java domain recalculation using canonical database food rows. Leaves base recipe untouched. | Phase 5 | **COMPLETE** | Verified in `RecipeAdaptationTest` and `RecipeAdaptationWebController`. |
| **24** | **Personalised Insights & Weekly Check-ins** | Weekly check-in overview (`/insights`) with categorized observations (Protein, Hydration, Activity, Pantry, Consistency) and actionable buttons. | `WeeklyInsightService`, `WeeklyInsightEntity`, `WeeklyInsightRepository`. | Requires longitudinal logs; non-judgmental and encouraging tone. Fully sensitive-user mode compliant. | Phase 5 | **COMPLETE** | Verified in `AssistantWebControllerTest` and `WeeklyInsightService`. |

---

## Group F: Community & Growth

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase | Status | Verification Evidence |
|---|---|---|---|---|---|:---:|---|
| **25** | **Community Recipe Blog** | Social recipe feed (`/community`), recipe composer with exact canonical food mapping (`/community/create`), single recipe view (`/community/recipes/{id}`), comments, likes, and creator profiles (`/community/users/{username}`). | `CommunityRecipeEntity`, `CommunityRecipeIngredientEntity`, `CommunityLikeEntity`, `CommunityCommentEntity`, `CommunityFollowEntity`, `CommunityService`, `CommunityWebController`, `CommunityApiController`. | Pure Java domain calculations compute exact per-serving macros and evidence-backed dietary tags from canonical ingredients. | Phase 6 | **COMPLETE** | Verified in `CommunityWebControllerTest`, `DietaryTagClassifierTest`, and `CommunityApiController`. |
| **26** | **Moderation & Trust System** | Content reporting modal, clinical moderation console (`/admin/moderation`), and professional credential verification (Registered Dietitians, Nutritionists, Doctors). | `ContentScreener`, `ContentReportEntity`, `ModerationActionEntity`, `ProfessionalVerificationEntity`, `ModerationService`, `ProfessionalVerificationService`. | Automated pure Java screening blocks hazardous claims ("cures diabetes", "starvation") before persistence. Human-in-the-loop review for pro badges. | Phase 6 | **COMPLETE** | Verified in `ContentScreenerTest` and `ModerationWebControllerTest`. |
| **27** | **Streaks, Badges & Challenges** | Compassionate habit streaks (`/achievements`), unlocked badges collection, and community wellness challenges (`/challenges`). | `StreakCalculator` (1-day grace period), `UserStreakEntity`, `BadgeDefinitionEntity`, `UserBadgeEntity`, `ChallengeEntity`, `ChallengeParticipationEntity`, `GamificationService`. | Strictly non-judgmental; excludes competitive weight-loss or caloric deficit leaderboards. Fully sensitive-user mode compliant. | Phase 6 | **COMPLETE** | Verified in `StreakCalculatorTest` and `AchievementsWebControllerTest`. |
| **28** | **Streak Counter (Compassionate Grace)** | Automated habit tracking across Diary Logging, Hydration, Step Targets, and Home Cooking with 1-day forgiveness grace period. | `StreakCalculator`, `UserStreakEntity`, `GamificationService`. | Prevents emotional user demoralization upon missing a single day. | Phase 6 | **COMPLETE** | Verified in `StreakCalculatorTest` and `AchievementsWebControllerTest`. |
| **29** | **Badges & Milestones** | Achievement milestones (`FIRST_MEAL_LOGGED`, `SEVEN_DAY_DIARY`, `HYDRATION_HERO`, `STEP_EXPLORER`, `HOME_CHEF`, `ZERO_PANTRY_WASTE`, `COMMUNITY_CREATOR`). | `GamificationService.checkAndAwardBadges()`, `BadgeDefinitionEntity`, `UserBadgeEntity`. | Automated background qualification from database activities without duplicate awards. | Phase 6 | **COMPLETE** | Verified in `AchievementsWebControllerTest`. |

---

## Group G: Administration, Family & Platform

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase | Status | Verification Evidence |
|---|---|---|---|---|---|:---:|---|
| **30** | **Family Profiles** | Switchable multi-profile selector (Spouse, Child, Elderly Parent). | `FamilyMemberEntity` in JPA. | Profile contexts cached in browser for fast offline switching. | Phase 7 | NOT_STARTED | Scaffolded view at `/family`. |
| **31** | **Admin Dashboard** | Clinical rule editor, food database manager, and user moderation. | Spring Security `ROLE_ADMIN` protected routes. | Requires active administrator session. | Phase 7 | NOT_STARTED | Planned for Phase 7. |
| **32** | **Responsive Layout** | CSS Grid & Flexbox adaptive layouts (Desktop, Tablet, Mobile) with 8pt grid. | Thymeleaf layout templates with unified CSS variables. | Fully local CSS stylesheets without CDN dependencies. | Phase 0-7 | **COMPLETE** | Verified on Mobile (375px), Tablet (768px), and Desktop (1440px). |
| **33** | **Progressive Web App (PWA)** | Web App Manifest, Service Worker caching, and "Add to Home Screen". | `sw.js`, `manifest.webmanifest`, `offline.html`. | Full offline fallback page and cached asset delivery. | Phase 0-7 | **COMPLETE** | Verified in browser audit; manifests and service worker active. |
| **34** | **Sensitive-User Mode** | One-click toggle masking calorie numbers with qualitative badges. | CSS class `.sensitive-mode` driven by `localStorage` & profile state. | Works completely client-side in real time across all views. | Phase 1 | **COMPLETE** | Tested on `/dashboard`, `/foods`, and `/ingredients`. |
