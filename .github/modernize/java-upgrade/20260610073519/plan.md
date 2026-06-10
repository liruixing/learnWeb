# Upgrade Plan: learn (20260610073519)

- **Generated**: 2026-06-10 07:35:19
- **HEAD Branch**: N/A
- **HEAD Commit ID**: N/A

## Available Tools

**JDKs**
- JDK 17: available on host PATH for baseline verification
- JDK 25: **<TO_BE_INSTALLED>** (required by step 1)

**Build Tools**
- Gradle Wrapper: 9.5.1 (supported for Java 25)
- Maven: not applicable for this project

## Guidelines

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

## Options

- Working branch: appmod/java-upgrade-20260610073519
- Run tests before and after the upgrade: true

## Upgrade Goals

- Upgrade Java runtime/toolchain to Java 25 (latest LTS)

## Technology Stack

| Technology/Dependency | Current | Min Compatible | Why Incompatible |
| --------------------- | ------- | -------------- | ---------------- |
| Java runtime | 17 | 25 | User requested latest LTS runtime
| Gradle wrapper | 9.5.1 | 9.1.0 | Compatible with Java 25, no upgrade needed
| Kotlin plugin | 2.2.21 | 2.3.0 | Kotlin 2.2 does not support JVM target 25
| Spring Boot | 4.0.6 | 4.0.6 | Already compatible with Java 25

## Derived Upgrades

- Kotlin plugin version must be upgraded to at least 2.3.0 because Kotlin 2.2.21 does not support `jvmTarget=25`.
- The Gradle Java toolchain should be updated from Java 17 to Java 25.
- Since the Gradle wrapper is already 9.5.1, no wrapper upgrade is required for Java 25 support.

## Impact Analysis

### Dependency Changes

| File | Dependency | Current | Action | Target | Reason |
|------|------------|---------|--------|--------|--------|
| build.gradle.kts | kotlin("jvm") | 2.2.21 | upgrade | 2.3.0 | Kotlin 2.3.0+ required for Java 25 support
| build.gradle.kts | kotlin("plugin.spring") | 2.2.21 | upgrade | 2.3.0 | Kotlin Spring plugin must match Kotlin compiler version

### Source Code Changes

| File | Location | Current | Required Change | Reason |
|------|----------|---------|----------------|--------|
| build.gradle.kts | `java.toolchain.languageVersion` | 17 | Update to 25 | Use Java 25 runtime/toolchain
| build.gradle.kts | `kotlin.compilerOptions` | no explicit jvmTarget | Add `jvmTarget = "25"` | Compile Kotlin bytecode for Java 25

### Configuration Changes

No application or CI/CD configuration changes are required for this runtime-only upgrade.

### CI/CD Changes

No CI/CD file updates are required in this workspace for the selected project.

### Risks & Warnings

- **Kotlin compiler compatibility**: Kotlin 2.3.0+ is required for Java 25, and plugin upgrade may surface new compiler diagnostics. Mitigation: keep the Spring Boot plugin unchanged, update only Kotlin plugin versions and jvmTarget, then recompile and fix any Kotlin-specific warnings or errors.
- **Host JDK availability**: JDK 25 is not currently installed in the detected toolchain. Mitigation: install JDK 25 in Step 1 using the installation tool, then use it for upgrade verification.

## Upgrade Steps

- Step 1: Setup Environment
  - **Rationale**: Ensure the required Java 25 runtime is installed before applying build changes.
  - **Changes to Make**: Install JDK 25 and verify Gradle wrapper 9.5.1 is available.
  - **Verification**: `java -version` shows Java 25; `./gradlew --version` shows Gradle 9.5.1.

- Step 2: Setup Baseline
  - **Rationale**: Record current project health before making upgrade changes.
  - **Changes to Make**: Run baseline compilation and tests on current code with JDK 17.
  - **Verification**: `./gradlew clean testClasses` and `./gradlew test` succeed.

- Step 3: Upgrade Kotlin and Java Toolchain to Java 25
  - **Rationale**: Apply the minimal code and build file changes required to target the new runtime.
  - **Changes to Make**: Update `build.gradle.kts` with Kotlin plugins `2.3.0`, set `java.toolchain.languageVersion` to 25, and add Kotlin `jvmTarget = "25"`.
  - **Verification**: `./gradlew clean testClasses` with JDK 25 succeeds.

- Step 4: CVE Validation & Fix
  - **Rationale**: Verify the upgraded dependency set is free of known direct-dependency vulnerabilities.
  - **Changes to Make**: Scan direct Gradle dependencies for CVEs and patch any affected direct dependencies.
  - **Verification**: `./gradlew clean testClasses` succeeds after fixes and re-scan confirms no remaining direct CVEs.

- Step 5: Final Validation
  - **Rationale**: Ensure the runtime upgrade is stable with the full test suite on Java 25.
  - **Changes to Make**: Run the full Gradle test suite and fix any failures caused by the upgrade.
  - **Verification**: `./gradlew clean test` with JDK 25 succeeds.
