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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Versionalize Java packages.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.16
 */
@Mojo(
    name = "versionalize",
    defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
    threadSafe = true
)
@ToString
@EqualsAndHashCode(callSuper = false)
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public final class VersionalizeMojo extends AbstractMojo {

    /**
     * Maven project.
     */
    @Component
    private transient MavenProject project;

    /**
     * Build number.
     * @checkstyle MemberNameCheck (10 lines)
     */
    @Parameter(
        property = "buildNumber",
        required = false,
        readonly = false
    )
    private transient String buildNumber;

    @Override
    @Loggable(value = Loggable.DEBUG, limit = 1, unit = TimeUnit.MINUTES)
    public void execute() throws MojoFailureException {
        StaticLoggerBinder.getSingleton().setMavenLog(this.getLog());
        final File src = new File(this.project.getBuild().getSourceDirectory());
        if (!src.exists()) {
            Logger.info(this, "source directory '%s' is absent", src);
            return;
        }
        final File dest =
            new File(this.project.getBuild().getOutputDirectory());
        if (dest.mkdirs()) {
            Logger.info(this, "created directory %s", dest);
        }
        Logger.info(this, "Versionalizing %s directory", dest);
        try {
            this.versionalize(src, dest);
        } catch (final IOException ex) {
            throw new MojoFailureException("failed to versionalize", ex);
        }
    }

    /**
     * Create and return a text of the version file.
     * @param dir The destination directory
     * @return The text
     */
    private String text(@NotNull final File dir) {
        final StringBuilder text = new StringBuilder(0)
            .append(String.format("Build Number: %s%n", this.buildNumber))
            .append(
                String.format(
                    "Project Version: %s%n",
                    this.project.getVersion()
                )
            )
            .append(
                String.format(
                    "Build Date: %s%n%n",
                    DateFormatUtils.ISO_DATETIME_FORMAT.format(new Date())
                )
            );
        for (final String name : VersionalizeMojo.files(dir, "*")) {
            final File file = new File(dir, name);
            if (file.isFile()) {
                text.append(name)
                    .append(": ")
                    .append(file.length())
                    .append('\n');
            }
        }
        return text.toString();
    }

    /**
     * Versionalize packages from source to dest.
     * @param src Source directory
     * @param dest Destination
     * @throws IOException If some IO problem
     */
    private void versionalize(@NotNull final File src, @NotNull final File dest)
        throws IOException {
        final Collection<File> dirs = FileUtils.listFilesAndDirs(
            src,
            new NotFileFilter(TrueFileFilter.INSTANCE),
            DirectoryFileFilter.DIRECTORY
        );
        final String name = String.format(
            "%s-%s-%s.txt",
            VersionalizeMojo.cleanup(this.project.getGroupId()),
            VersionalizeMojo.cleanup(this.project.getArtifactId()),
            VersionalizeMojo.cleanup(this.project.getPackaging())
        );
        for (final File dir : dirs) {
            if (VersionalizeMojo.files(dir, "*.java").isEmpty()) {
                continue;
            }
            final File ddir = new File(
                dest,
                StringUtils.substring(
                    dir.getCanonicalPath(),
                    src.getCanonicalPath().length() + 1
                )
            );
            final File version = new File(ddir, name);
            if (version.getParentFile().mkdirs()) {
                Logger.info(this, "created dir %s", version.getParentFile());
            }
            FileUtils.write(version, this.text(ddir));
            Logger.info(this, "File %s added", version);
        }
    }

    /**
     * Clean the text.
     * @param text The text
     * @return Clean version of it
     */
    private static String cleanup(final String text) {
        return text.replaceAll("[^_a-z0-9\\-]", "-");
    }

    /**
     * All Java files in the directory.
     * @param dir The directory
     * @param mask Mask to use
     * @return List of Java file names
     */
    private static Collection<String> files(final File dir, final String mask) {
        final FileFilter filter = new WildcardFileFilter(mask);
        final File[] files = dir.listFiles(filter);
        final Collection<String> names = new ArrayList<String>(files.length);
        for (final File file : files) {
            names.add(file.getName());
        }
        return names;
    }

}
