<!-- SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com> -->
<!-- SPDX-License-Identifier: MIT -->

# Architecture

| Module | Package | What it holds |
| --- | --- | --- |
| `memory` | `com.progmasoft.archer.memory` | `MemoryBuffer` and `AbstractMemoryBuffer`, off-heap memory; `BufferAllocator` with `DirectAllocator` and `AccountingAllocator`, which keeps a limit and throws `AllocationLimitException` beyond it |
| `memory-netty` | `com.progmasoft.archer.memory.netty` | `NettyAllocator`, the allocator over Netty's buffers |
| `vector` | `com.progmasoft.archer.vector` | Columnar vectors: `IntVector`, `LongVector`, `DoubleVector` over `FixedWidthVector`, `VarCharVector`, and the validity bitmaps of `Bitmaps` |

`vector` builds on `memory`; `memory-netty` is optional, for a program that already uses Netty. The test fixtures of
`memory` are shared with the other modules' tests.
