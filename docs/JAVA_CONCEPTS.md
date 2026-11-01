# EasyNutri — Java OOP Architecture & Syllabus Mapping

This document provides a systematic mapping of core Object-Oriented Programming (OOP) concepts and modern Java 17 features implemented in EasyNutri.

---

## 1. Encapsulation & Immutability

### Description
Data hiding and state protection prevent external entities from placing objects into inconsistent states. In Java 17, immutable records provide state encapsulation with built-in accessors, `equals()`, `hashCode()`, and `toString()`.

### Code Reference
- **Domain Records:**
  - `com.easynutri.domain.model.FoodCandidate`: Immutable candidate model for food evaluation.
  - `com.easynutri.domain.model.RecipeCandidate`: Immutable candidate model for multi-ingredient recipes.
  - `com.easynutri.domain.model.ProfileContext`: Immutable user context (allergies, conditions, goals).
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
- `com.easynutri.domain.rules.SafetyGate`: Contract for safety checks against clinical profiles.
- `com.easynutri.domain.nutrition.NutritionCalculator`: Contract for nutrient math.
- `com.easynutri.app.ui.policy.DisplayPolicy`: Contract for user-specific display formatting (sensitive user filtering).
- `com.easynutri.app.data.local.ProofDao`: Database persistence abstraction completely decoupled from Room SQL internals.

---

## 3. Polymorphism & Strategy Pattern

### Description
Different implementations can be substituted at runtime behind a common interface without client code awareness.

### Code Reference
- **Interface:** `DisplayPolicy`
  ```java
  public interface DisplayPolicy {
    String formatCalories(double calories);
    boolean shouldDisplayCalorieCount();
    boolean shouldDisplayWeightMetrics();
  }
  ```
- **Default Strategy:** `DefaultDisplayPolicy` displays standard kcal strings and metrics.
- **Sensitive Strategy (Phase 7 / Feature 33):** Alternative implementation masking trigger metrics with qualitative badges ("Nutrient Dense", "Balanced Fuel").
- **Substitution:** Injected via Hilt (`@Provides DisplayPolicy provideDisplayPolicy()`), allowing zero-impact strategy switching.

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
- Permits only `FoodCandidate` and `RecipeCandidate`. Any future addition requires deliberate declaration in the permits clause.

---

## 5. Custom Exception Hierarchy

### Description
Hierarchical error handling rooted in a domain-specific runtime exception to distinguish domain failures from unexpected system errors.

### Code Reference
- `com.easynutri.domain.exception.EasyNutriException`: Base unchecked application exception.
- `com.easynutri.domain.exception.ValidationException`: Specific exception carrying structured field-level error messages (`Map<String, String> errors`).

---

## 6. Compile-Time Annotation Processing & Inversion of Control

### Description
Decoupling class creation and lifecycle management through dependency injection and declarative metadata.

### Code Reference
- **Android Dependency Injection:**
  - `@HiltAndroidApp` on `EasyNutriApplication`
  - `@AndroidEntryPoint` on `MainActivity` and fragments
  - `@Module` and `@InstallIn(SingletonComponent.class)` in `DatabaseModule` and `NetworkModule`
- **Room ORM:**
  - `@Entity`, `@PrimaryKey`, `@ColumnInfo` on `ProofEntity`
  - `@Dao`, `@Query`, `@Insert` on `ProofDao`
  - `@Database` on `AppDatabase`
- **Spring Boot REST:**
  - `@RestController`, `@GetMapping`, `@PostMapping` on `HealthController` and `AuthStubController`

