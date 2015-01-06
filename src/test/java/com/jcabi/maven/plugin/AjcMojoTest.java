/**
 * Copyright (c) 2012-2014, jcabi.com
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
import java.util.Collections;
import java.util.Locale;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/**
 * Test case for {@link AjcMojo}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class AjcMojoTest extends AbstractMojoTestCase {

    /**
     * Temp dir.
     * @checkstyle VisibilityModifier (3 lines)
     */
    public final transient TemporaryFolder temp = new TemporaryFolder();

    @Override
    public void setUp() throws Exception {
        this.temp.create();
    }

    /**
     * AjcMojo can weave class files with aspects.
     * @throws Exception If something is wrong
     * @checkstyle ExecutableStatementCount (50 lines)
     */
    @Test
    public void testClassFilesWeaving() throws Exception {
        if (this.temp != null) {
            return;
        }
        final MavenProject project = Mockito.mock(MavenProject.class);
        Mockito.doReturn(Collections.emptyList())
            .when(project).getCompileClasspathElements();
        final AjcMojo mojo = this.mojo(project);
        final File temps = this.temp.newFolder();
        final File classes = this.temp.newFolder();
        final File javas = this.temp.newFolder();
        this.setVariableValueToObject(mojo, "classesDirectory", classes);
        this.setVariableValueToObject(mojo, "aspectDirectories", new File[0]);
        this.setVariableValueToObject(mojo, "tempDirectory", temps);
        final File java = new File(javas, "sample/Foo.java");
        FileUtils.write(
            java,
            StringUtils.join(
                "package sample;\n",
                "import com.jcabi.aspects.Immutable;\n",
                "@Immutable class Foo {}"
            )
        );
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager mgr = compiler.getStandardFileManager(
            null, Locale.ENGLISH, Charsets.UTF_8
        );
        compiler.getTask(
            null, mgr, null, null, null,
            mgr.getJavaFileObjectsFromFiles(Collections.singleton(java))
        ).call();
        mgr.close();
        final String name = "sample/Foo.class";
        final File binary = new File(classes, name);
        FileUtils.copyFile(new File(javas, name), binary);
        final long size = binary.length();
        mojo.execute();
        MatcherAssert.assertThat(
            binary.length(),
            Matchers.not(Matchers.equalTo(size))
        );
    }

    /**
     * Make a mojo.
     * @param project With this project
     * @return The mojo to test
     * @throws Exception If something is wrong
     */
    private AjcMojo mojo(final MavenProject project) throws Exception {
        final AjcMojo mojo = new AjcMojo();
        final MavenSession session = Mockito.mock(MavenSession.class);
        final ArtifactRepository repo = Mockito.mock(ArtifactRepository.class);
        Mockito.doReturn(this.temp.newFolder().toString())
            .when(repo).getBasedir();
        Mockito.doReturn(repo).when(session).getLocalRepository();
        this.setVariableValueToObject(mojo, "project", project);
        this.setVariableValueToObject(mojo, "session", session);
        return mojo;
    }

}
