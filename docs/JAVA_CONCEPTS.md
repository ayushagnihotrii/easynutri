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
