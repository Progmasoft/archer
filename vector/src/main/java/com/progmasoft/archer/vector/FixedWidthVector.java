/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MPL-2.0 WITH AdditionRef-Progmasoft-Exception-2.0
 */

package com.progmasoft.archer.vector;

import com.progmasoft.archer.memory.BufferAllocator;
import com.progmasoft.archer.memory.MemoryBuffer;
import java.util.Objects;

/**
 * A column whose values all take the same number of bytes and may be null: the values in one
 * buffer, little-endian, one after another, and a validity bitmap in another, one bit per value,
 * least significant bit first, set for a value that is not null. This is the layout of a
 * fixed-width column in Archer's format.
 *
 * <p>A new vector holds {@code capacity} nulls and its value count is 0; setting a value or a null
 * at an index makes the count reach past that index. The bytes of a null value are zero.
 */
public abstract sealed class FixedWidthVector implements AutoCloseable
    permits IntVector, LongVector, DoubleVector {
  /** The values, {@link #width()} bytes each. */
  final MemoryBuffer values;

  private final MemoryBuffer validity;
  private final int capacity;
  private final int width;
  private int valueCount;

  FixedWidthVector(BufferAllocator allocator, int capacity, int width) {
    Objects.requireNonNull(allocator, "allocator");
    if (capacity < 0) {
      throw new IllegalArgumentException("a vector cannot hold " + capacity + " values");
    }
    this.capacity = capacity;
    this.width = width;
    this.values = allocator.allocate((long) capacity * width);
    try {
      this.validity = allocator.allocate(Bitmaps.bytesFor(capacity));
    } catch (RuntimeException | Error e) {
      values.close();
      throw e;
    }
  }

  /** How many values the vector can hold. */
  public final int capacity() {
    return capacity;
  }

  /** How many bytes one value takes. */
  public final int width() {
    return width;
  }

  /** One more than the highest index that has been set, or 0. */
  public final int valueCount() {
    return valueCount;
  }

  /** Makes the value at {@code index} null. */
  public final void setNull(int index) {
    Objects.checkIndex(index, capacity);
    values.setZero(offset(index), width);
    Bitmaps.set(validity, index, false);
    valueCount = Math.max(valueCount, index + 1);
  }

  /** Whether the value at {@code index} is null. */
  public final boolean isNull(int index) {
    Objects.checkIndex(index, capacity);
    return !Bitmaps.get(validity, index);
  }

  /** How many of the first {@link #valueCount()} values are null. */
  public final int nullCount() {
    return valueCount - Bitmaps.countSet(validity, valueCount);
  }

  /**
   * Where the value at {@code index}, which must be inside the vector, starts in {@link #values}.
   */
  final long offset(int index) {
    return (long) index * width;
  }

  /**
   * Checks {@code index} before a value is written there and returns where it starts in {@link
   * #values}; {@link #markSet(int)} follows the write.
   */
  final long writeOffset(int index) {
    Objects.checkIndex(index, capacity);
    return offset(index);
  }

  /** Records that the value at {@code index} has just been written and is not null. */
  final void markSet(int index) {
    Bitmaps.set(validity, index, true);
    valueCount = Math.max(valueCount, index + 1);
  }

  /**
   * Checks that the value at {@code index} is not null and returns where it starts in {@link
   * #values}.
   *
   * @throws IllegalStateException when it is null
   */
  final long readOffset(int index) {
    if (isNull(index)) {
      throw new IllegalStateException("the value at " + index + " is null");
    }
    return offset(index);
  }

  /** Gives both buffers back. */
  @Override
  public final void close() {
    try {
      values.close();
    } finally {
      validity.close();
    }
  }
}
