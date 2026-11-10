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
* **Docker:** (Optional, for containerized PostgreSQL deployment).

---

## 3. Quick Start & Local Execution

### Run Web Application Locally (In-Memory H2 Database)
```bash
# Windows
.\gradlew.bat :server:bootRun

# Linux / macOS
./gradlew :server:bootRun
```
* **Application URL:** `http://localhost:8080`
* **Default Accounts:**
  * **Standard User:** `demo` / `Password123!`
  * **Administrator:** `admin` / `AdminSecure2026!` (Access to `/admin/moderation`)
* **Interactive API Documentation (Swagger UI):** `http://localhost:8080/swagger-ui.html`
* **Health Check Probe:** `http://localhost:8080/api/v1/health`
* **Nutrition Sourcing & Attribution:** `http://localhost:8080/attribution`

### Run All Automated Unit & Integration Tests (189 Tests)
```bash
# Windows
.\gradlew.bat test

# Linux / macOS
./gradlew test
```

### Verify Code Formatting (Google Java Format)
```bash
.\gradlew.bat spotlessCheck
```

### Package Production Executable JAR
```bash
.\gradlew.bat :server:bootJar
```
* The compiled standalone executable JAR is located at: `server/build/libs/server.jar` (~71.9 MB).

---

## 4. Key Platform Features

* **Universal Accessibility & PWA:** Responsive across desktop, tablet, and mobile with dedicated mobile bottom navigation. Installable as a Progressive Web App (PWA) with Service Worker v2 offline caching and IndexedDB mutation queue.
* **Sensitive-User Mode (Feature 34):** Full-stack enforcement masking calories, weights, and deficit terminology across all HTML views, interactive Chart.js graphs, downloadable PDFs, and REST API payloads.
* **Family Profiles & Multi-User Households:** Shared household meal planning with allergen union filtering, age-based dynamic portion scaling, and strict BOLA / cross-household isolation security.
* **AI Nutrition Assistant & Safety Gate:** 13 controlled Java application tools routed through pure Java domain rules. Intercepts acute medical crises (anaphylaxis, cardiac, stroke, poisoning) and directs users to 112 / 911 emergency services immediately.
* **Data Sovereignty & Privacy (DPDP Act 2023 / GDPR):** Self-service machine-readable JSON data export (`/settings/export-data`) and complete cascading account deletion with password verification (`/settings/delete-account`).
* **Clinical Content Moderation & Compassionate Gamification:** Pre-publish screening blocking medical cure claims and starvation diets; habit streaks with a 1-day forgiveness grace period.
* **Enterprise Security:** Content Security Policy (CSP), `X-Content-Type-Options: nosniff`, `X-Frame-Options: SAMEORIGIN`, session fixation migration, and BCrypt password encryption.

---

## 5. Nutrition Sourcing & Clinical Disclaimer

### Primary Data Sources
* **ICMR-National Institute of Nutrition (IFCT 2017):** Indian Food Composition Tables for authentic Indian regional ingredients and cooked dishes.
* **USDA FoodData Central:** Standard Reference Legacy and Foundation Foods catalogs.
* **Open Food Facts:** Barcode and packaged goods nutritional composition database.

### Clinical Disclaimer
> **IMPORTANT:** EasyNutri is intended for nutritional tracking and lifestyle dietary guidance for general wellness purposes only. It is **NOT** a certified medical diagnostic device or clinical treatment system. Always consult a qualified medical professional (physician or registered dietitian) for clinical dietary therapy, particularly for cardiovascular diseases, chronic kidney disease (CKD), or insulin-dependent diabetes. In case of acute medical emergencies, call **112** (India) or **911** (US) immediately.

### Wearable & Input Transparency
EasyNutri supports manual entry, CSV import, and browser-native Web Speech API voice dictation. In strict accordance with engineering integrity, EasyNutri does **not** simulate fake background Bluetooth, Apple HealthKit, or Android Health Connect sync daemons within browser sandboxes.

---

## 6. Containerized Production Deployment (Docker Compose)

```bash
# 1. Copy environment template
cp .env.example .env

# 2. Launch PostgreSQL 16 and backend container
cd server
docker-compose up --build -d
```

---

## 7. Documentation Index

* [Master Technical Specification](file:///docs/MASTER_SPEC.md)
* [Feature Matrix (All 34 Features)](file:///docs/FEATURE_MATRIX.md)
* [Architecture Decision Records (ADRs)](file:///docs/DECISIONS.md)
* [Development Progress Tracker](file:///docs/PROGRESS.md)
* [Java OOP Syllabus Mapping](file:///docs/JAVA_CONCEPTS.md)
* [Web Design System Specification](file:///docs/DESIGN_SYSTEM.md)
