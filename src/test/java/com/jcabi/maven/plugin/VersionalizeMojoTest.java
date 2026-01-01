/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.maven.plugin;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link VersionalizeMojo}.
 *
 * @since 0.1
 */
public final class VersionalizeMojoTest {

    @Test
    @Disabled
    public void skipsExecutionWhenRequired() {
        new Mojo<>(VersionalizeMojo.class)
            .execute();
    }

}
