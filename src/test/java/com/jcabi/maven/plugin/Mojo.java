/*
 * Copyright (c) 2012-2020, jcabi.com
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Mutable mojo builder.
 *
 * @param <T> Type of mojo
 * @since 0.1
 */
final class Mojo<T extends AbstractMojo> {

    /**
     * The type of mojo.
     */
    private final Class<T> type;

    /**
     * All attributes.
     */
    private final Map<String, Object> attrs;

    /**
     * Ctor.
     *
     * @param tpe The type
     */
    Mojo(final Class<T> tpe) {
        this.type = tpe;
        this.attrs = new HashMap<>(0);
    }

    /**
     * Add one more attribute and return self.
     *
     * @param attr The name
     * @param value The value
     * @return Itself
     */
    public Mojo<T> with(final String attr, final Object value) {
        this.attrs.put(attr, value);
        return this;
    }

    /**
     * Execute it.
     */
    public void execute() {
        try {
            final AbstractMojo mojo = this.type.getConstructor().newInstance();
            for (final Map.Entry<String, Object> ent : this.attrs.entrySet()) {
                final Field field = this.field(this.type, ent.getKey());
                field.setAccessible(true);
                field.set(mojo, ent.getValue());
            }
            mojo.execute();
        } catch (final MojoExecutionException | MojoFailureException
            | InstantiationException | IllegalAccessException
            | NoSuchMethodException | InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Take a field.
     * @param mojo The class
     * @param name Field name
     * @return Field
     */
    private Field field(final Class<?> mojo, final String name) {
        Field field;
        try {
            field = mojo.getDeclaredField(name);
        } catch (final NoSuchFieldException ex) {
            final Class<?> parent = mojo.getSuperclass();
            if (parent == null) {
                throw new IllegalStateException(
                    String.format(
                        "Can't find \"%s\" in %s",
                        name,
                        this.type.getCanonicalName()
                    ),
                    ex
                );
            }
            field = this.field(parent, name);
        }
        return field;
    }

}
