// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.ext

data class Matcher(val name: String, val action: () -> Unit)
fun String.matchAndExec(c: List<Matcher>): Boolean {
    c.forEach {
        if (this == it.name) {
            it.action()
            return true
        }
    }
    return false
}