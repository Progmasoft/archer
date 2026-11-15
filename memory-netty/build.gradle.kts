/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MIT
 */

dependencies {
    api(project(":archer-memory"))
    implementation("io.netty:netty-buffer:4.2.20.Final")
    testImplementation(testFixtures(project(":archer-memory")))
}
