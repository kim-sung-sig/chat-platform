# Build & Compile — Gradle Commands Reference

## Module-Specific Commands

```bash
# auth-server
./gradlew :apps:auth-server:compileJava compileTestJava --no-daemon
./gradlew :apps:auth-server:test
./gradlew :apps:auth-server:clean build
./gradlew :apps:auth-server:bootRun

# chat
./gradlew :apps:chat:compileJava compileTestJava --no-daemon
./gradlew :apps:chat:test

# push-service
./gradlew :apps:push-service:compileJava compileTestJava --no-daemon
./gradlew :apps:push-service:test

# common/core
./gradlew :common:core:clean build
./gradlew :common:core:test
```

## Dependency Inspection

```bash
# Full dependency tree
./gradlew :apps:auth-server:dependencies

# Specific dependency insight
./gradlew :apps:auth-server:dependencyInsight \
  --dependency spring-security \
  --configuration compileClasspath

# Check for updates
./gradlew dependencyUpdates
```

## Test Filtering

```bash
# Run specific test class
./gradlew :apps:auth-server:test --tests "com.example.chat.auth.domain.UserTest"

# Run tests matching pattern
./gradlew :apps:auth-server:test --tests "*CommandServiceTest"

# Run integration tests only
./gradlew :apps:auth-server:test --tests "*IntegrationTest"

# Run with full output
./gradlew :apps:auth-server:test --info

# Run and generate report
./gradlew :apps:auth-server:test jacocoTestReport
```

## CI/CD github actions

```yaml
# .github/workflows/build.yml
name: Build & Test

on:
  push:
    branches: [main, develop]
  pull_request:

jobs:
  build:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: testdb
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
        ports:
          - 5432:5432

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Grant execute permission
        run: chmod +x gradlew

      - name: Compile
        run: ./gradlew compileJava compileTestJava --no-daemon

      - name: Test
        run: ./gradlew test --no-daemon

      - name: Build
        run: ./gradlew clean build -x test --no-daemon

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: '**/build/reports/tests/'
```

## Troubleshooting

```bash
# Gradle version
./gradlew --version

# Scan build for issues
./gradlew build --scan

# Run with stack trace on failure
./gradlew test --stacktrace

# Refresh dependencies (clear cache)
./gradlew dependencies --refresh-dependencies

# Stop all daemons
./gradlew --stop
```