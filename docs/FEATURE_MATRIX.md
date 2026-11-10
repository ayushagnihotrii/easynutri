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
| **11** | **Personalized Weekly Planner** | 7-day responsive planner grid with drag/drop & slot selection. | `MealPlanService`, `MealPlanItemEntity`, `MealPlanner`. | Full schedule persisted in database; modal scheduling from recipes. | Phase 3 | **PARTIALLY_IMPLEMENTED** | Entity, repository, service & API implemented in Phase 2 (`V3__phase2_schema.sql`). |
| **12** | **Bookmarks & Collections** | Favorite recipe bookmarks with custom notes and interactive button toggles. | `SavedRecipeEntity`, `SavedRecipeRepository`, `SavedRecipeService`. | Database persistence with instant AJAX bookmark state toggling. | Phase 3 | **PARTIALLY_IMPLEMENTED** | Entity, service, and API controller verified in Phase 2. |
| **13** | **Pantry Tracker & Expiry Alerts** | Shelf-life tracker with color-coded warning badges (danger/warning/info). | `PantryService`, `PantryItemEntity`, `PantryItemRepository`. | Real-time database alerts rendered on Dashboard and Ingredient Finder. | Phase 3 | **PARTIALLY_IMPLEMENTED** | Schema, entity, service & dashboard alerts verified in Phase 1. |
| **14** | **Automatic Grocery List** | Dedicated `/grocery` checklist with real-time checkboxes, clear completed, custom add, and batch missing import from recipes. | `GroceryItemEntity`, `GroceryItemRepository`, `GroceryService`, `GroceryWebController`. | Interactive checkboxes with persistent database updates. | Phase 3 | **COMPLETE** | Tested in `GroceryServiceTest` and `GroceryWebController`. |
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
| **22** | **Safety Gate** | Hard-boundary clinical interceptor for all AI proposals. | Pure Java `SafetyGate` enforcing allergen blocks and medical limits. | **Zero AI bypass.** Server-enforced safety filter before rendering. | Phase 5 | **COMPLETE** | Verified in `DefaultSafetyGate` and `IngredientService`. |
| **23** | **Health Claim Fact-Checker** | Ayurvedic and Western nutrition claim verifier against ICMR guidelines. | Static clinical rules engine. | Pre-validated claims database cached for offline inspection. | Phase 5 | NOT_STARTED | Planned for Phase 5. |

---

## Group F: Community & Growth

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase | Status | Verification Evidence |
|---|---|---|---|---|---|:---:|---|
| **24** | **Recipe Blog** | Community recipe publishing with ingredient tags and photo uploads. | `CommunityRecipeEntity` in JPA. | Read-only cached blog posts available offline. | Phase 6 | NOT_STARTED | Scaffolded view at `/community`. |
| **25** | **Community Verification** | User star ratings and clinical dietitian review badges. | Moderation audit log in DB. | Cached review badges. | Phase 6 | NOT_STARTED | Planned for Phase 6. |
| **26** | **Health Claim Moderation** | Automated screening of recipe submissions for medical misinformation. | SafetyGate automated submission audit. | Rejects unverifiable medical claims before database persistence. | Phase 6 | NOT_STARTED | Planned for Phase 6. |
| **27** | **Streak Counter** | Consecutive daily logging streak counter with celebration badge. | Computed from daily food logs in JPA. | Stored in local profile cache. | Phase 6 | NOT_STARTED | Planned for Phase 6. |
| **28** | **Badges & Milestones** | Achievement cards (First Recipe Cooked, 7-Day Hydration, Fiber Master). | `BadgeService` evaluating user log achievements. | Awarded badges cached in browser profile. | Phase 6 | NOT_STARTED | Planned for Phase 6. |

---

## Group G: Administration, Family & Platform

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase | Status | Verification Evidence |
|---|---|---|---|---|---|:---:|---|
| **29** | **Family Profiles** | Switchable multi-profile selector (Spouse, Child, Elderly Parent). | `FamilyMemberEntity` in JPA. | Profile contexts cached in browser for fast offline switching. | Phase 7 | NOT_STARTED | Scaffolded view at `/family`. |
| **30** | **Admin Dashboard** | Clinical rule editor, food database manager, and user moderation. | Spring Security `ROLE_ADMIN` protected routes. | Requires active administrator session. | Phase 7 | NOT_STARTED | Planned for Phase 7. |
| **31** | **Responsive Layout** | CSS Grid & Flexbox adaptive layouts (Desktop, Tablet, Mobile) with 8pt grid. | Thymeleaf layout templates with unified CSS variables. | Fully local CSS stylesheets without CDN dependencies. | Phase 0-7 | **COMPLETE** | Verified on Mobile (375px), Tablet (768px), and Desktop (1440px). |
| **32** | **Progressive Web App (PWA)** | Web App Manifest, Service Worker caching, and "Add to Home Screen". | `sw.js`, `manifest.webmanifest`, `offline.html`. | Full offline fallback page and cached asset delivery. | Phase 0-7 | **COMPLETE** | Verified in browser audit; manifests and service worker active. |
| **33** | **Sensitive-User Mode** | One-click toggle masking calorie numbers with qualitative badges. | CSS class `.sensitive-mode` driven by `localStorage` & profile state. | Works completely client-side in real time across all views. | Phase 1 | **COMPLETE** | Tested on `/dashboard`, `/foods`, and `/ingredients`. |
