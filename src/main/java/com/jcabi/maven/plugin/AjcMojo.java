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

import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.tools.ajc.Main;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * AspectJ compile CLASS files.
 *
 * @since 0.7.16
 * @see <a href="http://www.eclipse.org/aspectj/doc/next/devguide/ajc-ref.html">AJC compiler manual</a>
 */
@Mojo(
    name = "ajc",
    defaultPhase = LifecyclePhase.PROCESS_CLASSES,
    threadSafe = true,
    requiresDependencyResolution = ResolutionScope.COMPILE
)
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.GodClass" })
public final class AjcMojo extends AbstractMojo implements Contextualizable {

    /**
     * Classpath separator.
     */
    private static final String SEP = System.getProperty("path.separator");

    /**
     * Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private transient MavenProject project;

    /**
     * Maven execution.
     */
    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private transient MojoExecution execution;

    /**
     * Maven session.
     */
    @Parameter(defaultValue = "${session}", readonly = true)
    private transient MavenSession session;

    /**
     * Rep session.
     */
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private transient RepositorySystemSession rsession;

    /**
     * Compiled directory.
     * @checkstyle MemberNameCheck (7 lines)
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private transient File classesDirectory;

    /**
     * Directory in which uwoven classes are copied.
     * @checkstyle MemberNameCheck (7 lines)
     */
    @Parameter(defaultValue = "${project.build.directory}/unwoven")
    private transient File unwovenClassesDir;

    /**
     * Disables the copy of unwoven files to unwovenClassesDir.
     * @checkstyle MemberNameCheck (7 lines)
     */
    @Parameter(defaultValue = "false")
    private transient boolean disableCopy;

    /**
     * Directories with aspects.
     * @checkstyle MemberNameCheck (6 lines)
     */
    @Parameter
    private transient File[] aspectDirectories;

    /**
     * Temporary directory.
     * @checkstyle MemberNameCheck (7 lines)
     */
    @Parameter(defaultValue = "${project.build.directory}/jcabi-ajc")
    private transient File tempDirectory;

    /**
     * Scopes to take into account.
     */
    @Parameter
    private transient String[] scopes;

    /**
     * Container.
     */
    private transient PlexusContainer container;

    /**
     * Java source version.
     */
    @Parameter(property = "source", defaultValue = "1.8")
    private transient String source;

    /**
     * Java target version.
     */
    @Parameter(property = "target", defaultValue = "1.8")
    private transient String target;

    /**
     * Project classpath.
     * @checkstyle MemberNameCheck (7 lines)
     */
    @Parameter(
        defaultValue = "${project.compileClasspathElements}",
        required = true,
        readonly = true
    )
    private transient List<String> classpathElements;

    /**
     * Ajc compiler message log.
     */
    @Parameter(
        property = "log",
        defaultValue = "${project.build.directory}/jcabi-ajc.log"
    )
    private transient String log;

    @Override
    public void contextualize(final Context context) throws ContextException {
        this.container = (PlexusContainer) context
            .get(PlexusConstants.PLEXUS_KEY);
    }

