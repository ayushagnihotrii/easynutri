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
* **Default Demo Account:**
  * **Username:** `demo`
  * **Password:** `Password123!`
* **Interactive API Documentation (Swagger UI):** `http://localhost:8080/swagger-ui.html`
* **Health Check Probe:** `http://localhost:8080/api/v1/health`

### Run All Automated Unit & Integration Tests
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
* The compiled standalone executable JAR is located at: `server/build/libs/server.jar`.

---

## 4. Key Platform Features

* **Universal Accessibility:** Responsive across desktop, tablet, and mobile with dedicated mobile bottom navigation.
* **Progressive Web App (PWA):** Installable as a desktop/mobile app via `manifest.webmanifest` and Service Worker (`sw.js`). Includes offline caching and live sync monitoring.
* **Sensitive-User Mode (Feature 33):** One-click toggle masking calories and numbers with blur and qualitative wellness badges ("Optimal Balance", "Nutrient Dense").
* **Multi-Input Ingredient Entry (Feature 9):** Supports typed entry, browser voice recognition, camera barcode scanning, and receipt/label photo OCR with manual fallbacks.
* **AI Nutrition Assistant (Features 22 & 23):** Natural language queries routed through pure Java tools and verified against the clinical safety gate. Deterministic demo mode active when no remote key is set.
* **Real Authentication:** Database-backed user management with BCrypt password hashing, CSRF protection, and session-based authentication.

---

## 5. Containerized Production Deployment (Docker Compose)

```bash
# 1. Copy environment template
cp .env.example .env

# 2. Launch PostgreSQL 16 and backend container
cd server
docker-compose up --build -d
```

---

## 6. Documentation Index

* [Master Technical Specification](file:///docs/MASTER_SPEC.md)
* [Feature Matrix (All 34 Features)](file:///docs/FEATURE_MATRIX.md)
* [Architecture Decision Records (ADRs)](file:///docs/DECISIONS.md)
* [Development Progress Tracker](file:///docs/PROGRESS.md)
* [Java OOP Syllabus Mapping](file:///docs/JAVA_CONCEPTS.md)
* [Web Design System Specification](file:///docs/DESIGN_SYSTEM.md)
