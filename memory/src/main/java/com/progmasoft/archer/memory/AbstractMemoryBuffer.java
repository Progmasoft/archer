/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MPL-2.0 WITH AdditionRef-Progmasoft-Exception-2.0
 */

package com.progmasoft.archer.memory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The checks every {@link MemoryBuffer} makes, around the reads and writes of one kind of memory. A
 * subclass implements the unchecked accesses and {@link #release()}.
 */
public abstract class AbstractMemoryBuffer implements MemoryBuffer {
  private final long capacity;
  private final Runnable onClose;
  private final AtomicBoolean closed = new AtomicBoolean();

  /**
   * A buffer of {@code capacity} bytes; {@code onClose} runs once, after {@link #release()}, when
   * the buffer is closed.
   */
  protected AbstractMemoryBuffer(long capacity, Runnable onClose) {
    if (capacity < 0) {
      throw new IllegalArgumentException("a buffer cannot have " + capacity + " bytes");
    }
    this.capacity = capacity;
    this.onClose = Objects.requireNonNull(onClose, "onClose");
  }

  @Override
  public final long capacity() {
    return capacity;
  }

  @Override
  public final byte getByte(long index) {
    check(index, Byte.BYTES);
    return getByte0(index);
  }

  @Override
  public final void setByte(long index, byte value) {
    check(index, Byte.BYTES);
    setByte0(index, value);
  }

  @Override
  public final int getInt(long index) {
    check(index, Integer.BYTES);
    return getInt0(index);
  }

  @Override
  public final void setInt(long index, int value) {
    check(index, Integer.BYTES);
    setInt0(index, value);
  }

  @Override
  public final long getLong(long index) {
    check(index, Long.BYTES);
    return getLong0(index);
  }

  @Override
  public final void setLong(long index, long value) {
    check(index, Long.BYTES);
    setLong0(index, value);
  }

  @Override
  public final void setZero(long index, long length) {
    check(index, length);
    setZero0(index, length);
  }

  @Override
  public final boolean isClosed() {
    return closed.get();
  }

  @Override
  public final void close() {
    if (closed.compareAndSet(false, true)) {
      try {
        release();
      } finally {
        onClose.run();
      }
    }
  }

  private void check(long index, long length) {
    if (closed.get()) {
      throw new IllegalStateException("the buffer is closed");
    }
    Objects.checkFromIndexSize(index, length, capacity);
  }

  /** Reads a byte at an index inside the buffer. */
  protected abstract byte getByte0(long index);

  /** Writes a byte at an index inside the buffer. */
  protected abstract void setByte0(long index, byte value);

  /** Reads a little-endian int whose bytes are inside the buffer. */
  protected abstract int getInt0(long index);

  /** Writes a little-endian int whose bytes are inside the buffer. */
  protected abstract void setInt0(long index, int value);

  /** Reads a little-endian long whose bytes are inside the buffer. */
  protected abstract long getLong0(long index);

  /** Writes a little-endian long whose bytes are inside the buffer. */
  protected abstract void setLong0(long index, long value);

  /** Zeroes bytes that are inside the buffer. */
  protected abstract void setZero0(long index, long length);

  /** Gives the memory back; called once. */
  protected abstract void release();
}
