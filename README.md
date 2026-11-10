# EasyNutri — Full-Stack Clinical Nutrition Web Application

EasyNutri is an offline-capable, clinical-grade nutrition tracking, meal planning, and smart pantry management platform. Built as a full-stack responsive web application powered by **Java 17** and **Spring Boot 4.1.2**, it delivers an accessible, commercial-grade experience across desktop browsers (Windows, macOS, Linux), mobile devices (Android, iOS), tablets, and standalone Progressive Web App (PWA) installations.

---

## 1. Architecture & Module Structure

The project follows a decoupled multi-module Gradle architecture with strict separation of concerns:

```
easynutri/
├── domain/     # Pure Java 17 business logic, rules engine & calculators (Zero framework dependencies)
├── server/     # Full-stack Spring Boot 4.1.2 application (Thymeleaf UI + REST APIs + Spring Security)
├── tools/      # Java 17 dataset ingestion utilities & JSON Schema Draft-07 validation test suite
├── data/       # Standardized JSON schemas and nutrition datasets (USDA / IFCT)
├── docs/       # Master specification, feature matrix, ADRs, progress tracker, and design system
└── archive/
    └── android-prototype/  # Preserved native Android prototype (tagged: android-phase0-backup)
```

---

## 2. Prerequisites

* **Java Development Kit (JDK):** JDK 17 (or JDK 21+ with Foojay automatic toolchain provisioning).
* **Gradle:** Wrapper included (Gradle 8.14.6).
* **Browser:** Any modern browser (Chrome, Safari, Firefox, Edge).
* **Database:** In-memory H2 (Development & testing default) or PostgreSQL 16+ (Production).
* **Containerization:** Docker & Docker Compose (Optional, for containerized PostgreSQL deployment).

---

## 3. Quick Start & Execution

### A. Local Development Execution (H2 In-Memory Database)
```bash
# Windows PowerShell
.\gradlew.bat :server:bootRun

# Linux / macOS
./gradlew :server:bootRun
```
* **Application URL:** `http://localhost:8080`
* **Development Mock Account:** When running under the default `dev` profile, a local development test user (`demo`) is seeded with rotated dev password `DevDemoPass2026!#` (overridable via environment variable `EASYNUTRI_DEMO_PASSWORD`).
* **Interactive API Documentation (Swagger UI):** `http://localhost:8080/swagger-ui.html`
* **Health Check Probe:** `http://localhost:8080/api/v1/health`
* **Nutrition Sourcing & Attribution:** `http://localhost:8080/attribution`

### B. Run Automated Unit & Integration Tests (197 Tests)
```bash
# Windows
.\gradlew.bat test

# Linux / macOS
./gradlew test
```
* **Domain Unit Tests:** 79 pure Java tests (`:domain`).
* **Server Integration Tests:** 118 Spring Boot MVC, security, and repository tests (`:server`).

### C. Verify Code Formatting (Google Java Format)
```bash
.\gradlew.bat spotlessCheck
# Or format code automatically:
.\gradlew.bat spotlessApply
```

### D. Package Standalone Production Executable JAR
```bash
.\gradlew.bat :server:bootJar
```
* The compiled standalone executable JAR is located at: `server/build/libs/server.jar` (~71.9 MB).

---

## 4. Production Configuration & Security Hardening

### A. Zero Default Credentials in Production
In production mode (`SPRING_PROFILES_ACTIVE=prod`):
- **Zero demo accounts are created.**
- **Zero hardcoded credentials exist in production code.**
- The application starts with a clean user database.

### B. Secure Initial Administrator Bootstrap
To provision an initial administrator account on a clean installation, configure the following environment variables prior to initial startup:

```env
EASYNUTRI_ADMIN_BOOTSTRAP_USERNAME=admin
EASYNUTRI_ADMIN_BOOTSTRAP_EMAIL=admin@yourdomain.com
EASYNUTRI_ADMIN_BOOTSTRAP_PASSWORD=YourStrongRandomPasswordHere!
```

**Bootstrap Security Rules:**
1. Credentials must be explicitly provided in the environment; no fallback password exists.
2. The password is cryptographically hashed using BCrypt (`BCryptPasswordEncoder`).
3. If an account with `ROLE_ADMIN` already exists in the database, bootstrap execution is skipped to prevent accidental overwrites or re-hashing.
4. Passwords and secrets are never logged in application traces.
5. Once the administrator account is provisioned, **unset** these environment variables from your production environment.

### C. Required Production Environment Variables
| Variable | Description | Example / Default |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `SERVER_PORT` | HTTP port | `8080` |
| `DB_HOST` | PostgreSQL hostname | `localhost` or `postgres` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | PostgreSQL database name | `easynutri` |
| `DB_USERNAME` | PostgreSQL username | `easynutri` |
| `DB_PASSWORD` | PostgreSQL password (**Required**) | `[secure_database_password]` |
| `EASYNUTRI_ADMIN_BOOTSTRAP_USERNAME` | One-time admin setup username | `admin` |
| `EASYNUTRI_ADMIN_BOOTSTRAP_EMAIL` | One-time admin setup email | `admin@example.com` |
| `EASYNUTRI_ADMIN_BOOTSTRAP_PASSWORD` | One-time admin setup password ($\ge 8$ chars) | `[secure_admin_password]` |

---

## 5. Database Initialization, Migrations & Backups

