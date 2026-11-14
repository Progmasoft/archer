/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MPL-2.0 WITH AdditionRef-Progmasoft-Exception-2.0
 */

package com.progmasoft.archer.vector;

import com.progmasoft.archer.memory.MemoryBuffer;

/** Bit i of a bitmap is bit (i % 8), least significant first, of byte (i / 8). */
final class Bitmaps {
  private Bitmaps() {}

  static long bytesFor(int bits) {
    return (bits + 7L) / 8;
  }

  static boolean get(MemoryBuffer bitmap, int index) {
    return (bitmap.getByte(index >>> 3) & (1 << (index & 7))) != 0;
  }

  static void set(MemoryBuffer bitmap, int index, boolean value) {
    long at = index >>> 3;
    int mask = 1 << (index & 7);
    int current = bitmap.getByte(at);
    bitmap.setByte(at, (byte) (value ? current | mask : current & ~mask));
  }

  static int countSet(MemoryBuffer bitmap, int bits) {
    int count = 0;
    for (int i = 0; i < bits; i++) {
      if (get(bitmap, i)) {
        count++;
      }
    }
    return count;
  }
}
