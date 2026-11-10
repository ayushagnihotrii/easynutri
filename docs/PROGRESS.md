# EasyNutri — Development Progress Tracker (Web Platform)

## Status Overview

| Phase | Description | Status | Verification Status |
|---|---|---|---|
| **Phase 0** | **Web Application Architecture Transition & Scaffolding** | **COMPLETE** | **All tests passing, bootJar verified** |
| **Phase 1** | **Authentication, Onboarding, Profiles & Nutrition Foundation** | **COMPLETE** | **All unit, repo & web MVC tests passing** |
| **Phase 2** | **Food Discovery, Multi-Input Finder & Deficiency Assistant** | **COMPLETE** | **All pure Java domain, DB & MVC integration tests passing** |
| **Phase 3** | **Meal Planning, Smart Pantry Tracking & Cooking Management** | **COMPLETE** | **All pure Java domain, DB & MVC integration tests passing (40/40)** |
| Phase 4 | Food Diary, Energy Burn & Hydration Tracking | Next Up | BMR/TDEE/MET engines & cooking session diary logs ready |
| Phase 5 | AI Assistant, Tool Execution & Clinical Safety Gate | Planned | Deterministic tool execution ready |
| Phase 6 | Community Recipe Blog, Moderation & Badges | Planned | Pending |
| Phase 7 | Family Profiles, Offline PWA Sync & Sensitive Mode | Planned | UI toggles and PWA assets scaffolded |
| Phase 8 | Comprehensive Testing, Security Audit & Production Release | Planned | Continuous verification active |

---

## Phase 3 Implementation Summary (Meal Planning, Smart Pantry & Cooking Complete)

### 1. Pure Java Business Logic (`:domain`)
- **`MealPlanner` & `DefaultMealPlanner`:**
  - 7-day intelligent meal plan generator with clinical safety auditing via `SafetyGate`.
  - Pantry and expiry prioritization: applies score bonuses for recipes using on-hand pantry items and ingredients nearing expiration.
  - 3-day variety enforcement preventing repetitive meal scheduling.
  - Locked meal preservation: locked slots survive plan regeneration without modification.
  - 1-tap safe alternative meal swap engine proposing 3 verified recipes for the same meal slot.
- **`GroceryBuilder` & `DefaultGroceryBuilder`:**
  - Plan-to-pantry reconciliation engine calculating exact deficit quantities: $\text{Required Demand} - \text{In Pantry Stock} = \text{To Buy}$.
  - Multi-recipe demand consolidation across varying portion sizes.
  - Indian grocery categorization (`VEGETABLES`, `PULSES`, `GRAINS`, `DAIRY`, `SPICES`, `GENERAL`).
- **Domain Records:**
  - `MealSlotProposal`: Immutable record holding proposed recipe, slot, servings, clinical selection reason, locked status, and completed flag.
  - `DailyMealPlan` and `WeeklyMealPlan`: Aggregated daily and 7-day schedule models with macro summaries.
  - `NutrientGapProvider`: Pure Java domain contract with `EmptyNutrientGapProvider` preventing fabricated nutrient gaps ahead of Phase 4 diary tracking.

### 2. Database & Persistence Layer (`:server`)
- **Flyway Migration `V4__phase3_schema.sql`:**
  - Extended `meal_plan_items`: added `locked` (BOOLEAN), `completed` (BOOLEAN), `selection_reason` (VARCHAR), and `notes` (TEXT).
  - Extended `pantry_items`: added `storage_location` (VARCHAR), `unit` (VARCHAR), `purchase_date` (DATE), and `notes` (TEXT).
  - Extended `grocery_items`: added `is_auto_generated` (BOOLEAN) to safeguard manual items from auto-reconciliation wipes.
  - Created `recipe_collections` & `collection_recipes`: persistent bookmark collections (Favorites, High Protein, Diabetic Friendly, etc.).
  - Created `cooking_sessions`: tracks distraction-free step progress, servings, and double-deduction prevention (`pantry_deducted`).
  - Created `user_notifications`: in-app scheduled expiry alerts and clinical reminders.
- **JPA Repositories & Entities:** `RecipeCollectionRepository`, `CookingSessionRepository`, `UserNotificationRepository`, `PantryItemRepository`, `MealPlanItemRepository`, `GroceryItemRepository`.
- **Application Services:**
  - `MealPlanService`: Full weekly plan generation, date-range retrieval, 1-tap meal replacement, lock toggling, completion checking, and servings updating.
  - `PantryService`: Inventory management, +/- gram adjustments, consumption logging, ingredient deduction on cooking completion, and "Cook Before It Expires" waste-prevention ranking.
  - `GroceryService`: Automated weekly plan reconciliation via `DefaultGroceryBuilder`, direct transfer of purchased grocery items to pantry stock with shelf-life assignment.
  - `RecipeCollectionService`: Default "Favorites" collection provisioning, custom collection CRUD, recipe bookmark assignment.
  - `CookingService`: Instruction regex parser extracting timers, scaled ingredient lists, step tracking, and double-deduction-safe pantry deduction.
  - `PantryExpiryScheduler`: `@Scheduled` daily cron job generating in-app notifications for foods approaching expiration (≤ 2 days).