### Database Migrations (Flyway)
EasyNutri manages database schemas automatically via Flyway migrations located under `server/src/main/resources/db/migration/`:
- `V1` – Core users, profiles, and foods catalog
- `V2` – Recipes and ingredients schema
- `V3` – Meal plans and pantry items
- `V4` – Food diary and tracking records
- `V5` – Hydration, steps, and activity burn logs
- `V6` – AI conversations and tool audit trails
- `V7` – Community recipe blog, moderation, streaks, and challenges
- `V8` – Family households, multi-user isolation, and PWA sync operations
- `V9` – Production performance indexes and query optimization

### Backup and Restore Instructions (PostgreSQL)
```bash
# 1. Create a complete database backup
pg_dump -h localhost -p 5432 -U easynutri -F c -b -v -f "easynutri_backup_$(date +%Y%m%d).dump" easynutri

# 2. Restore database from backup archive
pg_restore -h localhost -p 5432 -U easynutri -d easynutri -v "easynutri_backup_20261110.dump"
```

---

## 6. Containerized Deployment (Docker Compose)

```bash
# 1. Copy production environment template
cp .env.example .env

# 2. Edit .env with your production database credentials and bootstrap variables
nano .env

# 3. Launch PostgreSQL and EasyNutri containers
cd server
docker-compose up --build -d

# 4. View application startup logs
docker-compose logs -f app
```

---

## 7. Key Platform Features

* **Universal Accessibility & PWA:** Responsive across desktop, tablet, and mobile with dedicated mobile bottom navigation. Installable as a Progressive Web App (PWA) with Service Worker v2 offline caching and IndexedDB mutation queue.
* **Sensitive-User Mode (Feature 34):** Full-stack enforcement masking calories, weights, and deficit terminology across all HTML views, interactive Chart.js graphs, downloadable PDFs, and REST API payloads.
* **Family Profiles & Multi-User Households:** Shared household meal planning with allergen union filtering, age-based dynamic portion scaling, and strict BOLA / cross-household isolation security.
* **AI Nutrition Assistant & Safety Gate:** 13 controlled Java application tools routed through pure Java domain rules. Intercepts acute medical crises (anaphylaxis, cardiac, stroke, poisoning) and directs users to 112 / 911 emergency services immediately.
* **Clinical Content Moderation & Compassionate Gamification:** Pre-publish screening blocking medical cure claims and starvation diets; habit streaks with a 1-day forgiveness grace period.
* **Enterprise Security:** Content Security Policy (CSP), `X-Content-Type-Options: nosniff`, `X-Frame-Options: SAMEORIGIN`, session fixation migration, and BCrypt password encryption.

---

## 8. Privacy Controls & Technical Implementation Statement

EasyNutri provides robust technical controls supporting user privacy rights and data portability:
- **Self-Service Personal Data Export (`GET /settings/export-data`):** Exports a comprehensive JSON archive containing account metadata, biometrics profile, 100% of food diary entries, water logs, workout logs, step records, weight history, pantry inventory, grocery list, meal plans, saved recipes, and habit streaks.
- **Permanent Account Erasure (`POST /settings/delete-account`):** Password-verified cascading deletion that cleanly purges personal records, social interactions, AI conversations, and sync logs, while safely transferring household ownership to surviving members without corrupting shared meal plans.

> **Legal Disclaimer:** While EasyNutri implements technical privacy mechanisms aligned with data portability and erasure principles (e.g. GDPR and the Indian Digital Personal Data Protection Act 2023), formal regulatory compliance requires independent legal review based on deployment jurisdiction, organizational data processing agreements, and hosting environment.

---

## 9. Nutrition Sourcing & Clinical Boundaries

### Primary Data Sources
* **ICMR-National Institute of Nutrition (IFCT 2017):** Indian Food Composition Tables for authentic Indian regional ingredients and cooked dishes.
* **USDA FoodData Central:** Standard Reference Legacy and Foundation Foods catalogs.
* **Open Food Facts:** Barcode and packaged goods nutritional composition database.

### Clinical Disclaimer
> **IMPORTANT:** EasyNutri is intended for nutritional tracking and lifestyle dietary guidance for general wellness purposes only. It is **NOT** a certified medical diagnostic device or clinical treatment system. Always consult a qualified medical professional (physician or registered dietitian) for clinical dietary therapy, particularly for cardiovascular diseases, chronic kidney disease (CKD), or insulin-dependent diabetes. In case of acute medical emergencies, call **112** (India) or **911** (US) immediately.

### Wearable & Input Boundaries
EasyNutri supports manual entry, structured CSV import, and browser-native Web Speech API voice dictation. In strict accordance with engineering integrity, EasyNutri does **not** simulate fake background Bluetooth, Apple HealthKit, or Android Health Connect sync daemons within browser sandboxes.

---

## 10. Documentation Index

* [Master Technical Specification](file:///docs/MASTER_SPEC.md)
* [Feature Matrix (All 34 Features)](file:///docs/FEATURE_MATRIX.md)
* [Architecture Decision Records (ADRs)](file:///docs/DECISIONS.md)
* [Development Progress Tracker](file:///docs/PROGRESS.md)
* [Java OOP Syllabus Mapping](file:///docs/JAVA_CONCEPTS.md)
* [Web Design System Specification](file:///docs/DESIGN_SYSTEM.md)
