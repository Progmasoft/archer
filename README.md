<!--
SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
SPDX-License-Identifier: MPL-2.0 WITH AdditionRef-Progmasoft-Exception-2.0
-->

# Archer

Archer is a columnar in-memory format and its libraries for the JVM: memory outside the Java heap, and vectors laid
out in it for fast analytical work. It plays the part Apache Arrow plays, for Java 21 and later and the JVM languages
that call Java, such as Kotlin and Scala, rather than for many languages at once. The name comes from French: an *archer* is the one who shoots the arrows, a nod to Apache Arrow.

Archer is under development and has no release yet.

## Modules

| Artifact | What it holds |
|---|---|
| `com.progmasoft.archer:archer-memory` | `MemoryBuffer`, little-endian memory with checked accesses, and `BufferAllocator`s that count what their buffers hold, enforce a limit and refuse to close while a buffer is open. `DirectAllocator` uses direct `ByteBuffer`s. |
| `com.progmasoft.archer:archer-memory-netty` | `NettyAllocator`, the same allocator on Netty's pooled direct buffers. |
| `com.progmasoft.archer:archer-vector` | Columnar vectors on those buffers, starting with `IntVector`: values and a validity bitmap, least significant bit first. |

```kotlin
repositories {
    maven("https://maven.progmasoft.com/bigdata")
}

dependencies {
    implementation("com.progmasoft.archer:archer-vector:$archerVersion")
    implementation("com.progmasoft.archer:archer-memory-netty:$archerVersion")
}
```

```java
try (BufferAllocator allocator = new NettyAllocator(64L << 20);
     IntVector vector = new IntVector(allocator, 1024)) {
    vector.set(0, 42);
    vector.setNull(1);
}
```

## Building

Archer builds with any JDK from 21 on and is compiled against the Java 21 API.

```bash
./gradlew check
```

## License

[MPL-2.0 WITH AdditionRef-Progmasoft-Exception-2.0](LICENSE.txt).
