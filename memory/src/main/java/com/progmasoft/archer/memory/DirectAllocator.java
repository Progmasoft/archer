/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MPL-2.0 WITH AdditionRef-Progmasoft-Exception-2.0
 */

package com.progmasoft.archer.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Buffers on direct {@link ByteBuffer}s. The JVM frees their memory when the buffer is collected,
 * after it is closed; the allocator's count drops when it is closed.
 */
public final class DirectAllocator extends AccountingAllocator {
  /** An allocator whose open buffers may hold {@code limit} bytes together. */
  public DirectAllocator(long limit) {
    super(limit);
  }

  @Override
  protected MemoryBuffer newBuffer(int size, ReleaseListener onClose) {
    // A new direct buffer is zero-filled by the JVM.
    ByteBuffer memory = ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN);
    return new DirectBuffer(memory, onClose);
  }

  private static final class DirectBuffer extends AbstractMemoryBuffer {
    private ByteBuffer memory;

    DirectBuffer(ByteBuffer memory, ReleaseListener onClose) {
      this(memory, new Runnable[1], onClose);
    }

    private DirectBuffer(ByteBuffer memory, Runnable[] self, ReleaseListener onClose) {
      super(memory.capacity(), () -> self[0].run());
      this.memory = memory;
      self[0] = () -> onClose.released(this);
    }

    @Override
    protected byte getByte0(long index) {
      return memory.get((int) index);
    }

    @Override
    protected void setByte0(long index, byte value) {
      memory.put((int) index, value);
    }

    @Override
    protected int getInt0(long index) {
      return memory.getInt((int) index);
    }

    @Override
    protected void setInt0(long index, int value) {
      memory.putInt((int) index, value);
    }

    @Override
    protected long getLong0(long index) {
      return memory.getLong((int) index);
    }

    @Override
    protected void setLong0(long index, long value) {
      memory.putLong((int) index, value);
    }

    @Override
    protected void setZero0(long index, long length) {
      for (int i = (int) index, end = (int) (index + length); i < end; i++) {
        memory.put(i, (byte) 0);
      }
    }

    @Override
    protected void release() {
      memory = null;
    }
  }
}
