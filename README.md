# EasyNutri

EasyNutri is an offline-first, clinical-grade nutrition tracking, meal planning, and smart pantry management platform. It features a native Android client written in pure Java 17 using modern Material 3, a pure Java domain layer, an import CLI toolchain, and a Spring Boot 4.1.2 backend service.

---

## 1. Architecture & Module Structure

The project follows a decoupled multi-module Gradle architecture:

```
├── app/        # Native Android client (Java 17, XML, ViewBinding, MVVM, Room, Hilt, Material 3)
├── domain/     # Pure Java 17 business logic & rules engine (Zero Android, Zero Spring)
├── server/     # Spring Boot 4.1.2 REST API (Java 17, Springdoc OpenAPI 3.2.0, Flyway)
├── tools/      # Java 17 CLI utilities for dataset ingestion & schema validation
├── data/       # Standardized JSON schemas and sample nutrition/recipe datasets
└── docs/       # Master specification, ADRs, progress tracker, and design system docs
```

---

## 2. Prerequisites

- **Java Development Kit (JDK):** JDK 17 (or JDK 21+ with Foojay automatic toolchain provisioning).
- **Android SDK:** Command-line tools or Android Studio with API 36 (`compileSdk 36`, `targetSdk 36`).
- **Gradle:** Wrapper included (Gradle 8.14.6).
- **Docker & Docker Compose:** (Optional, for containerized backend execution).

---

## 3. Quick Start & Build Instructions

### Build Entire Project
```bash
# Windows
.\gradlew.bat assembleDebug

# Linux / macOS
./gradlew assembleDebug
```

### Run All Unit & Integration Tests
```bash
# Windows
.\gradlew.bat test :app:testDebugUnitTest

# Linux / macOS
./gradlew test :app:testDebugUnitTest
```

### Verify Code Formatting (Google Java Format)
```bash
# Check code style
.\gradlew.bat spotlessCheck

# Automatically format code
.\gradlew.bat spotlessApply
```

---

## 4. Running the Modules

### Android Client (`app`)
1. Assemble the debug APK:
   ```bash
   .\gradlew.bat :app:assembleDebug
   ```
2. The compiled APK is located at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
3. Install to a connected device or emulator:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

### Spring Boot Backend (`server`)

#### Option A: Local Development (In-Memory H2 Database)
```bash
.\gradlew.bat :server:bootRun
```
The server will start on port `8080` under the `dev` profile.
- Health Check: `http://localhost:8080/api/v1/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI Specification: `http://localhost:8080/v3/api-docs`

#### Option B: Docker Compose (PostgreSQL 16)
```bash
# 1. Copy the environment configuration template
cp .env.example .env

# 2. Start PostgreSQL and backend container
cd server
docker-compose up --build -d
```

### Dataset Import Utility (`tools`)
```bash
.\gradlew.bat :tools:run --args="import data/foods/sample_food.json"
```

---

## 5. Non-Negotiable Architecture Constraints

- **Pure Java:** Zero Kotlin. Zero Jetpack Compose.
- **Android UI:** Android XML layouts with ViewBinding and Material 3 Day/Night theme.
- **Data Sovereignty:** Room database configured without `fallbackToDestructiveMigration()`. All migrations must be explicitly written and tested.
- **Security:** No hardcoded database credentials or fake JWT authentication tokens.
- **Pure Domain:** The `domain` module contains zero framework dependencies.

---

## 6. Documentation Index

- [Master Specification](file:///docs/MASTER_SPEC.md)
- [Architecture Decision Records (ADRs)](file:///docs/DECISIONS.md)
- [Phase Progress Tracker](file:///docs/PROGRESS.md)
- [Java OOP & Syllabus Mapping](file:///docs/JAVA_CONCEPTS.md)
- [Design System & Accessibility Guide](file:///docs/DESIGN_SYSTEM.md)

