# EasyNutri — Architecture Decision Records (ADR)

## ADR-001: Multi-Module Architecture & Java 17 Toolchain

### Status
Accepted

### Context
EasyNutri requires a scalable, maintainable, and testable foundation supporting 33 clinical, nutrition, and pantry features across native Android, shared domain logic, backend REST services, and dataset import CLI utilities. The syllabus and academic evaluation enforce standard Java OOP principles and zero Kotlin.

### Decision
We partition the repository into a multi-module Gradle project:
- `:app`: Android application using Java 17, XML layouts, ViewBinding, and Material 3.
- `:domain`: Pure Java 17 business logic module with zero Android and zero Spring dependencies.
- `:server`: Spring Boot 4.1.2 service using Java 17.
- `:tools`: Java command-line utilities for dataset validation and import.
- `data/`: Version-controlled JSON schemas and nutrition datasets.

All modules standardize on Java 17 via Gradle toolchains resolved by Foojay Convention Plugin.

### Consequences
- Business logic in `:domain` can be thoroughly unit tested in milliseconds without Android emulators or Spring container overhead.
- Gradle version catalog (`gradle/libs.versions.toml`) centralizes all dependency versions.
- Strict isolation prevents Android UI or Spring web constructs from leaking into core nutrition algorithms.

---

## ADR-002: Android Tech Stack — Java, XML Layouts, ViewBinding, MVVM, and Material 3

### Status
Accepted

### Context
EasyNutri is evaluated as a pure Java project adhering strictly to classic Android architecture principles without Jetpack Compose or Kotlin runtime dependencies.

### Decision
- **Language:** Pure Java 17.
- **UI Toolkit:** Android XML layouts with ViewBinding enabled.
- **Architecture:** MVVM with `ViewModel` and `LiveData` from AndroidX Lifecycle.
- **Design System:** Google Material 3 (`Theme.Material3.DayNight.NoActionBar`).
- **Dependency Injection:** Hilt (`hilt-android` with Java `annotationProcessor`).
- **Navigation:** Single Activity (`MainActivity`) with Android Navigation Component (`nav_graph.xml`) and `BottomNavigationView`.

### Consequences
- Complete compliance with Java syllabus and evaluation rules.
- Type-safe view bindings without `findViewById` boilerplate or synthetic views.
- Clean separation of presentation from UI state and business logic.

---

## ADR-003: Spring Boot 4.1.2 Backend Foundation with Independent Docker Build Context

### Status
Accepted

### Context
The backend serves sync, aggregation, and community capabilities in subsequent phases. It must support high-throughput cloud deployment and modern Spring ecosystem standards while remaining independently buildable inside minimal Docker containers without requiring an Android SDK.

### Decision
- Upgraded backend to Spring Boot 4.1.2 and Springdoc OpenAPI 3.2.0.
- Implemented `docker-compose.yml` with `context: ..` pointing to the repository root.
- Added conditional evaluation in `settings.gradle`: when `BACKEND_BUILD_ONLY=true` is set, the `:app` Android module is excluded, allowing `server:bootJar` to build in standard Alpine/Temurin JRE/JDK containers without Android SDK.
- Auth endpoints are explicitly stubbed as Phase 6 development placeholders without generating fake/demo JWT tokens.

### Consequences
- Fast, secure, reproducible containerized builds.
- Clear API contracts documented via OpenAPI/Swagger.
- No dummy credentials or pseudo-security in early phases.

---

## ADR-004: Room Database Offline-First with Zero Destructive Migration Fallback

### Status
Accepted

### Context
EasyNutri prioritizes offline capability and health data sovereignty. A user's health profile, allergies, meal logs, and local nutrition history must never be wiped silently when updating the app.

### Decision
- Configured Room Database version 1 with `exportSchema = true`.
- Explicitly rejected `fallbackToDestructiveMigration()`.
- All future database schema modifications must provide explicit versioned `Migration` implementations (`MIGRATION_1_2`, etc.) with comprehensive automated migration tests.

### Consequences
- User data cannot be inadvertently destroyed during schema evolution.
- Schema changes are tracked in version-controlled JSON schema files under `app/schemas/`.

---

## ADR-005: Type-Safe Sealed Domain Safety Candidates and Strict Domain Isolation

### Status
Accepted

### Context
The nutrition rules engine must validate diverse entities (individual food items, compiled recipes, custom pantry meals) against clinical rules and user health profiles without resorting to untyped generic `Object` parameters or loose maps.

### Decision
- Defined a sealed Java 17 interface: `SafetyCandidate permits FoodCandidate, RecipeCandidate`.
- Enforced immutability via Java records (`ProfileContext`, `FoodCandidate`, `RecipeCandidate`, `RuleVerdict`, `SafetyResult`).
- Defensive copies created for all collection fields (`List.copyOf`, `Set.copyOf`).
- Pure Java domain interfaces (`SafetyGate`, `RulesEngine`, `NutritionCalculator`) accept strongly typed domain models.

### Consequences
- Compile-time exhaustiveness checking and type safety.
- Domain logic is decoupled from persistence entities and network DTOs.

