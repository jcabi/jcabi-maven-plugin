/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.maven.plugin;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link Mark}.
 *
 * @since 0.18
 */
final class MarkTest {

    @Test
    void reportsStaleWithoutMarker(@TempDir final Path temp) throws Exception {
        final Path dir = Files.createDirectories(temp.resolve("cls-ä"));
        Files.write(
            dir.resolve("Foo$1.class"), "x7q".getBytes(StandardCharsets.UTF_8)
        );
        MatcherAssert.assertThat(
            "mark cannot be fresh when the marker file is absent",
            new Mark(temp.resolve("tmp/done").toFile(), dir.toFile()).stale(),
            Matchers.is(true)
        );
    }

    @Test
    void reportsFreshAfterTouch(@TempDir final Path temp) throws Exception {
        Files.setLastModifiedTime(
            Files.write(
                Files.createDirectories(temp.resolve("woven/a/b"))
                    .resolve("Bar.class"), "k2".getBytes(StandardCharsets.UTF_8)
            ),
            FileTime.from(Instant.now().minusSeconds(73L))
        );
        final Mark mark = new Mark(
            temp.resolve("jcabi-ajc/m.txt").toFile(), temp.resolve("woven").toFile()
        );
        mark.touch();
        MatcherAssert.assertThat(
            "mark cannot be stale when no file is newer than the marker",
            mark.stale(),
            Matchers.is(false)
        );
    }

    @Test
    void reportsStaleWhenFileIsNewer(@TempDir final Path temp) throws Exception {
        final Path dir = Files.createDirectories(temp.resolve("out"));
        final Mark mark = new Mark(temp.resolve("w.mark").toFile(), dir.toFile());
        mark.touch();
        Files.setLastModifiedTime(
            Files.write(
                dir.resolve("Qux.class"), "z9".getBytes(StandardCharsets.UTF_8)
            ),
            FileTime.from(Instant.now().plusSeconds(41L))
        );
        MatcherAssert.assertThat(
            "mark cannot be fresh when a file is newer than the marker",
            mark.stale(),
            Matchers.is(true)
        );
    }

    @Test
    void reportsStaleWhenAnyDirectoryHasNewerFile(@TempDir final Path temp)
        throws Exception {
        final Path first = Files.createDirectories(temp.resolve("classes"));
        final Path second = Files.createDirectories(temp.resolve("aspects"));
        final Mark mark = new Mark(
            temp.resolve("done").toFile(), first.toFile(), second.toFile()
        );
        mark.touch();
        Files.setLastModifiedTime(
            Files.write(
                second.resolve("Tracing.aj"), "a".getBytes(StandardCharsets.UTF_8)
            ),
            FileTime.from(Instant.now().plusSeconds(19L))
        );
        MatcherAssert.assertThat(
            "mark cannot be fresh when a second directory has a newer file",
            mark.stale(),
            Matchers.is(true)
        );
    }

    @Test
    void ignoresAbsentDirectory(@TempDir final Path temp) throws Exception {
        final Mark mark = new Mark(
            temp.resolve("mk").toFile(), temp.resolve("nowhere").toFile()
        );
        mark.touch();
        MatcherAssert.assertThat(
            "mark cannot be stale when its directory does not exist",
            mark.stale(),
            Matchers.is(false)
        );
    }

    @Test
    void removesMarkerOnErase(@TempDir final Path temp) throws Exception {
        final Mark mark = new Mark(
            temp.resolve("tmp/done").toFile(), temp.toFile()
        );
        mark.touch();
        mark.erase();
        MatcherAssert.assertThat(
            "mark cannot be fresh after it was erased",
            mark.stale(),
            Matchers.is(true)
        );
    }
}
