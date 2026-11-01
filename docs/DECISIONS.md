# EasyNutri — Architecture Decision Records (ADR)

## ADR-001: Multi-Module Architecture & Java 17 Toolchain

### Status
Accepted

### Context
EasyNutri requires a scalable, maintainable, and testable foundation supporting all 34 clinical, nutrition, and pantry features across shared pure Java domain logic, backend REST services, and dataset import CLI utilities. The syllabus and evaluation enforce standard Java OOP principles and zero Kotlin.

### Decision
We partition the active repository into a 3-module Gradle project:
- `:domain`: Pure Java 17 business logic module with zero framework dependencies.
- `:server`: Spring Boot 4.1.2 full-stack web application (Thymeleaf UI + REST API) on Java 17.
- `:tools`: Java command-line utilities for dataset validation and import.
- `data/`: Version-controlled JSON schemas and nutrition datasets.
- `archive/android-prototype/`: Archived Android prototype safely preserved for reference.

All modules standardize on Java 17 via Gradle toolchains resolved by Foojay Convention Plugin.

---

## ADR-002: Android Tech Stack — Java, XML Layouts, ViewBinding, MVVM (Archived)

### Status
Superseded by ADR-006

### Context
Initial Phase 0 prototype evaluated native Android development.

### Decision
The Android application has been safely moved to `archive/android-prototype/` and tagged on branch `android-phase0-backup`. All primary development is shifted to the responsive Spring Boot web application.

---

## ADR-003: Spring Boot 4.1.2 Backend Foundation with Independent Docker Build Context

### Status
Accepted

### Context
The backend serves both browser HTML views and REST APIs. It must support high-throughput cloud deployment and modern Spring ecosystem standards while remaining independently buildable inside minimal Docker containers.

### Decision
- Standardized backend on Spring Boot 4.1.2 and Springdoc OpenAPI 3.2.0.
- Implemented `docker-compose.yml` with `context: ..` pointing to the repository root.
- Multi-stage `Dockerfile` compiles `server:bootJar` in an Eclipse Temurin 17 container.
- Real Spring Security authentication with BCrypt hashing and session-based auth.

---

## ADR-004: Relational Persistence with PostgreSQL & H2 Offline Development

### Status
Accepted

### Context
EasyNutri prioritizes data integrity and health data sovereignty. All user profiles, meal plans, pantry items, and logs must persist across application restarts.

### Decision
- Configured PostgreSQL 16 for production and H2 in-memory mode for rapid local development/testing.
- Versioned Flyway migrations track all relational schema changes under `db/migration/`.
- Strict JPA/Hibernate entities mapped for users, profiles, pantry, and meal plans.

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
- Pure Java domain interfaces (`SafetyGate`, `RulesEngine`, `NutritionCalculator`, `RecipeMatcher`, `MealPlanner`, `GroceryBuilder`, `DeficiencyService`, `BurnCalculator`, `InsightEngine`, `RecipeAdapter`).

---

## ADR-006: Pivot from Native Android Application to Full-Stack Responsive Web Application

### Status
Accepted

### Context
The user directive mandates making EasyNutri universally accessible across Windows, macOS, Linux, Android, iPhone, and tablets through modern web browsers, with Progressive Web App (PWA) installation capabilities, rather than constraining users to an Android-only binary.

### Decision
1. Backed up the Android implementation to Git branch & tag `android-phase0-backup`.
2. Moved Android source files to `archive/android-prototype/`.
3. Standardized active Gradle configuration to `:domain`, `:server`, and `:tools`.
4. Transformed `:server` into the unified full-stack application hosting responsive web interfaces, REST endpoints, and security.

---

## ADR-007: UI/UX Architecture — Thymeleaf, Vanilla JavaScript, Lucide Icons, and Chart.js

### Status
Accepted

### Context
To maintain strict Java ecosystem purity, zero Node.js backend dependencies, and fast server-side rendering, we selected a lightweight, robust web stack.

### Decision
- **Template Engine:** Thymeleaf 3.x with component fragments (`layout/main.html`, `fragments/sidebar.html`, `fragments/topbar.html`).
- **Styling:** Custom CSS3 design system (`main.css`) using CSS variables, 8pt spacing grid, responsive CSS Grid/Flexbox, rounded cards, and light/dark modes.
- **Client Scripting:** Modern vanilla JavaScript (`app.js`, `pwa.js`) for interactive drawer toggles, toast notifications, sensitive-mode blurring, and offline detection.
- **Iconography:** Lucide-style embedded SVGs providing lightweight, scalable icons without heavy icon webfont dependencies.
- **Charts:** Chart.js for responsive macro distributions and calorie trend analytics.

---

## ADR-008: Progressive Web App (PWA) Offline Strategy & IndexedDB Sync Queue

### Status
Accepted

### Context
EasyNutri must maintain core offline utility when network connectivity is lost (e.g. checking pantry items or reviewing planned recipes).

### Decision
- Configured Web App Manifest (`manifest.webmanifest`) enabling PWA installation ("Add to Home Screen").
- Service Worker (`sw.js`) caches critical static assets and provides an offline fallback page (`offline.html`).
- Dynamic status badge in the top navigation bar displays live network status ("Online" vs "Offline Mode").
- Browser IndexedDB holds pending diary logs and offline meal plan modifications, queueing them for automatic REST synchronization upon network reconnection.


---

## ADR-009: Phase 1 Relational Data Model & Pure Java Clinical Rules Architecture

### Status
Accepted

### Context
Phase 1 introduces user profile persistence, health setups, clinical calorie calculations, canonical food/recipe data models, dynamic serving scalers, and smart pantry recommendations. All business logic must remain in pure Java with zero web/Spring coupling, while relational tables must support clean schema migrations and user data privacy.

### Decision
1. **Relational Schema Evolution (`V2__phase1_schema.sql`):** Added `user_profiles`, `user_allergies`, `user_health_conditions`, `foods`, `recipes`, `recipe_ingredients`, and `pantry_items` tables with foreign keys and cascade deletions.
2. **Pure Java Domain Calculations:** Implemented `BmiCalculator` (WHO criteria), `NutritionGoalCalculator` (Mifflin-St Jeor BMR + TDEE + clinical floors of 1200/1500 kcal and adolescent/pregnancy safety guards), `DefaultNutritionCalculator` (exact per-100g scaling), `DefaultRecipeAdapter` (serving scaler and safe allergen substitution), and `DefaultRulesEngine` (hard allergen blocks with Indian/international synonyms, dietary checks, and condition limit audits).
3. **Canonical Data Sourcing:** Populated genuine Indian food items and classic recipes with source attribution (`IFCT` 2017 & `USDA FoodData Central`), and marked demo records explicitly with `verified: false`.
4. **Onboarding & Dynamic Dashboard:** Created a 3-step responsive onboarding wizard (`/onboarding`) redirecting new users before dashboard access, and integrated real database-backed clinical targets into `/dashboard`.
