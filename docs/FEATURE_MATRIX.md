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
| **7** | **Ingredient-Based Finder** | Multi-tag ingredient input with match percentage badge. | `DefaultRecipeMatcher.matchRecipes()`. | Local cache of recipes matched locally or via API. | Phase 2 | **IN_PROGRESS** | Domain algorithm implemented & tested in `DefaultRecipeMatcherTest`. |
| **8** | **Multi-Input Ingredient Entry** | 5 input channels: Typed, Voice, Barcode, Photo, and OCR. | Replaceable `IngredientIngestionService` interfaces. | **Typed input is always the primary fallback** when browser APIs are absent. | Phase 2 | NOT_STARTED | Planned for Phase 2. |
| **9** | **Leftover Mode** | Pantry cleanup filter prioritizing expiring produce. | `RecipeMatcher.calculateMatchScore()` with expiry bonus. | Local shelf-life calculator; manual date override. | Phase 2 | NOT_STARTED | Planned for Phase 2. |
| **10** | **Deficiency Helper** | Micronutrient alerts (Iron, Calcium, Vitamin B12, Vitamin D). | `DeficiencyService.findFoodsRichIn()`. | Clinically safe food recommendations cached offline. | Phase 2 | NOT_STARTED | Planned for Phase 2. |

---

## Group C: Planning

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase | Status | Verification Evidence |
|---|---|---|---|---|---|:---:|---|
| **11** | **Personalized Weekly Planner** | 7-day responsive planner grid with drag/drop & slot selection. | `MealPlanner.generateWeeklyPlan()`. | Full 7-day schedule cached in IndexedDB; queued sync. | Phase 3 | NOT_STARTED | Planned for Phase 3. |
| **12** | **Bookmarks & Collections** | Favorite recipe collections with custom tags. | JPA `BookmarkEntity` & `CollectionEntity`. | Offline bookmark toggle queued for background sync. | Phase 3 | NOT_STARTED | Planned for Phase 3. |
| **13** | **Pantry Tracker & Expiry Alerts** | Shelf-life tracker with color-coded warning badges (danger/warning/info). | `PantryService`, `PantryItemEntity`, `PantryItemRepository`. | Real-time database alerts rendered on Dashboard. | Phase 3 | **PARTIALLY_IMPLEMENTED** | Schema, entity, service & dashboard alerts verified in Phase 1. |
| **14** | **Automatic Grocery List** | Consolidated checklist derived from planned meals minus pantry stock. | `GroceryBuilder.buildGroceryList()`. | Interactive checkboxes usable offline in local browser cache. | Phase 3 | NOT_STARTED | Planned for Phase 3. |
| **15** | **Cooking Mode** | Distraction-free full-screen cooking view with timer & steps. | Pure client-side web view with Wakelock API where available. | Completely offline functional once recipe is loaded. | Phase 3 | NOT_STARTED | Planned for Phase 3. |

---

## Group D: Tracking

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase | Status | Verification Evidence |
|---|---|---|---|---|---|:---:|---|
| **16** | **Food Diary & Macro Tracking** | Daily log timeline with calorie, macro, and fiber totals. | `DiaryEntryEntity`, `NutritionCalculator`. | Offline logging queued in IndexedDB; auto-synced on reconnect. | Phase 4 | NOT_STARTED | Planned for Phase 4. |
| **17** | **Step Counter** | Manual step log entry & wearable file import (CSV/JSON). | `BurnCalculator.estimateStepBurn()`. | **Zero fabricated sensor readings.** Explicit manual log & file import. | Phase 4 | NOT_STARTED | Domain calculation verified in `BurnCalculatorTest`. |
| **18** | **Calories Burned Counter** | MET activity burn log with duration and intensity inputs. | `BurnCalculator.calculateActivityBurn()`. | Verified pure Java mathematical formulas. | Phase 4 | NOT_STARTED | Domain calculation verified in `BurnCalculatorTest`. |
| **19** | **Water Intake Tracker** | Quick 250ml / 500ml logging buttons with hydration visualizer. | `WaterLogEntity` in JPA. | Offline counter stored in `localStorage` & synced to server. | Phase 4 | NOT_STARTED | Target calculation verified in `NutritionGoalCalculator`. |
| **20** | **Progress Charts & Reports** | HTML5 Canvas charts (Chart.js) and printable PDF reports. | Server-side PDF generation & REST aggregation APIs. | Charts render from cached weekly log data offline. | Phase 4 | NOT_STARTED | Planned for Phase 4. |

