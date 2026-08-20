/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.maven.plugin;

import com.jcabi.log.Logger;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHolder;

/**
 * Message holder.
 * @since 0.1
 */
final class MsgHolder implements IMessageHolder {

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
    public int numMessages(final IMessage.Kind kind, final boolean greater) {
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
