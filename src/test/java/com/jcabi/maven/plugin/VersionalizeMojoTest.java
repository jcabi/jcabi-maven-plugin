/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.maven.plugin;

import java.nio.file.Path;
import org.apache.maven.project.MavenProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

/**
 * Test case for {@link VersionalizeMojo}.
 * @since 0.1
 */
final class VersionalizeMojoTest {

    @Test
    void skipsExecutionWhenRequired(@TempDir final Path temp) {
        final MavenProject project = Mockito.mock(
            MavenProject.class, Mockito.RETURNS_DEEP_STUBS
        );
        Mockito.when(project.getBuild().getSourceDirectory())
            .thenReturn(temp.resolve("sources").toString());
        Mockito.when(project.getBuild().getOutputDirectory())
            .thenReturn(temp.resolve("classes").toString());
        new Mojo<>(VersionalizeMojo.class)
            .with("project", project)
            .execute();
        MatcherAssert.assertThat(
            "output directory cannot appear when sources are absent",
            temp.resolve("classes").toFile().exists(),
            Matchers.is(false)
        );
    }
}
