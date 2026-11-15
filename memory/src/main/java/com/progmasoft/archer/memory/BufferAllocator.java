/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MIT
 */

package com.progmasoft.archer.memory;

/**
 * Makes {@link MemoryBuffer}s and keeps count of the memory they hold, up to a limit.
 *
 * <p>Closing an allocator while a buffer it made is still open throws {@link
 * IllegalStateException}: memory that is never given back is found where it was lost.
 */
public interface BufferAllocator extends AutoCloseable {
  /**
   * A new buffer of {@code size} bytes, all zero.
   *
   * @throws IllegalArgumentException when {@code size} is negative or larger than one buffer can be
   * @throws AllocationLimitException when the buffer would take the allocator past its limit
   * @throws IllegalStateException when the allocator is closed
   */
  MemoryBuffer allocate(long size);

  /** The bytes held by the open buffers of this allocator. */
  long allocatedBytes();

  /** The most bytes the open buffers of this allocator may hold together. */
  long limit();

  @Override
  void close();
}
