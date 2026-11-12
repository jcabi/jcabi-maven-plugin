/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * Marker file that remembers the moment of the last successful weave,
 * so that classes already woven are never handed to AspectJ again.
 *
 * @since 0.18
 */
final class Mark {

    /**
     * The marker file.
     */
    private final transient File file;

    /**
     * Directories whose files are compared with the marker.
     */
    private final transient File[] dirs;

    /**
     * Ctor.
     *
     * @param marker The marker file
     * @param watched Directories whose files are compared with the marker
     */
    Mark(final File marker, final File... watched) {
        this.file = marker;
        this.dirs = watched.clone();
    }

    /**
     * Is there a file newer than the marker, or no marker at all?
     *
     * @return TRUE if weaving is required
     */
    boolean stale() {
        return !this.file.exists() || Arrays.stream(this.dirs)
            .filter(File::isDirectory)
            .map(Mark::files)
            .flatMap(Collection::stream)
            .anyMatch(f -> FileUtils.isFileNewer(f, this.file));
    }

    /**
     * Create the marker or update its modification time.
     *
     * @throws IOException If fails
     */
    void touch() throws IOException {
        FileUtils.touch(this.file);
    }

    /**
     * Delete the marker, if it exists.
     *
     * @throws IOException If fails
     */
    void erase() throws IOException {
        Files.deleteIfExists(this.file.toPath());
    }

    private static Collection<File> files(final File dir) {
        return FileUtils.listFiles(
            dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE
        );
    }
}
