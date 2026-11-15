/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MIT
 */

package com.progmasoft.archer.memory;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DirectAllocatorTest extends AllocatorContract {
  @Override
  protected BufferAllocator allocator(long limit) {
    return new DirectAllocator(limit);
  }

  @Test
  void aNegativeLimitIsRefused() {
    assertThrows(IllegalArgumentException.class, () -> new DirectAllocator(-1));
  }
}
