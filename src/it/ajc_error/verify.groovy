/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

assert new File(basedir, 'build.log').text.contains('AJC failed, see log above')
assert new File(basedir, 'target/jcabi-ajc.log').text.contains('Missing cannot be resolved')
