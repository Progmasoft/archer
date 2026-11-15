/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MIT
 */

package com.progmasoft.archer.vector;

import com.progmasoft.archer.memory.BufferAllocator;

/**
 * A column of IEEE 754 binary64 numbers that may be null, eight little-endian bytes each. The bits
 * of a value are kept as they are, so every NaN keeps its payload.
 */
public final class DoubleVector extends FixedWidthVector {
  /** A vector of {@code capacity} values on buffers of {@code allocator}. */
  public DoubleVector(BufferAllocator allocator, int capacity) {
    super(allocator, capacity, Double.BYTES);
  }

  /** Sets the value at {@code index}. */
  public void set(int index, double value) {
    values.setLong(writeOffset(index), Double.doubleToRawLongBits(value));
    markSet(index);
  }

  /**
   * The value at {@code index}.
   *
   * @throws IllegalStateException when it is null
   */
  public double get(int index) {
    return Double.longBitsToDouble(values.getLong(readOffset(index)));
  }
}
