/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.maven.plugin;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UnwovenClasses}.
 *
 * @since 0.15
 */
public final class UnwovenClassesTest {

    /**
     * Classes directory.
     */
    private static final String CLASSES = "src/test/resources/classes";

    /**
     * Unwoven classes dir.
     */
    private static final String UNWOVEN = "src/test/resources/unwoven";

    @BeforeEach
    public void cleanBefore() throws Exception {
        UnwovenClassesTest.deleteResourceDirs();
    }

    @AfterEach
    public void cleanAfter() throws Exception {
        UnwovenClassesTest.deleteResourceDirs();
    }

    @Test
    void copiesUnwovenClasses() throws Exception {
        new UnwovenClasses(
            new File(UnwovenClassesTest.UNWOVEN),
            new File(UnwovenClassesTest.CLASSES),
            "process-classes"
        ).copy();
        MatcherAssert.assertThat(
            new File("src/test/resources/unwoven/MyPojo.txt").exists(),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new File("src/test/resources/unwoven/MySecondPojo.txt").exists(),
            Matchers.is(true)
        );
    }

    @Test
    void copiesUnwovenTestClasses() throws Exception {
        new UnwovenClasses(
            new File(UnwovenClassesTest.UNWOVEN),
            new File(UnwovenClassesTest.CLASSES),
            "process-test-classes"
        ).copy();
        MatcherAssert.assertThat(
            new File("src/test/resources/unwoven-test/MyPojo.txt").exists(),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new File(
                "src/test/resources/unwoven-test/MySecondPojo.txt"
            ).exists(),
            Matchers.is(true)
        );
    }

    private static void deleteResourceDirs() throws Exception {
        FileUtils.deleteDirectory(new File("src/test/resources/unwoven-test"));
        FileUtils.deleteDirectory(new File(UnwovenClassesTest.UNWOVEN));
    }
}