    @Override
    public void execute() throws MojoFailureException {
        StaticLoggerBinder.getSingleton().setMavenLog(this.getLog());
        final ArtifactHandler handler = this.project.getArtifact()
            .getArtifactHandler();
        if (!"java".equalsIgnoreCase(handler.getLanguage())) {
            Logger.warn(
                this,
                // @checkstyle LineLength (1 line)
                "Not executing AJC as the project is not a Java classpath-capable package"
            );
            return;
        }
        if (this.classesDirectory.mkdirs()) {
            Logger.info(this, "Created classes dir %s", this.classesDirectory);
        }
        if (!this.disableCopy
            && !this.unwovenClassesDir.equals(this.classesDirectory)) {
            this.copyUnwovenClasses();
        }
        if (this.hasClasses() || this.hasSourceroots()) {
            try {
                this.executeAjc();
            } catch (final IOException ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            Logger.warn(
                this,
                // @checkstyle LineLength (1 line)
                "Not executing AJC as there is no .class file or source roots file."
            );
        }
    }

    /**
     * Process classes and source roots files with AJC.
     *
     * @throws MojoFailureException If AJC failed to process files
     * @throws IOException If fails
     */
    private void executeAjc() throws MojoFailureException, IOException {
        if (this.tempDirectory.mkdirs()) {
            Logger.info(this, "Created temp dir %s", this.tempDirectory);
        }
        final Main main = new Main();
        final IMessageHolder mholder = new AjcMojo.MsgHolder();
        main.run(
            new String[] {
                "-Xset:avoidFinal=true",
                "-Xlint:warning",
                "-inpath",
                this.classesDirectory.getAbsolutePath(),
                "-sourceroots",
                this.sourceroots(),
                "-d",
                this.tempDirectory.getAbsolutePath(),
                "-classpath",
                StringUtils.join(this.classpath(), AjcMojo.SEP),
                "-aspectpath",
                this.aspectpath(),
                "-source",
                this.source,
                "-target",
                this.target,
                "-g:none",
                "-encoding",
                "UTF-8",
                "-time",
                "-log",
                this.log,
                "-showWeaveInfo",
                "-warn:constructorName",
                "-warn:packageDefaultMethod",
                "-warn:deprecation",
                "-warn:maskedCatchBlocks",
                "-warn:unusedLocals",
                "-warn:unusedArguments",
                "-warn:unusedImports",
                "-warn:syntheticAccess",
                "-warn:assertIdentifier",
            },
            mholder
        );
        try {
            FileUtils.copyDirectory(this.tempDirectory, this.classesDirectory);
            FileUtils.cleanDirectory(this.tempDirectory);
        } catch (final IOException ex) {
            throw new MojoFailureException(
                "failed to copy files and clean temp",
                ex
            );
        }
        Logger.info(
            this,
            // @checkstyle LineLength (1 line)
            "ajc result: %d file(s) processed, %d pointcut(s) woven, %d error(s), %d warning(s)",
            AjcMojo.files(this.classesDirectory).size(),
            mholder.numMessages(IMessage.WEAVEINFO, false),
            mholder.numMessages(IMessage.ERROR, true),
            mholder.numMessages(IMessage.WARNING, false)
        );
        if (mholder.hasAnyMessage(IMessage.ERROR, true)) {
            throw new MojoFailureException("AJC failed, see log above");
        }
    }

    /**
     * Get classpath for AJC.
     * @return Classpath
     */
    private Collection<String> classpath() {
        final Collection<String> scps;
        if (this.scopes == null) {
            scps = AjcMojo.scope();
        } else {
            scps = Arrays.asList(this.scopes);
        }
        final Collection<String> elements = new LinkedList<>();
        try {
            final DependencyGraphBuilder builder =
                DependencyGraphBuilder.class.cast(
                    this.container.lookup(
                        DependencyGraphBuilder.class.getCanonicalName(),
                        "default"
                    )
                );
            final ProjectBuildingRequest request =
                new DefaultProjectBuildingRequest();
            request.setProject(this.project);
            request.setRepositorySession(this.rsession);
            final DependencyNode node = builder.buildDependencyGraph(
                request,
                artifact -> scps.contains(artifact.getScope())
            );
            elements.addAll(this.dependencies(node, scps));
        } catch (final DependencyGraphBuilderException
            | ComponentLookupException ex) {
            throw new IllegalStateException(ex);
        }
        elements.addAll(this.classpathElements);
        return elements;
    }

    /**
     * Retrieve dependencies for from given node and scope.
     * @param node Node to traverse.
     * @param scps Scopes to use.
     * @return Collection of dependency files.
     */
    private Collection<String> dependencies(final DependencyNode node,
        final Collection<String> scps) {
        final Artifact artifact = node.getArtifact();
        final Collection<String> files = new LinkedList<>();
        if (artifact.getScope() == null
            || scps.contains(artifact.getScope())) {
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
                    files.addAll(this.dependencies(child, scps));
                }
            }
        }
        return files;
    }

    /**
     * Default scopes.
     * @return List of scopes.
     */
    private static Collection<String> scope() {
        final List<String> scps;
        if (AjcMojo.eclipseAether()) {
            scps = Arrays.asList(
                JavaScopes.COMPILE,
                JavaScopes.PROVIDED,
                JavaScopes.RUNTIME,
                JavaScopes.SYSTEM
            );
        } else {
            scps = Arrays.asList(
                JavaScopes.COMPILE,
                JavaScopes.RUNTIME,
                JavaScopes.PROVIDED,
                JavaScopes.SYSTEM
            );
        }
        return scps;
    }

    /**
     * If environment is inside Eclipse Aether.
     * @return True if Eclipse Aether.
     */
    private static boolean eclipseAether() {
        boolean found = false;
        try {
            Thread.currentThread().getContextClassLoader()
                .loadClass("org.sonatype.aether.graph.DependencyFilter");
        } catch (final ClassNotFoundException ex) {
            found = true;
        }
        return found;
    }

    /**
     * Get locations of all aspect libraries for AJC.
     * @return Classpath
     */
    private String aspectpath() {
        return new StringBuilder(0)
            .append(StringUtils.join(this.classpath(), AjcMojo.SEP))
            .append(AjcMojo.SEP)
            .append(System.getProperty("java.class.path"))
            .toString();
    }

    /**
     * Get locations of all source roots (with aspects in source form).
     * @return Directories separated
     * @throws IOException If fails
     */
    private String sourceroots() throws IOException {
        final String path;
        if (this.aspectDirectories == null
            || this.aspectDirectories.length == 0) {
            path = Files.createTempDirectory("temp")
                .toAbsolutePath().toString();
        } else {
            for (final File dir : this.aspectDirectories) {
                if (!dir.exists()) {
                    throw new IllegalStateException(
                        String.format("source directory %s is absent", dir)
                    );
                }
            }
            path = StringUtils.join(this.aspectDirectories, AjcMojo.SEP);
        }
        return path;
    }

    /**
     * Check if the project contains .class files.
     * @return True if .class files found
     */
    private boolean hasClasses() {
        return this.listClasses().size() > 0;
    }

    /**
     * List of all .class files from <b>classesDirectory</b>.
     * @return A Collection of .class files
     */
    private Collection<File> listClasses() {
        final IOFileFilter filter = FileFilterUtils
            .suffixFileFilter(".class");
        return FileUtils.listFiles(
            this.classesDirectory, filter, FileFilterUtils
                .directoryFileFilter()
        );
    }

    /**
     * Check if the project contains source roots files.
     * @return True if {@linkplain #aspectDirectories} contain files
     */
    private boolean hasSourceroots() {
        return this.aspectDirectories != null
            && this.aspectDirectories.length > 0;
    }

    /**
     * Find all files in the directory.
     * @param dir The directory
     * @return List of them
     */
    private static Collection<File> files(final File dir) {
        final Collection<File> files = new LinkedList<>();
        final Collection<File> all = FileUtils.listFiles(
            dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE
        );
        for (final File file : all) {
            if (file.isFile()) {
                files.add(file);
            }
        }
        return files;
    }

    /**
     * Copy the unwoven classes from <b>classesDirectory</b> to
     * <b>unwovenClassesDir</b>.
     * @throws MojoFailureException If something goes wrong
     */
    private void copyUnwovenClasses()
        throws MojoFailureException {
        if (this.hasClasses()) {
            new UnwovenClasses(
                this.unwovenClassesDir,
                this.classesDirectory,
                this.execution.getLifecyclePhase()
            ).copy();
        } else {
            Logger.warn(
                this,
                "No classes found at %s. Nothing will be copied to %s",
                this.classesDirectory,
                this.unwovenClassesDir
            );
        }
    }

    /**
     * Message holder.
     *
     * @since 0.1
     */
    private static final class MsgHolder implements IMessageHolder {
        /**
         * All messages seen so far.
         */
        private final transient Collection<IMessage> messages =
            new CopyOnWriteArrayList<>();

        @Override
        public boolean hasAnyMessage(final IMessage.Kind kind,
            final boolean greater) {
            boolean has = false;
            for (final IMessage msg : this.messages) {
                has = msg.getKind().equals(kind) || greater
                    && IMessage.Kind.COMPARATOR
                    .compare(msg.getKind(), kind) > 0;
                if (has) {
                    break;
                }
            }
            return has;
        }

        @Override
        public int numMessages(final IMessage.Kind kind,
            final boolean greater) {
            int num = 0;
            for (final IMessage msg : this.messages) {
                final boolean has = msg.getKind().equals(kind) || greater
                    && IMessage.Kind.COMPARATOR
                    .compare(msg.getKind(), kind) > 0;
                if (has) {
                    ++num;
                }
            }
            return num;
        }

        @Override
        public IMessage[] getMessages(final IMessage.Kind kind,
            final boolean greater) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<IMessage> getUnmodifiableListView() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clearMessages() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean handleMessage(final IMessage msg) {
            if (msg.getKind().equals(IMessage.ERROR)
                || msg.getKind().equals(IMessage.FAIL)
                || msg.getKind().equals(IMessage.ABORT)) {
                Logger.error(AjcMojo.class, msg.getMessage());
            } else if (msg.getKind().equals(IMessage.WARNING)) {
                Logger.warn(AjcMojo.class, msg.getMessage());
            } else {
                Logger.debug(AjcMojo.class, msg.getMessage());
            }
            this.messages.add(msg);
            return true;
        }

        @Override
        public boolean isIgnoring(final IMessage.Kind kind) {
            return false;
        }

        @Override
        public void dontIgnore(final IMessage.Kind kind) {
            assert kind != null;
        }

        @Override
        public void ignore(final IMessage.Kind kind) {
            assert kind != null;
        }
    }

}
