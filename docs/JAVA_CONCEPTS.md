# EasyNutri — Java OOP Architecture & Syllabus Mapping (Web Platform)

This document provides a systematic mapping of core Object-Oriented Programming (OOP) concepts and modern Java 17 features implemented in EasyNutri's web application.

---

## 1. Encapsulation & Immutability

### Description
Data hiding and state protection prevent external entities from placing objects into inconsistent states. In Java 17, immutable records provide state encapsulation with built-in accessors, `equals()`, `hashCode()`, and `toString()`.

### Code Reference
- **Domain Records:**
  - `com.easynutri.domain.model.FoodCandidate`: Immutable candidate model for food evaluation.
  - `com.easynutri.domain.model.RecipeCandidate`: Immutable candidate model for multi-ingredient recipes.
  - `com.easynutri.domain.model.ProfileContext`: Immutable user context (allergies, conditions, goals).
  - `com.easynutri.dto.HealthResponse`, `com.easynutri.dto.AuthResponse`, `com.easynutri.dto.LoginRequest`, `com.easynutri.dto.RegisterRequest`.
- **Defensive Copying:**
  `FoodCandidate` and `ProfileContext` create unmodifiable defensive copies:
  ```java
  public FoodCandidate {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(name, "name must not be null");
    nutrients = Collections.unmodifiableMap(new HashMap<>(nutrients));
    tags = Collections.unmodifiableSet(new HashSet<>(tags));
  }
  ```

---

## 2. Abstraction & Interfaces

### Description
Separating what an operation does from how it is implemented. High-level modules depend on abstractions rather than concrete classes.

### Code Reference
- `com.easynutri.domain.rules.RulesEngine`: Contract for rule evaluation.
- `com.easynutri.domain.nutrition.SafetyGate`: Contract for safety checks against clinical profiles.
- `com.easynutri.domain.calculation.NutritionCalculator`: Contract for nutrient math.
- `com.easynutri.domain.recipe.RecipeMatcher`: Contract for ingredient matching and leftover mode.
- `com.easynutri.domain.planner.MealPlanner`: Contract for weekly meal planning.
- `com.easynutri.domain.grocery.GroceryBuilder`: Contract for grocery consolidation.
- `com.easynutri.domain.nutrition.DeficiencyService`: Contract for detecting micronutrient deficiencies.
- `com.easynutri.domain.calculation.BurnCalculator`: Contract for BMR, TDEE, and MET activity burns.
- `com.easynutri.domain.insights.InsightEngine`: Contract for clinical habit insights.
- `com.easynutri.domain.recipe.RecipeAdapter`: Contract for recipe adaptation and serving scaling.
- `com.easynutri.ai.LlmClient`: Provider-independent contract for AI assistant integration.
- `com.easynutri.repository.UserRepository`: JPA interface abstracting all database queries.

---

## 3. Polymorphism & Strategy Pattern

### Description
Different implementations can be substituted at runtime behind a common interface without client code awareness.

### Code Reference
- **Calculator Strategy:** `DefaultBurnCalculator` implements `BurnCalculator`, evaluating Mifflin-St Jeor and MET equations.
- **AI Strategy:** `DeterministicLlmClient` implements `LlmClient` to execute pure Java tools deterministically without remote API dependencies.
- **Security UserDetails:** `CustomUserDetailsService` implements Spring Security's `UserDetailsService`.

---

## 4. Sealed Interfaces & Pattern Matching (Java 17)

### Description
Restricting interface implementation to a strictly controlled closed hierarchy, enabling exhaustive compiler checks and preventing unauthorized sub-classing.

### Code Reference
- `com.easynutri.domain.model.SafetyCandidate`:
  ```java
  public sealed interface SafetyCandidate permits FoodCandidate, RecipeCandidate {
    String id();
    String name();
    Map<String, Double> nutrients();
  }
  ```

---

## 5. Custom Exception Hierarchy

### Description
Hierarchical error handling rooted in a domain-specific runtime exception to distinguish domain failures from unexpected system errors.

### Code Reference
- `com.easynutri.domain.exception.EasyNutriException`: Base unchecked application exception.
- `com.easynutri.domain.exception.ValidationException`: Specific exception carrying structured field-level error messages (`Map<String, String> errors`).

