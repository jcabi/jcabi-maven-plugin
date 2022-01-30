package com.jcabi.foo;

import javax.validation.constraints.NotNull;

final class Sample {
    public String notNull(@NotNull final String value) {
        return value;
    }
}
