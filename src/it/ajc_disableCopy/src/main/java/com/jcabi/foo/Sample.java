/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.foo;

import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
final class Sample {
    public String notNull(@NotNull final String value) {
        return value;
    }
}
