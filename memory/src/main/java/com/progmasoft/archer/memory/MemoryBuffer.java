/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MPL-2.0 WITH AdditionRef-Progmasoft-Exception-2.0
 */

package com.progmasoft.archer.memory;

/**
 * A fixed-size block of memory outside the Java heap, read and written in little-endian byte order,
 * which is the order of Archer's columnar format on every platform.
 *
 * <p>Every access is checked: an index outside the buffer throws {@link IndexOutOfBoundsException},
 * an access after {@link #close()} throws {@link IllegalStateException}. A buffer is not safe for
 * concurrent writes; readers that start after the last write see it.
 */
public interface MemoryBuffer extends AutoCloseable {
  /** The size of the buffer in bytes. */
  long capacity();

  byte getByte(long index);

  void setByte(long index, byte value);

  int getInt(long index);

  void setInt(long index, int value);

  long getLong(long index);

  void setLong(long index, long value);

  /** Sets {@code length} bytes from {@code index} to zero. */
  void setZero(long index, long length);

  /** Whether the buffer has been closed and its memory given back. */
  boolean isClosed();

  /**
   * Gives the memory back to the allocator that made the buffer. Closing a closed buffer does
   * nothing.
   */
  @Override
  void close();
}
