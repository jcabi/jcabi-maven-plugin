/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

def file = new File(basedir, 'target/classes/com/jcabi/foo/com-jcabi-jcabi-test-jar.txt')
assert file.text.contains('Project Version: 1.0')
assert file.text.contains('Sample.class')
assert file.text.contains('Sample.txt')
