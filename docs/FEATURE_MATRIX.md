# EasyNutri — Full-Stack Web Application Feature Matrix

This document provides the definitive mapping of all 34 EasyNutri features, defining their full-stack web and PWA architecture, browser capabilities, fallback mechanisms, and implementation phases.

---

## Group A: Foundation

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase |
|---|---|---|---|---|---|
| **1** | **User Profiles & Health Setup** | Responsive onboarding wizard (age, biological sex, height, weight, activity). | `ProfileContext`, `UserService`, `UserEntity` JPA persistence. | IndexedDB local form preservation; syncs when online. | Phase 1 |
| **2** | **BMI, BMR & Calorie Goals** | Dynamic client dashboard metric cards with animated progress rings. | `DefaultBurnCalculator` (Mifflin-St Jeor equation). | Evaluated in pure Java 17; client-side cached calculations. | Phase 1 |
| **3** | **Food & Nutrition Database** | Searchable UI with macro charts and micronutrient breakdowns. | JPA `FoodRepository` + USDA / IFCT JSON catalogs in `data/`. | Static food dataset cached by Service Worker & IndexedDB. | Phase 1 |
| **4** | **Recipes with Serving Scaler** | Interactive recipe detail card with dynamic serving multiplier. | `RecipeAdapter.scaleServings()`, `RecipeCandidate`. | Client-side reactive DOM scaling; server recalculation on save. | Phase 2 |
| **5** | **Meal Categories** | Breakfast, Lunch, Dinner, Snack filter tags and tabs. | Enumerated category models in `:domain`. | Cached filter metadata. | Phase 1 |
| **6** | **Health Condition Planner** | Clinical condition checklists (Diabetes, Hypertension, CKD, GERD). | `RulesEngine`, `SafetyGate` pure Java evaluations. | Strict rule evaluation on server; safe defaults cached locally. | Phase 1 |
| **7** | **Smart Pantry Suggestions** | Expiration alert pills with instant "Cook Now" callouts. | `PantryEvaluator`, `RecipeMatcher`. | Cached pantry snapshot used for offline match suggestions. | Phase 3 |

---

## Group B: Finding Food

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase |
|---|---|---|---|---|---|
| **8** | **Ingredient-Based Finder** | Multi-tag ingredient input with match percentage badge. | `RecipeMatcher.matchRecipes()`. | Cached candidate catalog matched locally via JS or backend API. | Phase 2 |
| **9** | **Multi-Input Ingredient Entry** | 5 input channels: Typed, Voice, Barcode, Photo, and OCR. | Replaceable `IngredientIngestionService` interfaces. | **Typed input is always the primary fallback** when Web Speech, BarcodeDetector, or Camera are unavailable. | Phase 2 |
| **10** | **Leftover Mode** | Pantry cleanup filter prioritizing expiring produce. | `RecipeMatcher.calculateMatchScore()` with expiry weight. | Local shelf-life calculator; manual date override. | Phase 2 |
| **11** | **Deficiency Helper** | Micronutrient alerts (Iron, Calcium, Vitamin B12, Vitamin D). | `DeficiencyService.findFoodsRichIn()`. | Clinically safe food recommendations cached offline. | Phase 2 |

---

## Group C: Planning

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase |
|---|---|---|---|---|---|
| **12** | **Personalized Weekly Planner** | 7-day responsive planner grid with drag/drop & slot selection. | `MealPlanner.generateWeeklyPlan()`. | Full 7-day schedule cached in IndexedDB; queued sync. | Phase 3 |
| **13** | **Bookmarks & Collections** | Favorite recipe collections with custom tags. | JPA `BookmarkEntity` & `CollectionEntity`. | Offline bookmark toggle queued for background sync. | Phase 3 |
| **14** | **Pantry Tracker & Expiry Alerts** | Shelf-life table with color-coded warning badges. | `PantryTrackerService` calculating shelf-life days. | Local IndexedDB storage; push notifications where supported. | Phase 3 |
| **15** | **Automatic Grocery List** | Consolidated checklist derived from planned meals minus pantry stock. | `GroceryBuilder.buildGroceryList()`. | Interactive checkboxes usable offline in local browser cache. | Phase 3 |
| **16** | **Interactive Cooking Mode** | Distraction-free full-screen cooking view with timer & steps. | Pure client-side web view with Wakelock API where available. | Completely offline functional once recipe is loaded. | Phase 3 |

---

