// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.ext

fun String.equalsWithIgnore(other: String, ignoreStartIndex: Int = length): Boolean {
    val b = substring(0, ignoreStartIndex - 1) == other
    return if (b) true
    else this == other
}