/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MPL-2.0 WITH AdditionRef-Progmasoft-Exception-2.0
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