## Group D: Tracking

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase |
|---|---|---|---|---|---|
| **17** | **Food Diary & Macro Tracking** | Daily log timeline with calorie, macro, and fiber totals. | `DiaryEntryEntity`, `NutritionCalculator`. | Offline logging queued in IndexedDB; auto-synced on reconnect. | Phase 4 |
| **18** | **Step Tracking** | Manual step log entry & wearable file import (CSV/FIT/JSON). | `BurnCalculator.estimateStepBurn()`. | **Zero fabricated sensor readings.** Explicit manual log & file import. | Phase 4 |
| **19** | **Calories Burned** | MET activity burn log with duration and intensity inputs. | `BurnCalculator.calculateActivityBurn()`. | Verified pure Java mathematical formulas. | Phase 4 |
| **20** | **Water Intake** | Quick 250ml / 500ml logging buttons with hydration visualizer. | `WaterLogEntity` in JPA. | Offline counter stored in `localStorage` & synced to server. | Phase 4 |
| **21** | **Progress Charts & PDF Reports** | HTML5 Canvas charts (Chart.js) and printable PDF reports. | Server-side PDF generation & REST aggregation APIs. | Charts render from cached weekly log data offline. | Phase 4 |

---

## Group E: AI

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase |
|---|---|---|---|---|---|
| **22** | **AI Nutrition Assistant** | Responsive chat interface with structured markdown responses. | `LlmClient` interface executing Java application tools. | Server-side key isolation. **Deterministic demo mode** when no key is set. | Phase 5 |
| **23** | **AI Proposes, Java Rules Verify** | Visual badge showing "Java Rules Verified" and tools run. | Pure Java `SafetyGate` auditing AI proposals before display. | Any AI output violating safety is intercepted and sanitized. | Phase 5 |
| **24** | **AI Recipe Adaptation** | "Make this Vegan/Gluten-Free" button replacing ingredients. | `RecipeAdapter.adaptRecipe()` + verified substitution tables. | Safe substitution rules stored in JSON and verified in Java. | Phase 5 |
| **25** | **Personalized Insights** | Weekly dietary adherence summaries and motivational tips. | `InsightEngine.generateWeeklyInsights()`. | Rule-derived clinical insights computed from actual logs. | Phase 5 |

---

## Group F: Community

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase |
|---|---|---|---|---|---|
| **26** | **Community Recipe Blog** | Moderated recipe posts with comments and like counters. | `CommunityPostEntity`, `CommentEntity`. | Read-only cached feed offline; posts queued for sync. | Phase 6 |
| **27** | **Moderation & Trust** | Automated allergy & safety filtering for user-submitted recipes. | Pure Java `SafetyGate` vetting community uploads. | Unverified posts remain unpublished until reviewed. | Phase 6 |
| **28** | **Streaks, Badges & Challenges** | Gamified adherence cards, trophy showcases, and streaks. | `BadgeService`, `StreakCalculator`. | Streaks calculated strictly on server using real log dates. | Phase 6 |

---

## Group G: Platform & Accessibility

| # | Feature | Web & Browser Capability | Java Domain / Backend Core | Offline / Fallback Strategy | Phase |
|---|---|---|---|---|---|
| **29** | **Global Allergy & Diet Filters** | Topbar profile badges and global persistent exclusions. | Injected into all recipe and planner search queries. | Filter state persisted in user session and browser storage. | Phase 7 |
| **30** | **Family Profiles** | Household switcher managing multi-person dietary requirements. | `FamilyMemberEntity` linked to primary user account. | Meal planner verifies combined constraints for all members. | Phase 7 |
| **31** | **Offline PWA Mode & Sync** | Service Worker, `manifest.webmanifest`, IndexedDB sync queue. | REST synchronization endpoints with conflict resolution. | Automatic network detection with sync status indicator. | Phase 7 |
| **32** | **Wearable Integration Architecture** | Standardized file import (Apple Health export, Garmin, FIT). | Extensible `WearableDataParser` interface. | Companion bridge architecture documented; manual fallback. | Phase 7 |
| **33** | **Sensitive-User Mode** | One-click toggle masking calories and numbers with blur & qualitative tags. | `DisplayPolicy` architecture; numbers omitted from UI strings. | Client-side class toggle (`body.sensitive-mode`) persisted locally. | Phase 7 |
| **34** | **Multi-Language & Accessibility** | English & Hindi UI strings, WCAG AA contrast, semantic landmarks. | Spring MVC `MessageSource` / `i18n` bundle. | Browser locale detection with manual language dropdown. | Phase 7 |
