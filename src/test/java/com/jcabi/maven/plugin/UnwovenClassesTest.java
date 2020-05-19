/**
 * Copyright (c) 2012-2020, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.maven.plugin;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for {@link UnwovenClasses).
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
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

    /**
     * Clean also before just in case <b>AfterClass</b> fails.
     * @throws Exception If something goes wrong
     */
    @BeforeClass
    public static void cleanBefore() throws Exception {
        deleteResourceDirs();
    }

    /**
     * UnwovenClasses can copy compiled classes to a destination directory.
     * @throws Exception If something goes wrong
     */
    @Test
    public void copiesUnwovenClasses() throws Exception {
        new UnwovenClasses(
            new File(UNWOVEN), new File(CLASSES), "process-classes"
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

    /**
     * UnwovenClasses can copy test classes to a destination directory.
     * @throws Exception If something goes wrong
     */
    @Test
    public void copiesUnwovenTestClasses() throws Exception {
        new UnwovenClasses(
            new File(UNWOVEN), new File(CLASSES), "process-test-classes"
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

    /**
     * Clean resources after tests run.
     * @throws Exception If something goes wrong
     */
    @AfterClass
    public static void cleanAfter() throws Exception {
        deleteResourceDirs();
    }

    /**
     * Delete test resource directories (for cleanup).
     * @throws Exception If something goes wrong
     */
    private static void deleteResourceDirs() throws Exception {
        FileUtils.deleteDirectory(new File("src/test/resources/unwoven-test"));
        FileUtils.deleteDirectory(new File(UNWOVEN));
    }
}
