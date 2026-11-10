# EasyNutri — Development Progress Tracker (Web Platform)

## Status Overview

| Phase | Description | Status | Verification Status |
|---|---|---|---|
| **Phase 0** | **Web Application Architecture Transition & Scaffolding** | **COMPLETE** | **All tests passing, bootJar verified** |
| **Phase 1** | **Authentication, Onboarding, Profiles & Nutrition Foundation** | **COMPLETE** | **All unit, repo & web MVC tests passing** |
| **Phase 2** | **Food Discovery, Multi-Input Finder & Deficiency Assistant** | **COMPLETE** | **All pure Java domain, DB & MVC integration tests passing** |
| Phase 3 | Meal Planning, Smart Pantry Suggestions & Grocery List | Next Up | Grocery entity/service & Pantry foundations ready |
| Phase 4 | Food Diary, Energy Burn & Hydration Tracking | Planned | BMR/TDEE/MET engines ready in Phase 1 |
| Phase 5 | AI Assistant, Tool Execution & Clinical Safety Gate | Planned | Deterministic tool execution ready |
| Phase 6 | Community Recipe Blog, Moderation & Badges | Planned | Pending |
| Phase 7 | Family Profiles, Offline PWA Sync & Sensitive Mode | Planned | UI toggles and PWA assets scaffolded |
| Phase 8 | Comprehensive Testing, Security Audit & Production Release | Planned | Continuous verification active |

---

## Phase 2 Implementation Summary (Food Discovery & Multi-Input Complete)

### 1. Pure Java Business Logic (`:domain`)
- **`IngredientCandidate`:** Common immutable Java 17 domain representation for ingredients extracted across all input channels (typed, voice, photo, barcode, OCR, leftover).
- **`IngredientResolver` & `DefaultIngredientResolver`:** 
  - Comprehensive Indian alias dictionary covering English, Hindi Devanagari (चावल, दाल, पनीर, आलू, पालक, टमाटर, प्याज, आदि) and Hinglish transliterations (chawal, aloo, pyaz, dahi).
  - Regex-driven quantity and unit parser (`katori`, `cup`, `tbsp`, `tsp`, `grams`, `kg`, `pieces`) with household-to-gram conversion.
  - Natural language phrase extractor parsing conversational sentences (e.g. *"I have rice, 200g paneer, tomato and 2 onions"*).
- **`RecipeMatcher` & `DefaultRecipeMatcher` (Enhanced):**
  - Granular match labeling: `READY_NOW` ("Ready Now"), `READY_WITH_SWAP` ("Ready With a Swap"), `MISSING_ONE` ("Missing 1 Ingredient"), `MISSING_TWO` ("Missing 2 Ingredients"), `MISSING_MANY` ("Missing 3+ Ingredients").
  - Intelligent substitution engine (e.g. paneer <-> tofu, curd <-> lemon/yogurt, toor dal <-> moong dal, rice <-> millets/quinoa).
  - Leftover consumption prioritization giving bonus score and visual badges to recipes utilizing cooked inventory.
- **`LeftoverSafetyEvaluator`:** 
  - Enforces microbiological safety boundaries based on ICMR and FSSAI guidelines.
  - Cooked food at ambient room temperature > 4 hours triggers `BLOCK` (preventing ingestion of heat-stable *B. cereus* toxins).
  - Refrigerated leftovers > 72 hours triggers `BLOCK` (prolonged storage spoilage).
  - Ambient 2-4 hours and refrigerated 48-72 hours trigger `WARN` with mandatory reheating guidance (>= 75°C).
- **`DefaultDeficiencyService` & `NutrientInfo`:**
  - Encapsulates ICMR-NIN 2024 Reference Daily Allowances across 10 vital nutrients: Iron, Vitamin B12, Vitamin D, Calcium, Protein, Dietary Fibre, Folate, Zinc, Vitamin C, Magnesium.
  - Automatically calculates % daily RDA per serving and ranks verified Indian foods descending by nutrient density.
  - Supplies clinical absorption enhancement rules (e.g. pairing plant iron with Vitamin C, avoiding tea/coffee within 2 hours of iron meals, consuming fat-soluble Vitamin D with healthy fats).
- **`FoodRecognitionProvider` & `OcrProvider`:** Domain interfaces with resilient default fallback implementations ensuring zero fabricated AI confidence scores.

### 2. Database & Persistence Layer (`:server`)
- **Flyway Migration `V3__phase2_schema.sql`:**
  - `saved_recipes`: User bookmarks with custom notes and unique constraint `(user_id, recipe_id)`.
  - `grocery_items`: Shopping checklist items with quantity, unit, checked status, category, and food references.
  - `meal_plan_items`: Calendar meal schedule with date, slot (`BREAKFAST`, `LUNCH`, `DINNER`, `SNACK`), and servings.
  - `food_portions`: Household portion definitions with exact gram weights per food.
