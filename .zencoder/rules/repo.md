---
description: Repository Information Overview
alwaysApply: true
---

# MovieFlix Information

## Summary

MovieFlix is an Android application for tracking movies and TV shows. It allows users to discover new content, manage watchlists, track viewing progress, and share movie recommendations. The app is built with Kotlin using MVVM architecture and integrates with TMDB API.

## Structure

- **app/**: Main Android application module
  - **src/main/java/com/example/movieflix/**: Source code organized in Clean Architecture
  - **src/main/res/**: Android resources (layouts, drawables, etc.)
- **gradle/**: Gradle wrapper and configuration
- **images/**: App screenshots and promotional images
- **gallery/**: Logo variations

## Language & Runtime

**Language**: Kotlin
**Version**: Kotlin 2.1.0
**Build System**: Gradle (Kotlin DSL)
**Package Manager**: Gradle
**Android SDK**:

- **Compile SDK**: 36
- **Target SDK**: 36
- **Min SDK**: 24

## Dependencies

**Main Dependencies**:

- **UI Components**: androidx.appcompat:1.7.0, material:1.12.0, constraintlayout:2.2.1
- **Navigation**: androidx.navigation:2.8.9
- **Image Loading**: Glide 4.16.0
- **Architecture**: Lifecycle (ViewModel, LiveData) 2.8.7
- **Dependency Injection**: Dagger Hilt 2.54
- **Networking**: Retrofit 2.11.0, OkHttp 5.0.0-alpha.3
- **Database**: Room 2.7.2
- **Animation**: Lottie 6.3.0
- **Data Storage**: DataStore 1.1.4
- **Video Player**: AndroidYoutubePlayer 13.0.0

**Development Dependencies**:

- JUnit 4.13.2
- Espresso 3.6.1

## Build & Installation

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

## Testing

**Framework**: JUnit, Espresso
**Test Location**:

- Unit tests: app/src/test/java/
- Instrumentation tests: app/src/androidTest/java/

**Run Command**:

```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest
```