### 3. Responsive Web UI & Full-Stack Controllers (`:server`)
- **Clinical Weekly Meal Planner (`/planner`):**
  - Interactive 7-day calendar navigation (Previous Week, Next Week, Current Week).
  - "Generate My Weekly Plan" button with clinical reasoning badges (e.g. *"Uses ingredients in pantry (Palak)"*, *"Prioritizes Paneer approaching expiry"*).
  - Servings scaler dropdowns, lock/unlock toggle buttons, completion checkboxes, and remove actions.
  - 1-Tap Safe Meal Swap modal powered by AJAX loading top 3 safe alternative recipes.
  - One-click "Sync with Grocery List" button.
- **Bookmarks & Collections (`/saved-recipes`):**
  - Collection tabs (All Saved, Favorites, custom collections).
  - Create new collection modal and delete collection action.
  - Search bar across recipe titles and personal cooking notes.
  - Meal slot filter dropdown.
  - Recipe cards with inline personal notes editor, "Add to Collection" dropdown, and quick action buttons (Cook Now, Remove).
- **Smart Pantry Tracker (`/pantry`):**
  - Metric summary cards (Total Inventory, Expiring Soon, Expired, Fresh & Safe).
  - "Cook Before It Expires" recommendation card prioritizing expiring stock.
  - Location filtering tabs (All, Cupboard, Fridge, Freezer) and Expiry status filter tabs.
  - Quantity controls (+/- 50g buttons), Mark Used button, Delete button.
  - Add Pantry Item modal with food selection, unit, location, and expiration date.
- **Interactive Cooking Mode (`/cook/{recipeId}`):**
  - Dedicated distraction-free step-by-step cooking interface.
  - Servings scaling selector with real-time recalculation of ingredient quantities.
  - Interactive scaled ingredients checklist.
  - Automatic timer extraction from instruction text with interactive start/pause/reset controls.
  - Hands-free Web Speech API integration (voice commands: *"Next"*, *"Previous"*, *"Read"*, *"Timer"*, and Text-to-Speech step reading).
  - Complete Cooking modal with pantry auto-deduction (protected against double-deduction) and food diary logging.

---

## Phase 2 Implementation Summary (Food Discovery & Multi-Input Complete)

### 1. Pure Java Business Logic (`:domain`)
- **`IngredientCandidate`:** Common immutable Java 17 domain representation for ingredients extracted across all input channels.
- **`IngredientResolver` & `DefaultIngredientResolver`:** Comprehensive Indian alias dictionary covering English, Hindi Devanagari, and Hinglish transliterations.
- **`RecipeMatcher` & `DefaultRecipeMatcher`:** Granular match labeling (`READY_NOW`, `READY_WITH_SWAP`, `MISSING_ONE`, `MISSING_TWO`, `MISSING_MANY`), substitution engine, and leftover prioritization.
- **`LeftoverSafetyEvaluator`:** Enforces microbiological safety boundaries based on ICMR and FSSAI guidelines (>4h room temp / >72h fridge BLOCK).
- **`DefaultDeficiencyService` & `NutrientInfo`:** Encapsulates ICMR-NIN 2024 RDA across 10 vital nutrients with bioavailability enhancement protocols.

---

## Phase 1 Implementation Summary (Foundation Complete)

### 1. Pure Java Business Logic (`:domain`)
- **`BmiCalculator`:** WHO standard thresholds and physiological range validation.
- **`NutritionGoalCalculator`:** Dynamic Mifflin-St Jeor BMR + TDEE with clinical safety floors (1200/1500 kcal) and adolescent/pregnancy safety guards.
- **`DefaultNutritionCalculator`:** Linear scaling of nutrients from canonical per-100g database values.
- **`DefaultRecipeAdapter`:** Dynamic serving scaler and allergen substitution engine.
- **`DefaultRulesEngine`:** Hard-block allergen matching, dietary restrictions, and clinical condition audits.
- **`SmartPantryRecommender`:** Staple recommendations filtered by user allergies and health conditions.

---

## Verification Test Results

| Command | Target | Result | Output |
|---|---|---|---|
| `.\gradlew.bat :domain:test` | Pure Java Domain Calculations, Planners & Reconcilers | **SUCCESS** | 45 domain unit tests passing |
| `.\gradlew.bat test` | Entire Test Suite (`:domain`, `:server`, `:tools`) | **SUCCESS** | **40/40 server integration tests passing, 0 failures** |
| `.\gradlew.bat spotlessCheck` | Java Code Style Verification | **SUCCESS** | Clean code formatting across all modules |
| `.\gradlew.bat :server:bootJar` | Executable Spring Boot JAR Package | **SUCCESS** | Standalone production artifact generated |
