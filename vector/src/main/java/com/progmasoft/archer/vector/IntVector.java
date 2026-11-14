/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MPL-2.0 WITH AdditionRef-Progmasoft-Exception-2.0
 */

package com.progmasoft.archer.vector;

import com.progmasoft.archer.memory.BufferAllocator;
import com.progmasoft.archer.memory.MemoryBuffer;
import java.util.Objects;

/**
 * A column of 32-bit integers that may be null: the values in one buffer, four little-endian bytes
 * each, and a validity bitmap in another, one bit per value, least significant bit first, set for a
 * value that is not null. This is the layout of a fixed-width column in Archer's format.
 *
 * <p>A new vector holds {@code capacity} nulls and its value count is 0; setting a value or a null
 * at an index makes the count reach past that index.
 */
public final class IntVector implements AutoCloseable {
  private final MemoryBuffer values;
  private final MemoryBuffer validity;
  private final int capacity;
  private int valueCount;

  /** A vector of {@code capacity} values on buffers of {@code allocator}. */
  public IntVector(BufferAllocator allocator, int capacity) {
    Objects.requireNonNull(allocator, "allocator");
    if (capacity < 0) {
      throw new IllegalArgumentException("a vector cannot hold " + capacity + " values");
    }
    this.capacity = capacity;
    this.values = allocator.allocate((long) capacity * Integer.BYTES);
    try {
      this.validity = allocator.allocate(Bitmaps.bytesFor(capacity));
    } catch (RuntimeException | Error e) {
      values.close();
      throw e;
    }
  }

  /** How many values the vector can hold. */
  public int capacity() {
    return capacity;
  }

  /** One more than the highest index that has been set, or 0. */
  public int valueCount() {
    return valueCount;
  }

  /** Sets the value at {@code index}. */
  public void set(int index, int value) {
    Objects.checkIndex(index, capacity);
    values.setInt((long) index * Integer.BYTES, value);
    Bitmaps.set(validity, index, true);
    valueCount = Math.max(valueCount, index + 1);
  }

  /** Makes the value at {@code index} null. */
  public void setNull(int index) {
    Objects.checkIndex(index, capacity);
    values.setInt((long) index * Integer.BYTES, 0);
    Bitmaps.set(validity, index, false);
    valueCount = Math.max(valueCount, index + 1);
  }

  /** Whether the value at {@code index} is null. */
  public boolean isNull(int index) {
    Objects.checkIndex(index, capacity);
    return !Bitmaps.get(validity, index);
  }

  /**
   * The value at {@code index}.
   *
   * @throws IllegalStateException when it is null
   */
  public int get(int index) {
    if (isNull(index)) {
      throw new IllegalStateException("the value at " + index + " is null");
    }
    return values.getInt((long) index * Integer.BYTES);
  }

  /** How many of the first {@link #valueCount()} values are null. */
  public int nullCount() {
    return valueCount - Bitmaps.countSet(validity, valueCount);
  }

  /** Gives both buffers back. */
  @Override
  public void close() {
    try {
      values.close();
    } finally {
      validity.close();
    }
  }
}
