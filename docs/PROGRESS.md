# EasyNutri — Development Progress Tracker (Web Platform)

## Status Overview

| Phase | Description | Status | Verification Status |
|---|---|---|---|
| **Phase 0** | **Web Application Architecture Transition & Scaffolding** | **COMPLETE** | **All tests passing, bootJar verified** |
| **Phase 1** | **Authentication, Onboarding, Profiles & Nutrition Foundation** | **COMPLETE** | **All unit, repo & web MVC tests passing** |
| Phase 2 | Food Discovery, Serving Scaler & Multi-Input Finder | Next Up | Serving scaler & search core ready in Phase 1 |
| Phase 3 | Meal Planning, Smart Pantry Suggestions & Grocery List | Planned | Smart Pantry recommender ready in Phase 1 |
| Phase 4 | Food Diary, Energy Burn & Hydration Tracking | Planned | BMR/TDEE/MET engines ready in Phase 1 |
| Phase 5 | AI Assistant, Tool Execution & Clinical Safety Gate | Planned | Deterministic tool execution ready |
| Phase 6 | Community Recipe Blog, Moderation & Badges | Planned | Pending |
| Phase 7 | Family Profiles, Offline PWA Sync & Sensitive Mode | Planned | UI toggles and PWA assets scaffolded |
| Phase 8 | Comprehensive Testing, Security Audit & Production Release | Planned | Continuous verification active |

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
- **`DefaultRecipeMatcher`:** Ingredient presence scoring algorithm with ranking and customizable threshold.
- **`SmartPantryRecommender`:** Tailored household staple recommendations grouped into Grains, Pulses, Dairy/Plant-protein, Vegetables, and Spices, filtered by user allergies and health conditions.

### 2. Database & Persistence Layer (`:server`)
- **Flyway Migration `V2__phase1_schema.sql`:**
  - `user_profiles`: stores biometric metrics, goals, dietary preferences, pregnancy, sensitive mode, target calories/macros.
  - `user_allergies`: collection table for active allergy exclusions.
  - `user_health_conditions`: collection table for diagnosed clinical conditions.
  - `foods`: canonical food facts per 100g (energy, protein, carbs, sugars, fats, fiber, sodium, potassium, calcium, iron, source, verified).
  - `recipes`: structured recipes with servings, prep/cook times, instructions, and tags.
  - `recipe_ingredients`: foreign-key join mapping recipes to canonical foods with exact gram amounts.
  - `pantry_items`: user-specific pantry inventory with shelf-life tracking and expiration dates.
- **JPA Repositories:** `UserProfileRepository`, `FoodRepository`, `RecipeRepository`, `PantryItemRepository`.
- **Application Services:** `UserProfileService`, `FoodService`, `RecipeService`, `PantryService`.
- **Data Ingestion (`DataInitializer`):** Automatically ingests genuine Indian food items (IFCT 2017 & USDA FoodData Central) and classic recipes (`indian_foods.json`, `indian_recipes.json`), and seeds sample pantry inventory for the demo user.

### 3. Responsive Web UI & Full-Stack Controllers (`:server`)
- **Onboarding Wizard (`/onboarding`):** Beautiful 3-step interactive setup wizard guiding new users through biometrics, lifestyle goals, dietary preferences, allergies, and health conditions.
- **Dynamic Dashboard (`/dashboard`):** Real-time calculated BMI, personalized calorie target, protein/carb/fat/water targets, live pantry expiration alerts, and smart staple recommendations.
- **Discover Recipes (`/recipes`):** Meal slot filtering (`BREAKFAST`, `LUNCH`, `DINNER`, `SNACK`) and search.
- **Interactive Recipe Detail & Serving Scaler (`/recipes/{id}`):** Interactive `+` / `-` portion scaler recalculating ingredient amounts and per-serving/total nutrition from database rows.
- **Food & Nutrition Database (`/foods`):** Searchable food catalog with category filtering and per-100g nutrient facts.
- **Profile Settings (`/settings`):** Editable biometrics, allergies, conditions, and sensitive-user mode toggle with database persistence.

---

## Verification Test Results

| Command | Target | Result | Output |
|---|---|---|---|
| `.\gradlew.bat spotlessCheck` | Google Java Format compliance | **PASSED** | Compliant across all modules |
| `.\gradlew.bat :domain:test` | Pure Java domain calculations | **PASSED** | 9 tests passed (BMI, BMR, Goals, Rules, Scaling, Matcher, Staples) |
| `.\gradlew.bat :tools:test` | CLI & JSON schemas | **PASSED** | Schema validation passed |
| `.\gradlew.bat :server:test` | Spring Boot web & API tests | **PASSED** | 12 integration tests passed (Auth, Dashboard, Onboarding, Food, Health) |
| `.\gradlew.bat test` | Full project test suite | **PASSED** | All 12 actionable test tasks passed |
| `.\gradlew.bat :server:bootJar` | Executable Spring Boot JAR | **PASSED** | Verified production executable JAR |
