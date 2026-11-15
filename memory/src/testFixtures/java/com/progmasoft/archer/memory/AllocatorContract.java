/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MIT
 */

package com.progmasoft.archer.memory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * What every {@link BufferAllocator} and its buffers do; the tests of an allocator extend this
 * class and say how to make one.
 */
public abstract class AllocatorContract {
  /** A new allocator with the given limit. */
  protected abstract BufferAllocator allocator(long limit);

  @Test
  void aNewBufferIsZeroAndHasItsSize() {
    try (BufferAllocator allocator = allocator(1024);
        MemoryBuffer buffer = allocator.allocate(64)) {
      assertEquals(64, buffer.capacity());
      for (long i = 0; i < 64; i++) {
        assertEquals(0, buffer.getByte(i));
      }
    }
  }

  @Test
  void valuesAreLittleEndian() {
    try (BufferAllocator allocator = allocator(1024);
        MemoryBuffer buffer = allocator.allocate(16)) {
      buffer.setInt(0, 0x01020304);
      assertEquals(0x04, buffer.getByte(0));
      assertEquals(0x01, buffer.getByte(3));
      assertEquals(0x01020304, buffer.getInt(0));
      buffer.setLong(8, 0x0102030405060708L);
      assertEquals(0x08, buffer.getByte(8));
      assertEquals(0x0102030405060708L, buffer.getLong(8));
      buffer.setByte(1, (byte) -1);
      assertEquals((byte) -1, buffer.getByte(1));
      buffer.setZero(0, 16);
      assertEquals(0, buffer.getInt(0));
      assertEquals(0, buffer.getLong(8));
    }
  }

  @Test
  void bytesAreCopiedInAndOut() {
    try (BufferAllocator allocator = allocator(1024);
        MemoryBuffer buffer = allocator.allocate(8)) {
      buffer.setBytes(2, new byte[] {9, 1, 2, 3, 9}, 1, 3);
      assertEquals(1, buffer.getByte(2));
      assertEquals(3, buffer.getByte(4));
      assertEquals(0, buffer.getByte(5));
      byte[] out = new byte[5];
      buffer.getBytes(1, out, 1, 4);
      assertArrayEquals(new byte[] {0, 0, 1, 2, 3}, out);
      buffer.setBytes(8, new byte[0], 0, 0);
      assertThrows(IndexOutOfBoundsException.class, () -> buffer.setBytes(6, new byte[3], 0, 3));
      assertThrows(IndexOutOfBoundsException.class, () -> buffer.getBytes(0, out, 3, 3));
      assertThrows(IndexOutOfBoundsException.class, () -> buffer.getBytes(0, out, -1, 1));
    }
  }

  @Test
  void accessesOutsideTheBufferAreRefused() {
    try (BufferAllocator allocator = allocator(1024);
        MemoryBuffer buffer = allocator.allocate(8)) {
      assertThrows(IndexOutOfBoundsException.class, () -> buffer.getByte(8));
      assertThrows(IndexOutOfBoundsException.class, () -> buffer.getByte(-1));
      assertThrows(IndexOutOfBoundsException.class, () -> buffer.getInt(5));
      assertThrows(IndexOutOfBoundsException.class, () -> buffer.setLong(1, 0));
      assertThrows(IndexOutOfBoundsException.class, () -> buffer.setZero(4, 5));
    }
  }

  @Test
  void aClosedBufferIsGivenBackAndRefusesAccess() {
    try (BufferAllocator allocator = allocator(1024)) {
      MemoryBuffer buffer = allocator.allocate(100);
      assertEquals(100, allocator.allocatedBytes());
      buffer.close();
      buffer.close();
      assertTrue(buffer.isClosed());
      assertEquals(0, allocator.allocatedBytes());
      assertThrows(IllegalStateException.class, () -> buffer.getByte(0));
    }
  }

  @Test
  void theLimitHolds() {
    try (BufferAllocator allocator = allocator(100);
        MemoryBuffer first = allocator.allocate(60)) {
      assertEquals(100, allocator.limit());
      AllocationLimitException error =
          assertThrows(AllocationLimitException.class, () -> allocator.allocate(41));
      assertTrue(error.getMessage().contains("41"), error.getMessage());
      assertEquals(60, allocator.allocatedBytes());
      try (MemoryBuffer second = allocator.allocate(40)) {
        assertEquals(100, allocator.allocatedBytes());
        assertEquals(40, second.capacity());
      }
      assertEquals(60, first.capacity());
    }
  }

  @Test
  void sizesOutsideTheRangeAreRefused() {
    try (BufferAllocator allocator = allocator(Long.MAX_VALUE)) {
      assertThrows(IllegalArgumentException.class, () -> allocator.allocate(-1));
      assertThrows(
          IllegalArgumentException.class,
          () -> allocator.allocate(AccountingAllocator.MAX_BUFFER_SIZE + 1));
      try (MemoryBuffer empty = allocator.allocate(0)) {
        assertEquals(0, empty.capacity());
      }
    }
  }

  @Test
  void closingWithAnOpenBufferIsALeak() {
    BufferAllocator allocator = allocator(1024);
    MemoryBuffer buffer = allocator.allocate(10);
    IllegalStateException error = assertThrows(IllegalStateException.class, allocator::close);
    assertTrue(error.getMessage().contains("still open"), error.getMessage());
    assertThrows(IllegalStateException.class, () -> allocator.allocate(1));
    buffer.close();
    allocator.close();
  }
}
