/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MIT
 */

package com.progmasoft.archer.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.progmasoft.archer.memory.BufferAllocator;
import com.progmasoft.archer.memory.DirectAllocator;
import org.junit.jupiter.api.Test;

class WideVectorsTest {
  @Test
  void longValuesAndNulls() {
    try (BufferAllocator allocator = new DirectAllocator(1 << 20);
        LongVector vector = new LongVector(allocator, 4)) {
      assertEquals(Long.BYTES, vector.width());
      vector.set(0, Long.MIN_VALUE);
      vector.set(3, 0x0102030405060708L);
      assertEquals(Long.MIN_VALUE, vector.get(0));
      assertEquals(0x0102030405060708L, vector.get(3));
      assertEquals(4, vector.valueCount());
      assertEquals(2, vector.nullCount());
      assertTrue(vector.isNull(1));
      assertThrows(IllegalStateException.class, () -> vector.get(2));
      vector.setNull(3);
      assertThrows(IllegalStateException.class, () -> vector.get(3));
      assertThrows(IndexOutOfBoundsException.class, () -> vector.set(4, 1));
    }
  }

  @Test
  void doubleValuesKeepTheirBits() {
    try (BufferAllocator allocator = new DirectAllocator(1 << 20);
        DoubleVector vector = new DoubleVector(allocator, 4)) {
      double payloadNaN = Double.longBitsToDouble(0x7ff8_0000_0000_0123L);
      vector.set(0, -0.0);
      vector.set(1, payloadNaN);
      vector.set(2, Math.PI);
      vector.setNull(3);
      assertEquals(Double.doubleToRawLongBits(-0.0), Double.doubleToRawLongBits(vector.get(0)));
      assertEquals(0x7ff8_0000_0000_0123L, Double.doubleToRawLongBits(vector.get(1)));
      assertEquals(Math.PI, vector.get(2));
      assertEquals(1, vector.nullCount());
    }
  }

  @Test
  void aNullHasZeroBytes() {
    try (BufferAllocator allocator = new DirectAllocator(1 << 20);
        LongVector vector = new LongVector(allocator, 2)) {
      vector.set(1, -1L);
      vector.setNull(1);
      assertEquals(0, vector.values.getLong(vector.offset(1)));
    }
  }

  @Test
  void theBuffersAreGivenBack() {
    try (BufferAllocator allocator = new DirectAllocator(1 << 20)) {
      DoubleVector vector = new DoubleVector(allocator, 100);
      assertEquals(100 * 8 + 13, allocator.allocatedBytes());
      vector.close();
      assertEquals(0, allocator.allocatedBytes());
    }
  }
}
