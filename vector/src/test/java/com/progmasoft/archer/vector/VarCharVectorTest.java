/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MIT
 */

package com.progmasoft.archer.vector;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.progmasoft.archer.memory.AllocationLimitException;
import com.progmasoft.archer.memory.BufferAllocator;
import com.progmasoft.archer.memory.DirectAllocator;
import org.junit.jupiter.api.Test;

class VarCharVectorTest {
  @Test
  void valuesAndNulls() {
    try (BufferAllocator allocator = new DirectAllocator(1 << 20);
        VarCharVector vector = new VarCharVector(allocator, 5, 64)) {
      vector.set(0, "Archer");
      vector.setNull(1);
      vector.set(2, "");
      vector.set(3, "çığır");
      assertEquals(4, vector.valueCount());
      assertEquals("Archer", vector.get(0));
      assertTrue(vector.isNull(1));
      assertThrows(IllegalStateException.class, () -> vector.get(1));
      assertFalse(vector.isNull(2));
      assertEquals("", vector.get(2));
      assertEquals("çığır", vector.get(3));
      assertEquals(6 + 0 + 9, vector.dataBytes());
      assertEquals(1, vector.nullCount());
      assertTrue(vector.isNull(4), "a value not written yet is null");
    }
  }

  @Test
  void skippedIndexesAreNullsWithoutBytes() {
    try (BufferAllocator allocator = new DirectAllocator(1 << 20);
        VarCharVector vector = new VarCharVector(allocator, 6, 16)) {
      vector.set(1, "a");
      vector.set(4, "bc");
      assertEquals(5, vector.valueCount());
      assertEquals(3, vector.nullCount());
      assertEquals("a", vector.get(1));
      assertEquals("bc", vector.get(4));
      assertEquals(3, vector.dataBytes());
    }
  }

  @Test
  void valuesAreWrittenInOrder() {
    try (BufferAllocator allocator = new DirectAllocator(1 << 20);
        VarCharVector vector = new VarCharVector(allocator, 3, 16)) {
      vector.set(1, "x");
      assertThrows(IllegalStateException.class, () -> vector.set(0, "y"));
      assertThrows(IllegalStateException.class, () -> vector.setNull(1));
      assertThrows(IndexOutOfBoundsException.class, () -> vector.set(3, "z"));
      assertEquals("x", vector.get(1));
    }
  }

  @Test
  void theDataBufferGrowsAndKeepsItsBytes() {
    try (BufferAllocator allocator = new DirectAllocator(1 << 20);
        VarCharVector vector = new VarCharVector(allocator, 1000, 0)) {
      StringBuilder expected = new StringBuilder();
      for (int i = 0; i < 1000; i++) {
        vector.set(i, "value " + i);
        expected.append("value ").append(i);
      }
      assertTrue(vector.dataCapacity() >= vector.dataBytes());
      assertEquals(expected.length(), vector.dataBytes());
      for (int i = 0; i < 1000; i++) {
        assertEquals("value " + i, vector.get(i));
      }
    }
  }

  @Test
  void bytesAreCopied() {
    try (BufferAllocator allocator = new DirectAllocator(1 << 20);
        VarCharVector vector = new VarCharVector(allocator, 1, 4)) {
      byte[] bytes = {'a', 'b'};
      vector.set(0, bytes);
      bytes[0] = 'z';
      assertArrayEquals(new byte[] {'a', 'b'}, vector.getBytes(0));
    }
  }

  @Test
  void theBuffersAreGivenBackAfterGrowing() {
    try (BufferAllocator allocator = new DirectAllocator(1 << 20)) {
      VarCharVector vector = new VarCharVector(allocator, 8, 2);
      assertEquals(9 * 4 + 1 + 2, allocator.allocatedBytes());
      vector.set(0, "longer than two bytes");
      vector.close();
      assertEquals(0, allocator.allocatedBytes());
    }
  }

  @Test
  void aFailedGrowthKeepsTheVector() {
    try (BufferAllocator allocator = new DirectAllocator(9 * 4 + 1 + 4 + 6)) {
      try (VarCharVector vector = new VarCharVector(allocator, 8, 4)) {
        vector.set(0, "abcd");
        assertThrows(AllocationLimitException.class, () -> vector.set(1, "e"));
        assertEquals(1, vector.valueCount());
        assertEquals("abcd", vector.get(0));
      }
      assertEquals(0, allocator.allocatedBytes());
    }
  }

  @Test
  void aVectorThatDoesNotFitLeavesNothingAllocated() {
    try (BufferAllocator allocator = new DirectAllocator(40)) {
      assertThrows(AllocationLimitException.class, () -> new VarCharVector(allocator, 8, 16));
      assertEquals(0, allocator.allocatedBytes());
    }
    assertThrows(
        IllegalArgumentException.class,
        () -> new VarCharVector(new DirectAllocator(1), Integer.MAX_VALUE, 0));
    assertThrows(
        IllegalArgumentException.class, () -> new VarCharVector(new DirectAllocator(1), 1, -1));
  }
}
