# EasyNutri — Master Technical Specification (Web Application Architecture)

**Official Architecture Directive · November 2026**

---

## 1. Executive Summary & Product Architecture

EasyNutri is an offline-capable, clinical-grade nutrition tracking, meal planning, and smart pantry management platform. 

### Architecture Pivot
Under the **Official Web Application Development Directive**, EasyNutri is built as a **Full-Stack Responsive Web Application** accessible across all modern browsers on:
* **Desktop:** Windows, macOS, Linux.
* **Mobile & Tablets:** Android phones/tablets, iPhones, iPads.
* **Progressive Web App (PWA):** Installable as a standalone app on supporting operating systems.

The previous native Android implementation has been safely preserved in `archive/android-prototype/` and tagged on branch `android-phase0-backup`. All active development targets the unified Spring Boot full-stack architecture.

---

## 2. Technology Stack

### Backend & Service Layer
* **Language & Runtime:** Java 17 (Eclipse Temurin toolchain).
* **Framework:** Spring Boot 4.1.2.
* **Web Layer:** Spring MVC (Web Controllers & REST APIs).
* **Template Engine:** Thymeleaf 3.x with clean fragment architecture.
* **Persistence & ORM:** Spring Data JPA with Hibernate ORM.
* **Database:** PostgreSQL 16 (production), H2 in-memory (local development & unit testing).
* **Schema Evolution:** Flyway database migrations (`db/migration/`).
* **Security:** Spring Security with BCrypt password hashing, CSRF protection, and session-based authentication.
* **API Documentation:** Springdoc OpenAPI 3.2.0 (Swagger UI).
* **Build System:** Gradle 8.14.6 with Gradle Version Catalog (`gradle/libs.versions.toml`).
* **Code Formatting:** Spotless 6.25.0 with Google Java Format 1.22.0.

### Frontend & Client Layer
* **Markup & Templating:** HTML5 semantic structure rendered via Thymeleaf fragments.
* **Styles:** Custom responsive CSS3 design system (`main.css`) with CSS custom properties, 8pt spacing grid, responsive CSS Grid/Flexbox, rounded cards, and light/dark modes.
* **Client Scripting:** Modern vanilla JavaScript (`app.js`, `pwa.js`) without heavy node frameworks (zero React/Next.js/Angular/Node.js).
* **Iconography:** Scalable Lucide-style embedded SVGs.
* **Visualizations:** Chart.js for macronutrient splits and caloric adherence trends.
* **PWA & Offline:** Web App Manifest (`manifest.webmanifest`), Service Worker (`sw.js`), offline fallback (`offline.html`), and live network sync monitoring.

### Active Gradle Modules
1. `:domain` – Pure Java 17 business logic, rules engine, and calculators (Zero framework dependencies).
2. `:server` – Spring Boot web application containing both Thymeleaf web UI and REST API controllers.
3. `:tools` – Command-line ingestion and JSON Schema Draft-07 validation test suite.

---

## 3. UI/UX Visual Identity & Layout System

### Brand Design Direction
* **Tone:** Premium, Minimal, Modern, Clean, Friendly, Health-Focused, Professional.
* **Palette:**
  * Primary Green: `#237A57` (Action buttons, branding, active navigation indicators)
  * Mint: `#E8F5EC` (Highlights, chips, badge backgrounds)
  * Background: `#F8FAF7` (Canvas background)
  * Surface: `#FFFFFF` (Cards, sidebars, headers)
  * Main Text: `#1C2922` (High-contrast typography)
  * Secondary Text: `#78847D` (Subtitles and helper labels)
  * Accent Orange: `#FFAD73` (Energy badges, warnings, hydration highlights)

### Responsive Layout
* **Desktop (>1024px):** Fixed 260px left sidebar with 13 core navigation destinations; sticky top header with search, sensitive-mode toggle, theme toggle, and profile dropdown; centered 1400px responsive grid.
* **Mobile (<768px):** Collapsible slide-in sidebar drawer; fixed 64px bottom navigation bar with 5 primary touch destinations (`Home`, `Recipes`, `Diary`, `Pantry`, `Assistant`); minimum 44x44px touch targets; zero horizontal scrolling.

