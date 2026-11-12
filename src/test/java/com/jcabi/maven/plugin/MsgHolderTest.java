/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.maven.plugin;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageUtil;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link MsgHolder}.
 *
 * @since 0.18
 */
final class MsgHolderTest {

    @Test
    void listsHandledMessages() {
        final IMessageHolder holder = new MsgHolder();
        final IMessage msg = MessageUtil.abort("bröken äspect 7f");
        holder.handleMessage(msg);
        MatcherAssert.assertThat(
            "holder cannot lose a message it handled",
            holder.getUnmodifiableListView(),
            Matchers.contains(msg)
        );
    }
}
