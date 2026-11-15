/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MPL-2.0 WITH AdditionRef-Progmasoft-Exception-2.0
 */

package com.progmasoft.archer.vector;

import com.progmasoft.archer.memory.AccountingAllocator;
import com.progmasoft.archer.memory.BufferAllocator;
import com.progmasoft.archer.memory.MemoryBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A column of UTF-8 strings that may be null: the bytes of all values one after another in a data
 * buffer, {@code capacity + 1} little-endian 32-bit offsets into it, and a validity bitmap, least
 * significant bit first. Value {@code i} is the bytes from offset {@code i} up to offset {@code i +
 * 1}; a null value has no bytes. This is the layout of a variable-width column in Archer's format.
 *
 * <p>Values are written in index order, as a column is built: setting a value or a null at an index
 * below {@link #valueCount()} is refused, and the indexes skipped by a write become nulls. The data
 * buffer grows when a value does not fit, up to {@link AccountingAllocator#MAX_BUFFER_SIZE} bytes.
 */
public final class VarCharVector implements AutoCloseable {
  private static final int COPY_CHUNK = 8192;

  private final BufferAllocator allocator;
  private final MemoryBuffer offsets;
  private final MemoryBuffer validity;
  private final int capacity;
  private MemoryBuffer data;
  private int valueCount;

  /**
   * A vector of {@code capacity} values on buffers of {@code allocator}, whose data buffer starts
   * with {@code dataCapacity} bytes.
   */
  public VarCharVector(BufferAllocator allocator, int capacity, int dataCapacity) {
    this.allocator = Objects.requireNonNull(allocator, "allocator");
    if (capacity < 0 || capacity == Integer.MAX_VALUE) {
      throw new IllegalArgumentException("a vector cannot hold " + capacity + " values");
    }
    if (dataCapacity < 0) {
      throw new IllegalArgumentException("a data buffer cannot have " + dataCapacity + " bytes");
    }
    this.capacity = capacity;
    MemoryBuffer offsets = allocator.allocate((capacity + 1L) * Integer.BYTES);
    MemoryBuffer validity = null;
    try {
      validity = allocator.allocate(Bitmaps.bytesFor(capacity));
      this.data = allocator.allocate(dataCapacity);
    } catch (RuntimeException | Error e) {
      offsets.close();
      if (validity != null) {
        validity.close();
      }
      throw e;
    }
    this.offsets = offsets;
    this.validity = validity;
  }

  /** How many values the vector can hold. */
  public int capacity() {
    return capacity;
  }

  /** One more than the highest index that has been set, or 0. */
  public int valueCount() {
    return valueCount;
  }

  /** How many bytes the values hold together. */
  public int dataBytes() {
    return end(valueCount);
  }

  /** How many bytes the data buffer has room for before it grows. */
  public long dataCapacity() {
    return data.capacity();
  }

  /** Sets the value at {@code index} to the UTF-8 bytes of {@code value}. */
  public void set(int index, String value) {
    set(index, value.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Sets the value at {@code index} to {@code bytes}, which must be UTF-8; the vector keeps a copy.
   */
  public void set(int index, byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes");
    int start = startWrite(index);
    if (bytes.length > Integer.MAX_VALUE - start) {
      throw new IllegalStateException("the values cannot hold more than 2^31 - 1 bytes");
    }
    int end = start + bytes.length;
    reserve(end);
    data.setBytes(start, bytes, 0, bytes.length);
    offsets.setInt((index + 1L) * Integer.BYTES, end);
    Bitmaps.set(validity, index, true);
    valueCount = index + 1;
  }

  /** Makes the value at {@code index} null. */
  public void setNull(int index) {
    int start = startWrite(index);
    offsets.setInt((index + 1L) * Integer.BYTES, start);
    valueCount = index + 1;
  }

  /** Whether the value at {@code index} is null. */
  public boolean isNull(int index) {
    Objects.checkIndex(index, capacity);
    return !Bitmaps.get(validity, index);
  }

  /**
   * The bytes of the value at {@code index}, a new array.
   *
   * @throws IllegalStateException when it is null
   */
  public byte[] getBytes(int index) {
    if (isNull(index)) {
      throw new IllegalStateException("the value at " + index + " is null");
    }
    int start = end(index);
    byte[] bytes = new byte[end(index + 1) - start];
    data.getBytes(start, bytes, 0, bytes.length);
    return bytes;
  }

  /**
   * The value at {@code index}, decoded from UTF-8; malformed bytes become U+FFFD.
   *
   * @throws IllegalStateException when it is null
   */
  public String get(int index) {
    return new String(getBytes(index), StandardCharsets.UTF_8);
  }

  /** How many of the first {@link #valueCount()} values are null. */
  public int nullCount() {
    return valueCount - Bitmaps.countSet(validity, valueCount);
  }

  /** Gives the buffers back. */
  @Override
  public void close() {
    try {
      offsets.close();
    } finally {
      try {
        validity.close();
      } finally {
        data.close();
      }
    }
  }

  /** Offset {@code index}: where value {@code index} starts and value {@code index - 1} ends. */
  private int end(int index) {
    return offsets.getInt((long) index * Integer.BYTES);
  }

  /**
   * Checks that {@code index} may be written now, makes the indexes skipped before it nulls, and
   * returns where its bytes start.
   */
  private int startWrite(int index) {
    Objects.checkIndex(index, capacity);
    if (index < valueCount) {
      throw new IllegalStateException(
          "values are written in order: " + index + " is below the value count " + valueCount);
    }
    int start = end(valueCount);
    // A skipped index is a null without bytes; its validity bit is already clear.
    for (int skipped = valueCount; skipped < index; skipped++) {
      offsets.setInt((skipped + 1L) * Integer.BYTES, start);
    }
    return start;
  }

  /** Makes the data buffer hold at least {@code bytes} bytes, keeping what it holds. */
  private void reserve(int bytes) {
    long current = data.capacity();
    if (bytes <= current) {
      return;
    }
    long grown = Math.min(Math.max(bytes, current * 2), AccountingAllocator.MAX_BUFFER_SIZE);
    MemoryBuffer larger = allocator.allocate(grown);
    try {
      int used = end(valueCount);
      byte[] chunk = new byte[Math.min(COPY_CHUNK, Math.max(used, 1))];
      for (int at = 0; at < used; at += chunk.length) {
        int length = Math.min(chunk.length, used - at);
        data.getBytes(at, chunk, 0, length);
        larger.setBytes(at, chunk, 0, length);
      }
    } catch (RuntimeException | Error e) {
      larger.close();
      throw e;
    }
    data.close();
    data = larger;
  }
}
