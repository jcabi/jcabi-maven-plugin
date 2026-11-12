/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

assert new File(basedir, 'build.log').text.contains('Not executing AJC as nothing in')
assert !new File(basedir, 'target/classes/jcabi-ajc.done').exists()
assert new File(basedir, 'target/jcabi-ajc/jcabi-ajc.done').exists()
assert basedir.list().findAll { it.startsWith('ajcore') }.isEmpty()
File woven = new File(basedir, 'target/classes/com/jcabi/foo/Sample.class')
File unwoven = new File(basedir, 'target/customUnwoven/com/jcabi/foo/Sample.class')
assert !Arrays.equals(woven.readBytes(), unwoven.readBytes())
