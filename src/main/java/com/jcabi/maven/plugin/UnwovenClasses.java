/**
 * Copyright (c) 2012-2015, jcabi.com
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

import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Operations on the unwoven classes, like storing them in a separate
 * location from the woven ones. Unwoven classes are classes which weren't
 * yet weaved by the aspect weaver.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.15
 */
public final class UnwovenClasses {

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
    public UnwovenClasses(final File uwvn, final File cls,
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
    public void copy() throws MojoFailureException {
        if ("process-classes".equals(this.phase)) {
            this.unwoven.mkdirs();
            Logger.info(
                this, "Unwoven classes will be copied to %s",
                this.unwoven
            );
            this.copyContents(this.classes, this.unwoven);
        } else if ("process-test-classes".equals(this.phase)) {
            final String suffix = "-test";
            final File unwovenTests = new File(
                this.unwoven.getPath().concat(suffix)
            );
            unwovenTests.mkdirs();
            Logger.info(
                this, "Unwoven test classes will be copied to %s",
                unwovenTests
            );
            this.copyContents(this.classes, unwovenTests);
        }
    }

    /**
     * Copies contents from one dir to another.
     * @param from From directory
     * @param dest Destination directory
     * @throws MojoFailureException If something goes wrong while
     *  cleaning the destination director or copying files to it
     */
    private void copyContents(
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
