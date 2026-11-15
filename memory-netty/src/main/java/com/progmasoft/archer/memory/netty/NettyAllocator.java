/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MIT
 */

package com.progmasoft.archer.memory.netty;

import com.progmasoft.archer.memory.AbstractMemoryBuffer;
import com.progmasoft.archer.memory.AccountingAllocator;
import com.progmasoft.archer.memory.MemoryBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import java.util.Objects;

/**
 * Buffers on Netty's direct buffers, pooled by default: closing a buffer gives its memory back to
 * Netty's pool at once.
 */
public final class NettyAllocator extends AccountingAllocator {
  private final ByteBufAllocator netty;

  /** An allocator on Netty's default pooled allocator. */
  public NettyAllocator(long limit) {
    this(limit, PooledByteBufAllocator.DEFAULT);
  }

  /** An allocator on the given Netty allocator. */
  public NettyAllocator(long limit, ByteBufAllocator netty) {
    super(limit);
    this.netty = Objects.requireNonNull(netty, "netty");
  }

  @Override
  protected MemoryBuffer newBuffer(int size, ReleaseListener onClose) {
    ByteBuf memory = netty.directBuffer(size, size);
    try {
      // Pooled memory comes back as it was left.
      memory.setZero(0, size);
      return new NettyBuffer(memory, size, onClose);
    } catch (RuntimeException | Error e) {
      memory.release();
      throw e;
    }
  }

  private static final class NettyBuffer extends AbstractMemoryBuffer {
    private final ByteBuf memory;

    NettyBuffer(ByteBuf memory, int size, ReleaseListener onClose) {
      this(memory, size, new Runnable[1], onClose);
    }

    private NettyBuffer(ByteBuf memory, int size, Runnable[] self, ReleaseListener onClose) {
      super(size, () -> self[0].run());
      this.memory = memory;
      self[0] = () -> onClose.released(this);
    }

    @Override
    protected byte getByte0(long index) {
      return memory.getByte((int) index);
    }

    @Override
    protected void setByte0(long index, byte value) {
      memory.setByte((int) index, value);
    }

    @Override
    protected int getInt0(long index) {
      return memory.getIntLE((int) index);
    }

    @Override
    protected void setInt0(long index, int value) {
      memory.setIntLE((int) index, value);
    }

    @Override
    protected long getLong0(long index) {
      return memory.getLongLE((int) index);
    }

    @Override
    protected void setLong0(long index, long value) {
      memory.setLongLE((int) index, value);
    }

    @Override
    protected void getBytes0(long index, byte[] destination, int offset, int length) {
      memory.getBytes((int) index, destination, offset, length);
    }

    @Override
    protected void setBytes0(long index, byte[] source, int offset, int length) {
      memory.setBytes((int) index, source, offset, length);
    }

    @Override
    protected void setZero0(long index, long length) {
      memory.setZero((int) index, (int) length);
    }

    @Override
    protected void release() {
      memory.release();
    }
  }
}