### 13 Primary Navigation Destinations
1. **Dashboard** (`/dashboard`): Daily calories, macros, hydration, step burn, pantry alerts, and meal plan summary.
2. **Discover Recipes** (`/recipes`): Verified clinical recipes, serving scaler, cook time, and macro profiles.
3. **Ingredient Finder** (`/recipes/finder`): Multi-input ingredient search (typed, voice, barcode, photo OCR) with match percentage badges.
4. **Meal Planner** (`/planner`): 7-day personalized weekly schedule with macro balancing.
5. **Food Diary** (`/diary`): Daily meal timeline, macro breakdown, and quick food logging.
6. **My Pantry** (`/pantry`): Shelf-life tracker, expiration alerts, and Smart Pantry Suggestions.
7. **Grocery List** (`/grocery`): Auto-consolidated shopping list derived from meal plans minus pantry stock.
8. **Activity** (`/tracking`): MET-based energy expenditure, manual step logging, and water intake counter.
9. **Progress Reports** (`/reports`): 7-day caloric adherence charts, macro split analysis, and PDF export preview.
10. **AI Nutrition Assistant** (`/assistant`): Verified natural language chat executing Java tools with clinical safety gate audits.
11. **Community** (`/community`): Moderated community recipe feed, comments, likes, badges, and streaks.
12. **Family Profiles** (`/family`): Household multi-profile switcher managing combined dietary restrictions.
13. **Settings** (`/settings`): Sensitive-User Mode toggle, dark theme switcher, global allergy filters, and language.

---

## 4. Browser Adaptation & Platform Limitations

Browser environments cannot access native hardware in the same manner as Android:
1. **Wearables & Sensors:** Health Connect is Android-specific. In the web platform, step and activity tracking use manual logging and wearable data file imports (CSV, FIT, JSON). **Sensor readings are never fabricated.**
2. **Multi-Input Ingestion:** Web Speech API provides voice input with a typed input fallback. Browser Camera & BarcodeDetector APIs support camera scanning with manual barcode entry fallback. Photo & bill OCR use server-side upload processing.
3. **Offline & Sync:** Progressive Web App Service Worker caches static assets and offline pages. IndexedDB queues offline diary entries and planner updates, automatically syncing upon network reconnection.
4. **Sensitive-User Mode (Feature 33):** Client-side toggle (`body.sensitive-mode`) blurs numerical metrics and replaces them with qualitative wellness badges ("Optimal Balance", "Nutrient Dense", "High Protein").

---

## 5. Pure Java Business Logic Architecture (`:domain`)

Core nutrition logic remains strictly encapsulated in pure Java 17:
* `NutritionCalculator`: Macronutrient math, energy density, and serving multiplier calculations.
* `RulesEngine`: Clinical rule evaluation against diabetic, hypertensive, renal, and allergy constraints.
* `SafetyGate`: Strict validation intercepting proposed meals or foods that violate health constraints.
* `RecipeMatcher`: Ingredient-based candidate ranking and pantry leftover matching.
* `MealPlanner`: Weekly schedule synthesis enforcing daily macro and calorie bounds.
* `GroceryBuilder`: Compiling consolidated grocery lists subtracting pantry stock.
* `DeficiencyService`: Identifying micronutrient gaps (Iron, Calcium, B12, Vitamin D) and recommending nutrient-dense foods.
* `BurnCalculator`: Mifflin-St Jeor BMR, TDEE, and MET activity energy expenditure.
* `InsightEngine`: Clinical habit insights derived from multi-day food logs.
* `RecipeAdapter`: Ingredient substitutions and serving scalers.

---

## 6. Development Phases (Web Application)

* **Phase 0:** Web Application Scaffolding, Project Protection, and Architecture Transition *(Completed)*.
* **Phase 1:** Authentication, Onboarding Wizard, User Profiles & Nutrition Foundation.
* **Phase 2:** Food Discovery, Serving Scaler & Multi-Input Ingredient Finder.
* **Phase 3:** Meal Planning, Smart Pantry Suggestions & Automatic Grocery List.
* **Phase 4:** Food Diary, MET Energy Burn & Hydration Tracking.
* **Phase 5:** AI Nutrition Assistant, Tool Execution & Safety Gate Verification.
* **Phase 6:** Community Recipe Blog, Content Moderation & Streaks/Badges.
* **Phase 7:** Family Profiles, Offline PWA Sync, Accessibility & Sensitive-User Mode.
* **Phase 8:** Comprehensive Testing, Security Hardening & Production Release.
