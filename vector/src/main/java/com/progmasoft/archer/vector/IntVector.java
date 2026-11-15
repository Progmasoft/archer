/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MPL-2.0 WITH AdditionRef-Progmasoft-Exception-2.0
 */

package com.progmasoft.archer.vector;

import com.progmasoft.archer.memory.BufferAllocator;

/** A column of 32-bit integers that may be null, four little-endian bytes each. */
public final class IntVector extends FixedWidthVector {
  /** A vector of {@code capacity} values on buffers of {@code allocator}. */
  public IntVector(BufferAllocator allocator, int capacity) {
    super(allocator, capacity, Integer.BYTES);
  }

  /** Sets the value at {@code index}. */
  public void set(int index, int value) {
    values.setInt(writeOffset(index), value);
    markSet(index);
  }

  /**
   * The value at {@code index}.
   *
   * @throws IllegalStateException when it is null
   */
  public int get(int index) {
    return values.getInt(readOffset(index));
  }
}
