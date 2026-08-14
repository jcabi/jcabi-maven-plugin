/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.maven.plugin;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * Files of the dependencies of a project, found in the dependency graph
 * of the project and located in the local repository.
 * @since 0.16
 */
final class Dependencies {

    /**
     * Plexus container.
     */
    private final transient PlexusContainer container;

    /**
     * Maven project.
     */
    private final transient MavenProject project;

    /**
     * Maven session.
     */
    private final transient MavenSession session;

    /**
     * Ctor.
     * @param cnt Plexus container
     * @param prj Maven project
     * @param ssn Maven session
     */
    Dependencies(final PlexusContainer cnt, final MavenProject prj,
        final MavenSession ssn) {
        this.container = cnt;
        this.project = prj;
        this.session = ssn;
    }

    /**
     * Files of all dependencies within the given scopes.
     * @param scopes Scopes to take into account
     * @return Collection of file names
     */
    Collection<String> files(final Collection<String> scopes) {
        try {
            final ProjectBuildingRequest request =
                new DefaultProjectBuildingRequest();
            request.setProject(this.project);
            request.setRepositorySession(this.session.getRepositorySession());
            return this.files(
                DependencyGraphBuilder.class.cast(
                    this.container.lookup(
                        DependencyGraphBuilder.class.getCanonicalName(),
                        "default"
                    )
                ).buildDependencyGraph(
                    request,
                    artifact -> scopes.contains(artifact.getScope())
                ),
                scopes
            );
        } catch (final DependencyGraphBuilderException
            | ComponentLookupException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Files of the given node and of all its children.
     * @param node Node to traverse
     * @param scopes Scopes to take into account
     * @return Collection of file names
     */
    private Collection<String> files(final DependencyNode node,
        final Collection<String> scopes) {
        final Artifact artifact = node.getArtifact();
        final Collection<String> files = new ArrayList<>(0);
        if (artifact.getScope() == null
            || scopes.contains(artifact.getScope())) {
            if (artifact.getScope() == null) {
                files.add(artifact.getFile().toString());
            } else {
                files.add(
                    this.session.getLocalRepository().find(artifact).getFile()
                        .toString()
                );
            }
            for (final DependencyNode child : node.getChildren()) {
                if (child.getArtifact().compareTo(node.getArtifact()) != 0) {
                    files.addAll(this.files(child, scopes));
                }
            }
        }
        return files;
    }
}