---

## Group E: Intelligence — AI

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase | Status | Verification Evidence |
|---|---|---|---|---|---|:---:|---|
| **21** | **AI Nutrition Chat Assistant** | Responsive chat interface with structured markdown responses. | `LlmClient` interface executing Java application tools. | Server-side key isolation. **Deterministic demo mode** when no key is set. | Phase 5 | **PARTIALLY_IMPLEMENTED** | `DeterministicLlmClient` implemented; tested in `AssistantApiController`. |
| **22** | **AI Proposes, Rules Verify** | Visual badge showing "Java Rules Verified" and tools run. | Pure Java `SafetyGate` auditing AI proposals before display. | Any AI output violating safety is intercepted and sanitized. | Phase 5 | NOT_STARTED | Planned for Phase 5. |
| **23** | **AI Recipe Adaptation** | "Make this Vegan/Gluten-Free" button replacing ingredients. | `RecipeAdapter.adaptRecipe()` + verified substitution tables. | Safe substitution rules stored in JSON and verified in Java. | Phase 5 | **PARTIALLY_IMPLEMENTED** | `DefaultRecipeAdapter.adaptRecipe()` implemented & unit tested in Phase 1. |
| **24** | **Personalized Insights** | Weekly dietary adherence summaries and motivational tips. | `InsightEngine.generateWeeklyInsights()`. | Rule-derived clinical insights computed from actual logs. | Phase 5 | NOT_STARTED | Planned for Phase 5. |

---

## Group F: Community

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase | Status | Verification Evidence |
|---|---|---|---|---|---|:---:|---|
| **25** | **Community Recipe Blog** | Moderated recipe posts with comments and like counters. | `CommunityPostEntity`, `CommentEntity`. | Read-only cached feed offline; posts queued for sync. | Phase 6 | NOT_STARTED | Planned for Phase 6. |
| **26** | **Moderation & Trust** | Automated allergy & safety filtering for user-submitted recipes. | Pure Java `SafetyGate` vetting community uploads. | Unverified posts remain unpublished until reviewed. | Phase 6 | NOT_STARTED | Planned for Phase 6. |
| **27** | **Streaks, Badges & Challenges** | Gamified adherence cards, trophy showcases, and streaks. | `BadgeService`, `StreakCalculator`. | Streaks calculated strictly on server using real log dates. | Phase 6 | NOT_STARTED | Planned for Phase 6. |

---

## Group G: Platform & Accessibility

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase | Status | Verification Evidence |
|---|---|---|---|---|---|:---:|---|
| **28** | **Global Allergy & Diet Filters** | Topbar profile badges and global persistent exclusions. | Injected into all recipe and planner search queries. | Filter state persisted in user session and browser storage. | Phase 7 | **PARTIALLY_IMPLEMENTED** | Enforced in `DefaultRulesEngine` and persisted in `UserProfileEntity`. |
| **29** | **Family Profiles** | Household switcher managing multi-person dietary requirements. | `FamilyMemberEntity` linked to primary user account. | Meal planner verifies combined constraints for all members. | Phase 7 | NOT_STARTED | Planned for Phase 7. |
| **30** | **Offline Mode & Sync (PWA)** | Service Worker, `manifest.webmanifest`, IndexedDB sync queue. | REST synchronization endpoints with conflict resolution. | Automatic network detection with `#networkSyncBadge`. | Phase 7 | **PARTIALLY_IMPLEMENTED** | PWA manifest, service worker, and sync badge active in Phase 0. |
| **31** | **Multi-Language & Accessibility** | English & Hindi UI strings, WCAG AA contrast, semantic landmarks. | Spring MVC `MessageSource` / `i18n` bundle. | Browser locale detection with manual language dropdown. | Phase 7 | **PARTIALLY_IMPLEMENTED** | Bilingual food & recipe fields (English & Hindi) active in Phase 1. |
| **32** | **Wearable Integration Architecture** | Standardized file import (Apple Health export, Garmin, FIT). | Extensible `WearableDataParser` interface. | Companion bridge architecture documented; manual fallback. | Phase 7 | NOT_STARTED | Planned for Phase 7. |
| **33** | **Sensitive-User Mode** | One-click toggle masking calories and numbers with blur & qualitative tags. | `DisplayPolicy` architecture; numbers omitted from UI strings. | Client-side class toggle (`body.sensitive-mode`) persisted locally & in DB profile. | Phase 7 | **COMPLETE** | Verified on Dashboard, Recipe Detail, Food Search, and Settings. |
