/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MIT
 */

package com.progmasoft.archer.memory.netty;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.progmasoft.archer.memory.AllocatorContract;
import com.progmasoft.archer.memory.BufferAllocator;
import com.progmasoft.archer.memory.MemoryBuffer;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.jupiter.api.Test;

class NettyAllocatorTest extends AllocatorContract {
  @Override
  protected BufferAllocator allocator(long limit) {
    return new NettyAllocator(limit);
  }

  @Test
  void pooledMemoryComesBackZeroed() {
    PooledByteBufAllocator pool = new PooledByteBufAllocator(true);
    try (BufferAllocator allocator = new NettyAllocator(1024, pool)) {
      try (MemoryBuffer first = allocator.allocate(64)) {
        first.setLong(0, -1L);
      }
      try (MemoryBuffer second = allocator.allocate(64)) {
        assertEquals(0, second.getLong(0));
      }
    }
  }
}