---

## 6. Dependency Injection & Annotations

### Description
Decoupling class creation and lifecycle management through dependency injection and declarative metadata in Spring Boot 4.1.2.

### Code Reference
- `@SpringBootApplication` on `EasyNutriApplication`
- `@Controller`, `@RestController`, `@GetMapping`, `@PostMapping` on web and REST controllers
- `@Service`, `@Repository`, `@Transactional` on business service layers
- `@Entity`, `@Table`, `@Id`, `@Column` on `UserEntity`


---

## 7. Mathematical & Clinical Calculation Engines

### Description
Separation of business mathematics into pure, side-effect-free static and strategy methods. Zero coupling with web controllers or persistence layers.

### Code Reference
- `com.easynutri.domain.calculation.BmiCalculator`: Pure Java implementation of WHO BMI formulas with range validation.
- `com.easynutri.domain.calculation.NutritionGoalCalculator`: Mifflin-St Jeor TDEE energy calculation, adolescent & maternal safety bounds, and macronutrient gram allocations.
- `com.easynutri.domain.calculation.DefaultNutritionCalculator`: High-precision linear nutrient scaling using `BigDecimal.setScale()`.
- `com.easynutri.domain.recipe.DefaultRecipeMatcher`: Deterministic ingredient match scoring algorithm using Java Sets and Streams.
- `com.easynutri.domain.recipe.SmartPantryRecommender`: Category-based pantry staple generation with allergy exclusions.

---

## 8. Clinical Rules Engine & Precedence Order

### Description
Evaluating clinical health conditions, dietary types, and allergies using deterministic priority ordering (`BLOCK > WARN > ALLOW`).

### Code Reference
- `com.easynutri.domain.rules.DefaultRulesEngine`:
  - Hard allergen matching using comprehensive Indian and international synonym sets (`Map<String, Set<String>> ALLERGEN_SYNONYMS`).
  - Dietary constraints (`VEGAN`, `VEGETARIAN`, `JAIN`).
  - Medical condition sodium, sugar, and potassium checks.


---

## 9. Plan-to-Pantry Reconciliation & Pure Java Planning Engines

### Description
Complex business algorithms modeling multi-entity reconciliation (aggregating multi-recipe ingredient demands across varying portion scales and computing net purchase requirements after subtracting available inventory) implemented in pure Java with zero external dependencies.

### Code Reference
- `com.easynutri.domain.planner.DefaultMealPlanner`:
  - 7-day personalized weekly schedule optimization.
  - Multi-factor scoring (pantry inventory bonus, expiry bonus, 3-day variety enforcement).
  - Locked slot preservation preventing overwrites during schedule regeneration.
  - 1-tap safe replacement generator proposing alternatives conforming to the same slot and clinical safety profile.
- `com.easynutri.domain.grocery.DefaultGroceryBuilder`:
  - Pure Java reconciliation algorithm: $\text{Required Demand} - \text{In Pantry Stock} = \text{To Buy}$.
  - Multi-category sorting and classification (`VEGETABLES`, `PULSES`, `GRAINS`, `DAIRY`, `SPICES`, `GENERAL`).
- `com.easynutri.domain.planner.MealSlotProposal`, `DailyMealPlan`, `WeeklyMealPlan`: Immutable Java 17 records modeling schedule state.

---

## 10. Idempotency & Resource Protection Patterns

### Description
Guaranteed idempotent operations prevent side effects (such as double-deducting ingredients from pantry inventory upon multiple cooking completions or overwriting manual grocery additions during weekly plan regeneration).

### Code Reference
- **Double-Deduction Prevention:** `com.easynutri.service.CookingService` checks `CookingSessionEntity.pantry_deducted` before invoking `pantryService.deductIngredients()`, guaranteeing that re-submitting or finishing a cooking session never drains inventory twice.
- **Auto-Reconciliation Isolation:** `GroceryItemEntity.is_auto_generated` flag distinguishes auto-planned items from custom user entries, allowing `groceryService.reconcileWithWeeklyPlan()` to delete and refresh auto-generated rows without affecting manual items or checked checkboxes.
