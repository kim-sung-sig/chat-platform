---
name: Build & Compilation Rules (Gradle, Multi-module)
description: |
  Consistent Gradle build setup. Compilation must succeed before submitting work.
  Use ./gradlew for tasks, never direct gradle command.
scope: All modules (apps/*, common/*)
applies-to: |
  - Building, testing, packaging code
  - Before submitting PR or completing feature
  - Running PDCA analyze phase
version: 1.0
triggers:
  - Before committing code
  - Running tests locally
---

# Rule: Build & Compilation

## Core Principle

**Use `./gradlew` for all build tasks. Compilation must pass before PR submission.**

## Essential Commands

```bash
# ✅ Full build (compile + test)
./gradlew clean build

# ✅ Compile only (no tests) — analyze phase
./gradlew compileJava compileTestJava --no-daemon

# ✅ Specific module
./gradlew :apps:auth-server:clean build

# ✅ Run tests
./gradlew test

# ✅ Run specific test class
./gradlew :apps:auth-server:test --tests MessageServiceTest

# ❌ Don't use
gradle build  # Uses system Gradle
```

## Pre-Commit Checklist

```bash
./gradlew compileJava compileTestJava --no-daemon || exit 1
./gradlew test --no-daemon || exit 1
```

## PDCA Analyze Phase

```bash
# ✅ MUST PASS
./gradlew compileJava compileTestJava --no-daemon

# If fails → show errors, stop, request fix
# If passes → continue to /pdca analyze
```

## Common Issues

| Issue | Fix |
|-------|-----|
| Out of Memory | `org.gradle.jvmargs=-Xmx4096m` in gradle.properties |
| Stale cache | `./gradlew clean` |
| Dependency conflicts | `./gradlew dependencyInsight --dependency org.springframework` |

## Module Dependencies

```
apps/auth-server → common:core, common:logging, common:web
apps/chat → common:* modules

❌ apps/ → apps/ (no cross-app dependencies)
❌ common/ → apps/ (no downward dependencies)
```

## Checklist

- [ ] Compilation succeeds
- [ ] Tests pass
- [ ] No warnings
- [ ] All modules build
- [ ] JAR builds

---

**Examples**: `build-and-compile/docs/`
