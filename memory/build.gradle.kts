/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MIT
 */

plugins { `java-test-fixtures` }

dependencies {
    "testFixturesApi"(platform("org.junit:junit-bom:6.1.3"))
    "testFixturesApi"("org.junit.jupiter:junit-jupiter-api")
}
