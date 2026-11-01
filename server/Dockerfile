# Stage 1: Build using root project context
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

# Instruct settings.gradle to configure only backend modules
ENV BACKEND_BUILD_ONLY=true

# Copy root Gradle distribution and wrapper
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle .

# Copy module sources
COPY domain/ domain/
COPY tools/ tools/
COPY server/ server/

# Compile server bootJar
RUN chmod +x gradlew && ./gradlew :server:bootJar --no-daemon

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN addgroup --system easynutri && adduser --system --ingroup easynutri easynutri
USER easynutri:easynutri

COPY --from=build /workspace/server/build/libs/*.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]

