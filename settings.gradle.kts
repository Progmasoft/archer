/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MPL-2.0 WITH AdditionRef-Progmasoft-Exception-2.0
 */

rootProject.name = "archer"

// Off-heap memory: buffers, allocators and their accounting.
include("archer-memory")
project(":archer-memory").projectDir = file("memory")

// The allocator of archer-memory on Netty's pooled direct buffers.
include("archer-memory-netty")
project(":archer-memory-netty").projectDir = file("memory-netty")

// Columnar vectors over archer-memory buffers.
include("archer-vector")
project(":archer-vector").projectDir = file("vector")
