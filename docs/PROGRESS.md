# EasyNutri — Development Progress Tracker (Web Platform)

## Status Overview

| Phase | Description | Status | Verification Status |
|---|---|---|---|
| **Phase 0** | **Web Application Architecture Transition & Scaffolding** | **COMPLETE** | **All tests passing, bootJar verified** |
| Phase 1 | Authentication, Onboarding, Profiles & Nutrition Foundation | Planned | Ready to begin |
| Phase 2 | Food Discovery, Serving Scaler & Multi-Input Finder | Planned | Pending |
| Phase 3 | Meal Planning, Smart Pantry Suggestions & Grocery List | Planned | Pending |
| Phase 4 | Food Diary, Energy Burn & Hydration Tracking | Planned | Pending |
| Phase 5 | AI Assistant, Tool Execution & Clinical Safety Gate | Planned | Pending |
| Phase 6 | Community Recipe Blog, Moderation & Badges | Planned | Pending |
| Phase 7 | Family Profiles, Offline PWA Sync & Sensitive Mode | Planned | Pending |
| Phase 8 | Comprehensive Testing, Security Audit & Production Release | Planned | Pending |

---

## Phase 0 Implementation Summary (Web Transition)

### 1. Project Protection & Scaffolding
- **Git Backup:** Created Git backup branch and tag `android-phase0-backup` preserving initial Android prototype.
- **Archive Preservation:** Safely relocated Android source files to `archive/android-prototype/`.
- **Active Build:** Configured `settings.gradle` with the 3 active modules: `:domain`, `:server`, `:tools`.
- **Toolchains:** Java 17 toolchain across all modules; Gradle 8.14.6 wrapper; Spring Boot 4.1.2; Springdoc 3.2.0; Spotless 6.25.0 with Google Java Format 1.22.0.

### 2. Full-Stack Web Application (`:server`)
- **Web UI & Templating:** Spring MVC + Thymeleaf with responsive desktop sidebar (13 destinations), sticky topbar with search and user menu, and mobile bottom navigation drawer.
- **Design System:** Full custom CSS3 design system (`main.css`) using brand palette (`#237A57`, `#E8F5EC`, `#F8FAF7`, `#FFFFFF`, `#1C2922`, `#78847D`, `#FFAD73`), rounded cards (16px), 8pt grid, and complete dark mode overrides.
- **Real Authentication:** Database-backed user management via `UserEntity`, `UserRepository`, `UserService`, `CustomUserDetailsService`, with BCrypt password hashing, CSRF protection, and session-based auth. Default demo user (`demo` / `Password123!`) seeded on startup.
- **Progressive Web App (PWA):** `manifest.webmanifest`, Service Worker (`sw.js`) caching assets, `offline.html` fallback, and live connectivity badge (`#networkSyncBadge`).
- **Sensitive-User Mode:** One-click toggle (`#sensitiveToggleBtn`) masking calorie and weight metrics with visual blur and qualitative wellness labels.
- **REST APIs:** Open endpoints for `GET /api/v1/health`, `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, and verified AI chat `POST /api/v1/assistant/chat`.
- **AI Architecture:** `LlmClient` interface with `DeterministicLlmClient` executing pure Java tools without hallucinating numbers.

### 3. Pure Java Domain Module (`:domain`)
- Complete isolation from web/framework dependencies.
- Added domain contracts: `RecipeMatcher`, `MealPlanner`, `GroceryBuilder`, `DeficiencyService`, `BurnCalculator`, `InsightEngine`, `RecipeAdapter`.
- Implemented `DefaultBurnCalculator` calculating Mifflin-St Jeor BMR, TDEE, and MET activity burn.
- Tested: `BurnCalculatorTest`, `DomainArchitectureTest`, `DomainSmokeTest`.

### 4. Tools Module (`:tools`)
- CLI dataset importer and JSON Schema Draft-07 validation test suite (`SchemaValidationTest`).

---

## Verification Test Results

| Command | Target | Result | Output |
|---|---|---|---|
| `.\gradlew.bat spotlessCheck` | Multi-module code formatting | **PASSED** | Google Java Format compliant |
| `.\gradlew.bat :domain:test` | Pure Java domain calculations | **PASSED** | 3 tests passed |
| `.\gradlew.bat :tools:test` | CLI & JSON schemas | **PASSED** | Schema validation passed |
| `.\gradlew.bat :server:test` | Spring Boot web & API tests | **PASSED** | 6 web integration tests passed |
| `.\gradlew.bat test` | Full project test suite | **PASSED** | 11 actionable tasks passed |
| `.\gradlew.bat :server:bootJar` | Executable Spring Boot JAR | **PASSED** | Generated `server/build/libs/server.jar` (67.5 MB) |
