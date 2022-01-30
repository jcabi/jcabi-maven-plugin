/*
 * Copyright (c) 2012-2022 Yegor Bugayenko
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
