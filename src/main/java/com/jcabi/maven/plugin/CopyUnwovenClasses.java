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
 * Unwoven classes should be copied to a specified directory.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @todo #48:30min This class extracts the copying of unwoven classes
 *  from AjcMojo. Unit tests should be written for it.
 */
public final class CopyUnwovenClasses {
    /**
     * Directory where unwoven classes are saved.
     */
    private final transient File unwovenclassesdir;

    /**
     * Output directory from where the classes are taken.
     */
    private final transient File classesdir;

    /**
     * Maven execution phase.
     */
    private final transient String phase;
    /**
     * Constructor.
     * @param unwoven Dir where unwoven classes go
     * @param classes Directory where the classes are found
     * @param execphase Maven execution phase
     */
    public CopyUnwovenClasses(
        final File unwoven,
        final File classes,
        final String execphase
    ) {
        this.unwovenclassesdir = unwoven;
        this.classesdir = classes;
        this.phase = execphase;
    }

    /**
     * Perform the copy.
     * @throws MojoFailureException If there is an IOException when
     *  copying the files
     */
    public void copy() throws MojoFailureException {
        if ("process-classes".equals(this.phase)) {
            this.unwovenclassesdir.mkdirs();
            Logger.info(
                this, "Unwoven classes will be copied to %s",
                this.unwovenclassesdir
            );
            this.copyContents(this.classesdir, this.unwovenclassesdir);
        } else if ("process-test-classes".equals(this.phase)) {
            final String suffix = "-test";
            final File unwovenTests = new File(
                this.unwovenclassesdir.getPath().concat(suffix)
            );
            unwovenTests.mkdirs();
            Logger.info(
                this, "Unwoven test classes will be copied to %s",
                unwovenTests
            );
            this.copyContents(this.classesdir, unwovenTests);
        }
    }

    /**
     * Copies contents from one dir to another.
     * @param fromdir From directory
     * @param todir To directory
     * @throws MojoFailureException If something goes wrong while copying files
     */
    private void copyContents(
        final File fromdir,
        final File todir
    ) throws MojoFailureException {
        try {
            FileUtils.cleanDirectory(this.unwovenclassesdir);
            FileUtils.copyDirectory(fromdir, todir, false);
        } catch (final IOException ex) {
            throw new MojoFailureException(
                String.format(
                    "Exception when copying unwoven classes to %s: %s",
                    this.unwovenclassesdir, ex.getMessage()
                ),
                ex
            );
        }
    }

}
