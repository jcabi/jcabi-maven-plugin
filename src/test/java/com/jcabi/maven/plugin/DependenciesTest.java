/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.maven.plugin;

import java.util.Collections;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.codehaus.plexus.PlexusContainer;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Dependencies}.
 *
 * @since 0.16
 */
final class DependenciesTest {

    @Test
    void skipsProjectArtifactWithoutFile() throws Exception {
        final Artifact artifact = Mockito.mock(Artifact.class);
        Mockito.when(artifact.getScope()).thenReturn(null);
        Mockito.when(artifact.getFile()).thenReturn(null);
        final DependencyNode node = Mockito.mock(DependencyNode.class);
        Mockito.when(node.getArtifact()).thenReturn(artifact);
        Mockito.when(node.getChildren()).thenReturn(Collections.emptyList());
        final DependencyGraphBuilder builder = Mockito.mock(
            DependencyGraphBuilder.class
        );
        Mockito.when(
            builder.buildDependencyGraph(Mockito.any(), Mockito.any())
        ).thenReturn(node);
        final PlexusContainer container = Mockito.mock(PlexusContainer.class);
        Mockito.when(container.lookup(Mockito.anyString(), Mockito.eq("default")))
            .thenReturn(builder);
        MatcherAssert.assertThat(
            "Project artifact without a file must be skipped, not fail",
            new Dependencies(
                container,
                Mockito.mock(MavenProject.class),
                Mockito.mock(MavenSession.class)
            ).files(Collections.singleton("compile")),
            Matchers.empty()
        );
    }
}
