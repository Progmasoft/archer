<!-- SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com> -->
<!-- SPDX-License-Identifier: MPL-2.0 WITH AdditionRef-Progmasoft-Exception-2.0 -->

# Changelog

Versions follow [Semantic Versioning](https://semver.org).

## Unreleased

- `archer-memory`: `MemoryBuffer` with little-endian, bounds-checked accesses; `BufferAllocator` with a limit, a count
  of allocated bytes and leak detection on close; `DirectAllocator` on direct `ByteBuffer`s.
- `archer-memory-netty`: `NettyAllocator` on Netty's pooled direct buffers, zeroing reused memory.
- `archer-memory`: `MemoryBuffer.getBytes` and `setBytes` copy ranges of byte arrays in and out.
- `archer-vector`: `IntVector`, `LongVector` and `DoubleVector`, fixed-width columns with a validity bitmap, least
  significant bit first, on a common `FixedWidthVector`; `VarCharVector`, UTF-8 strings with 32-bit offsets, written in
  index order, whose data buffer grows.
