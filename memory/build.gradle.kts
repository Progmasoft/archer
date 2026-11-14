/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MPL-2.0 WITH AdditionRef-Progmasoft-Exception-2.0
 */

plugins { `java-test-fixtures` }

dependencies {
    "testFixturesApi"(platform("org.junit:junit-bom:6.1.3"))
    "testFixturesApi"("org.junit.jupiter:junit-jupiter-api")
}
