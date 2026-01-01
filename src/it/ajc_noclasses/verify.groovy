/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

/*
 No classes were found in classesDirectory, so no unwoven classes to copy in
 unwovenClassesDir.
*/

assert new File(basedir, 'target/classes').list().length == 0
assert !new File(basedir, 'target/unwoven').exists()
