/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

assert !new File(basedir, 'target/classes/jcabi-ajc.log').exists()
assert new File(basedir, 'target/jcabi-ajc.log').exists()
File wovenClasses = new File(basedir, 'target/classes/com/jcabi/foo/Sample.class')
File unwovenClasses = new File(basedir, 'target/unwoven')
assert wovenClasses.exists()
assert !unwovenClasses.exists()
