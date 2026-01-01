/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
