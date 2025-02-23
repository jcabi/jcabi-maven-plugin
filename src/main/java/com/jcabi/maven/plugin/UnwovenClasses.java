/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.maven.plugin;

import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Operations on the unwoven classes, like storing them in a separate
 * location from the woven ones. Unwoven classes are classes which weren't
 * yet weaved by the aspect weaver.
 *
 * @since 0.15
 */
final class UnwovenClasses {

    /**
     * Directory where unwoven classes are saved.
     */
    private final transient File unwoven;

    /**
     * Output directory from where the classes are taken.
     */
    private final transient File classes;

    /**
     * Maven execution phase.
     */
    private final transient String phase;

    /**
     * Constructor.
     * @param uwvn Dir where unwoven classes go
     * @param cls Directory where the classes are found
     * @param phs Maven execution phase
     */
    UnwovenClasses(final File uwvn, final File cls,
        final String phs) {
        this.unwoven = uwvn;
        this.classes = cls;
        this.phase = phs;
    }

    /**
     * Perform the copy. Unwoven classes go in <b>unwoven</b> directory, while
     * unwoven test classes go in <b>unwoven</b> + -test directory.
     * @throws MojoFailureException If there is an IOException when
     *  copying the files
     */
    void copy() throws MojoFailureException {
        if ("process-classes".equals(this.phase)) {
            this.unwoven.mkdirs();
            Logger.info(
                this, "Unwoven classes will be copied to %s",
                this.unwoven
            );
            UnwovenClasses.copyContents(this.classes, this.unwoven);
        } else if ("process-test-classes".equals(this.phase)) {
            final String suffix = "-test";
            final File tests = new File(
                this.unwoven.getPath().concat(suffix)
            );
            tests.mkdirs();
            Logger.info(
                this, "Unwoven test classes will be copied to %s",
                tests
            );
            UnwovenClasses.copyContents(this.classes, tests);
        }
    }

    /**
     * Copies contents from one dir to another.
     * @param from From directory
     * @param dest Destination directory
     * @throws MojoFailureException If something goes wrong while
     *  cleaning the destination director or copying files to it
     */
    private static void copyContents(
        final File from, final File dest) throws MojoFailureException {
        try {
            FileUtils.cleanDirectory(dest);
            FileUtils.copyDirectory(from, dest, false);
        } catch (final IOException ex) {
            throw new MojoFailureException(
                String.format(
                    "Error when cleaning dest dir or when copying files: %s",
                    ex.getMessage()
                ),
                ex
            );
        }
    }

}
