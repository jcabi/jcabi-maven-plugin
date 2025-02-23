/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.maven.plugin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

/**
 * Test case for {@link AjcMojo}.
 *
 * @since 0.1
 * @checkstyle ExecutableStatementCountCheck (200 lines)
 */
public final class AjcMojoTest {

    @Test
    @Disabled
    public void testClassFilesWeaving(@TempDir final Path temp) throws Exception {
        final MavenProject project = Mockito.mock(MavenProject.class);
        Mockito.doReturn(Collections.emptyList())
            .when(project).getCompileClasspathElements();
        final Path temps = temp.resolve("temps");
        final Path classes = temp.resolve("classes");
        final Path javas = temp.resolve("javas");
        final MavenSession session = Mockito.mock(MavenSession.class);
        final ArtifactRepository repo = Mockito.mock(ArtifactRepository.class);
        Mockito.doReturn(temp.resolve("xx").toFile().toString())
            .when(repo).getBasedir();
        Mockito.doReturn(repo).when(session).getLocalRepository();
        final Path java = javas.resolve("sample/Foo.java");
        java.getParent().toFile().mkdirs();
        Files.write(
            java,
            StringUtils.join(
                "package sample;\n",
                "import com.jcabi.aspects.Immutable;\n",
                "@Immutable class Foo {}"
            ).getBytes(StandardCharsets.UTF_8)
        );
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager mgr = compiler.getStandardFileManager(
            null, Locale.ENGLISH, StandardCharsets.UTF_8
        );
        compiler.getTask(
            null, mgr, null, null, null,
            mgr.getJavaFileObjectsFromFiles(
                Collections.singleton(java.toFile())
            )
        ).call();
        mgr.close();
        final String name = "sample/Foo.class";
        final Path binary = classes.resolve(name);
        FileUtils.copyFile(javas.resolve(name).toFile(), binary.toFile());
        final long size = binary.toFile().length();
        new Mojo<>(AjcMojo.class)
            .with("project", project)
            .with("session", session)
            .with("classesDirectory", classes.toFile())
            .with("aspectDirectories", new File[0])
            .with("tempDirectory", temps.toFile())
            .execute();
        MatcherAssert.assertThat(
            binary.toFile().length(),
            Matchers.not(Matchers.equalTo(size))
        );
    }

}
