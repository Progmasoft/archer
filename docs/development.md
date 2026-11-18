<!-- SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com> -->
<!-- SPDX-License-Identifier: MIT -->

# Development

Archer needs JDK 21 (Temurin); the Gradle wrapper fetches the rest.

```sh
./gradlew check jacocoTestReport   # tests and coverage
./gradlew javadoc
```

CI runs the checks with coverage and builds the Javadoc.
