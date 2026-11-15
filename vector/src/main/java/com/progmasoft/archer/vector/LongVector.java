/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MIT
 */

package com.progmasoft.archer.vector;

import com.progmasoft.archer.memory.BufferAllocator;

/** A column of 64-bit integers that may be null, eight little-endian bytes each. */
public final class LongVector extends FixedWidthVector {
  /** A vector of {@code capacity} values on buffers of {@code allocator}. */
  public LongVector(BufferAllocator allocator, int capacity) {
    super(allocator, capacity, Long.BYTES);
  }

  /** Sets the value at {@code index}. */
  public void set(int index, long value) {
    values.setLong(writeOffset(index), value);
    markSet(index);
  }

  /**
   * The value at {@code index}.
   *
   * @throws IllegalStateException when it is null
   */
  public long get(int index) {
    return values.getLong(readOffset(index));
  }
}
