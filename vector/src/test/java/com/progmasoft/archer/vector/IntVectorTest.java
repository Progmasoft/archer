/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MPL-2.0 WITH AdditionRef-Progmasoft-Exception-2.0
 */

package com.progmasoft.archer.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.progmasoft.archer.memory.AllocationLimitException;
import com.progmasoft.archer.memory.BufferAllocator;
import com.progmasoft.archer.memory.DirectAllocator;
import com.progmasoft.archer.memory.MemoryBuffer;
import org.junit.jupiter.api.Test;

class IntVectorTest {
  @Test
  void valuesAndNulls() {
    try (BufferAllocator allocator = new DirectAllocator(1 << 20);
        IntVector vector = new IntVector(allocator, 10)) {
      assertEquals(10, vector.capacity());
      assertEquals(0, vector.valueCount());
      assertTrue(vector.isNull(3), "a new vector holds nulls");
      vector.set(0, 42);
      vector.set(2, -7);
      vector.setNull(1);
      assertEquals(3, vector.valueCount());
      assertEquals(42, vector.get(0));
      assertEquals(-7, vector.get(2));
      assertTrue(vector.isNull(1));
      assertEquals(1, vector.nullCount());
      assertThrows(IllegalStateException.class, () -> vector.get(1));
      vector.set(1, 5);
      assertFalse(vector.isNull(1));
      vector.setNull(0);
      assertTrue(vector.isNull(0));
      assertEquals(1, vector.nullCount());
    }
  }

  @Test
  void theBitmapIsLeastSignificantBitFirst() {
    try (BufferAllocator allocator = new DirectAllocator(1 << 20);
        MemoryBuffer bitmap = allocator.allocate(Bitmaps.bytesFor(16))) {
      Bitmaps.set(bitmap, 0, true);
      Bitmaps.set(bitmap, 9, true);
      Bitmaps.set(bitmap, 15, true);
      assertEquals((byte) 0b0000_0001, bitmap.getByte(0));
      assertEquals((byte) 0b1000_0010, bitmap.getByte(1));
      assertEquals(3, Bitmaps.countSet(bitmap, 16));
      Bitmaps.set(bitmap, 9, false);
      assertEquals((byte) 0b1000_0000, bitmap.getByte(1));
    }
  }

  @Test
  void indexesOutsideTheVectorAreRefused() {
    try (BufferAllocator allocator = new DirectAllocator(1 << 20);
        IntVector vector = new IntVector(allocator, 4)) {
      assertThrows(IndexOutOfBoundsException.class, () -> vector.set(4, 0));
      assertThrows(IndexOutOfBoundsException.class, () -> vector.setNull(-1));
      assertThrows(IndexOutOfBoundsException.class, () -> vector.isNull(4));
    }
    assertThrows(IllegalArgumentException.class, () -> new IntVector(new DirectAllocator(1), -1));
  }

  @Test
  void theBuffersAreGivenBack() {
    try (BufferAllocator allocator = new DirectAllocator(1 << 20)) {
      IntVector vector = new IntVector(allocator, 100);
      assertEquals(100 * 4 + 13, allocator.allocatedBytes());
      vector.close();
      assertEquals(0, allocator.allocatedBytes());
    }
  }

  @Test
  void aVectorThatDoesNotFitLeavesNothingAllocated() {
    try (BufferAllocator allocator = new DirectAllocator(400)) {
      assertThrows(AllocationLimitException.class, () -> new IntVector(allocator, 100));
      assertEquals(0, allocator.allocatedBytes());
    }
  }
}