- **JPA Repositories:** `SavedRecipeRepository`, `GroceryItemRepository`, `MealPlanItemRepository`, `FoodPortionRepository`.
- **Application Services:** 
  - `IngredientService`: Orchestrates `IngredientResolver`, `RecipeMatcher`, `LeftoverSafetyEvaluator`, and `SafetyGate`.
  - `GroceryService`: Interactive addition, batch import of missing ingredients, checkbox toggles, and completed cleanup.
  - `SavedRecipeService`: Recipe bookmarking and status check.
  - `MealPlanService`: Scheduling recipes into user calendar.
  - `BarcodeLookupService`: Barcode lookup against local food database with test fallback mappings.

### 3. Responsive Web UI & Full-Stack Controllers (`:server`)
- **Clinical Ingredient Finder (`/ingredients`):**
  - Dedicated modern interface with 5 multi-input tabs (Typed Autocomplete, Web Speech API Voice, Photo Upload, Barcode Scanner, Smart OCR).
  - Interactive Leftover Mode toggle with real-time ICMR/FSSAI safety alert banner.
  - Ingredient chip manager with category pills and one-click quick add from user's pantry.
  - Rich recipe result cards displaying match %, readiness badge, available vs missing ingredient lists, suggested swaps, per-serving nutrition, and SafetyGate warnings.
  - Four fully functional action buttons: **View Recipe**, **+ Missing to Grocery**, **Save Recipe**, and **📅 Add to Plan**.
- **Micronutrient Deficiency Assistant (`/deficiency-helper`):**
  - 10 nutrient selector pills, ICMR 2024 RDA comparison tailored to age/gender/pregnancy.
  - Top Indian food density ranking grid showing % of daily target.
  - Bioavailability enhancement tips and suitable clinical recipes.
  - Mandatory clinical advisory notice.
- **Smart Grocery Checklist (`/grocery`):**
  - Interactive shopping progress bar and checklist.
  - Real-time checkbox state toggling, item deletion, and "Clear Checked Items" action.
  - "Add Custom Item" modal with unit and category dropdowns.

---

## Phase 1 Implementation Summary (Foundation Complete)

### 1. Pure Java Business Logic (`:domain`)
- **`BmiCalculator`:** WHO standard thresholds (`UNDERWEIGHT <18.5`, `NORMAL 18.5-24.9`, `OVERWEIGHT 25.0-29.9`, `OBESE >=30.0`), physiological range validation (20-400kg, 50-260cm), and tailored clinical guidance.
- **`NutritionGoalCalculator`:** 
  - Dynamic Mifflin-St Jeor BMR + TDEE calculation across 5 activity multipliers.
  - Safe caloric adjustments for `LOSE_WEIGHT` (-500 kcal), `MAINTAIN` (0), and `GAIN_MUSCLE` (+300 kcal).
  - Clinical safety floors: 1200 kcal/day for females, 1500 kcal/day for males.
  - Adolescent safety rule: Caloric deficits prohibited for under-18 users.
  - Maternal safety rule: Caloric restriction prohibited during pregnancy.
  - Macronutrient target distribution (protein, carbs, fat, hydration) derived from body weight and goal.
- **`DefaultNutritionCalculator`:** Linear scaling of nutrients from canonical per-100g database values.
- **`DefaultRecipeAdapter`:** Dynamic serving scaler (`scaleServings`) and ingredient substitution engine (`adaptRecipe`) replacing allergenic foods with certified alternatives.
- **`DefaultRulesEngine`:** 
  - Hard-block allergen matching with Indian and international synonym mapping (`peanut`/`moongphali`, `dairy`/`paneer`/`curd`, `gluten`/`wheat`/`atta`, `soy`/`tofu`, `egg`/`anda`, `shellfish`).
  - Dietary restrictions (`VEGAN`, `VEGETARIAN`, `JAIN` root-vegetable exclusions).
  - Clinical condition evaluations (`HYPERTENSION` sodium limits, `DIABETES_T2` sugar limits, `CKD` potassium monitoring).
  - Strict precedence order: `BLOCK` > `WARN` > `ALLOW`.
- **`SmartPantryRecommender`:** Tailored household staple recommendations grouped into Grains, Pulses, Dairy/Plant-protein, Vegetables, and Spices, filtered by user allergies and health conditions.

---

## Verification Test Results

| Command | Target | Result | Output |
|---|---|---|---|
| `.\gradlew.bat spotlessCheck` | Google Java Format compliance | **PASSED** | Compliant across all modules |
| `.\gradlew.bat :domain:test` | Pure Java domain calculations | **PASSED** | 41 tests passed (BMI, BMR, Goals, Rules, Matcher, Ingredients, Leftovers, Deficiencies) |
| `.\gradlew.bat :tools:test` | CLI & JSON schemas | **PASSED** | Schema validation passed |
| `.\gradlew.bat :server:test` | Spring Boot web & API tests | **PASSED** | 23 integration tests passed (Auth, Dashboard, Onboarding, Food, Ingredients, Deficiency, Grocery, APIs) |
| `.\gradlew.bat test` | Full project test suite | **PASSED** | All 11 actionable test tasks executed successfully |
| `.\gradlew.bat :server:bootJar` | Executable Spring Boot JAR | **PASSED** | Verified production executable JAR |
