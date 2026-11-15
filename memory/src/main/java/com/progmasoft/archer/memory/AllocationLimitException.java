/*
 * SPDX-FileCopyrightText: 2026 Progmasoft <support@progmasoft.com>
 * SPDX-License-Identifier: MIT
 */

package com.progmasoft.archer.memory;

/** A buffer that would take its allocator past its limit. */
public final class AllocationLimitException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /** Says how much was asked for, how much is held, and the limit. */
  public AllocationLimitException(long requested, long allocated, long limit) {
    super(
        "cannot allocate "
            + requested
            + " bytes: "
            + allocated
            + " of the limit of "
            + limit
            + " bytes are allocated");
  }
}
