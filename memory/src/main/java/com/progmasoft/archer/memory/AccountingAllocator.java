/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MIT
 */

package com.progmasoft.archer.memory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The accounting of a {@link BufferAllocator}: its limit, the bytes its open buffers hold, and the
 * check that none is left open when it closes. A subclass makes the buffers of one kind of memory.
 */
public abstract class AccountingAllocator implements BufferAllocator {
  /**
   * The largest buffer: one buffer is indexed by a Java array or a Netty buffer, both int-sized.
   */
  public static final long MAX_BUFFER_SIZE = Integer.MAX_VALUE;

  private final long limit;
  private final AtomicLong allocated = new AtomicLong();
  private final Set<MemoryBuffer> open = ConcurrentHashMap.newKeySet();
  private volatile boolean closed;

  /** An allocator whose open buffers may hold {@code limit} bytes together. */
  protected AccountingAllocator(long limit) {
    if (limit < 0) {
      throw new IllegalArgumentException("an allocator cannot have a limit of " + limit + " bytes");
    }
    this.limit = limit;
  }

  @Override
  public final MemoryBuffer allocate(long size) {
    if (size < 0 || size > MAX_BUFFER_SIZE) {
      throw new IllegalArgumentException(
          "a buffer has 0 to " + MAX_BUFFER_SIZE + " bytes, not " + size);
    }
    if (closed) {
      throw new IllegalStateException("the allocator is closed");
    }
    reserve(size);
    MemoryBuffer buffer;
    try {
      buffer = newBuffer((int) size, released -> release(released, size));
    } catch (RuntimeException | Error e) {
      allocated.addAndGet(-size);
      throw e;
    }
    open.add(buffer);
    return buffer;
  }

  private void reserve(long size) {
    while (true) {
      long current = allocated.get();
      if (size > limit - current) {
        throw new AllocationLimitException(size, current, limit);
      }
      if (allocated.compareAndSet(current, current + size)) {
        return;
      }
    }
  }

  private void release(MemoryBuffer buffer, long size) {
    if (open.remove(buffer)) {
      allocated.addAndGet(-size);
    }
  }

  /**
   * A new buffer of {@code size} zero bytes, which calls {@code onClose} with itself once it is
   * closed and its memory given back.
   */
  protected abstract MemoryBuffer newBuffer(int size, ReleaseListener onClose);

  /** Told when a buffer has been closed. */
  @FunctionalInterface
  protected interface ReleaseListener {
    /** {@code buffer} has given its memory back. */
    void released(MemoryBuffer buffer);
  }

  @Override
  public final long allocatedBytes() {
    return allocated.get();
  }

  @Override
  public final long limit() {
    return limit;
  }

  @Override
  public void close() {
    closed = true;
    int count = open.size();
    if (count > 0) {
      throw new IllegalStateException(
          count + " buffer(s) of " + allocated.get() + " byte(s) are still open");
    }
  }
}
