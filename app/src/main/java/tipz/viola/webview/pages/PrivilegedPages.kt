// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.pages

class PrivilegedPages {
    companion object {
        val privilegedPages = arrayOf(
            PageObject().apply {
                displayUrl = "viola://start"
                showEmptyUrl = true
                actualUrl = ProjectUrls.actualStartUrl
            },
            PageObject().apply {
                displayUrl = "viola://license"
                actualUrl = ProjectUrls.actualLicenseUrl
            },
        )

        fun isPrivilegedPage(actualUrl: String?) = privilegedPages.any { it.actualUrl == actualUrl }
        fun getDisplayUrl(actualUrl: String?): String? {
            return try {
                privilegedPages.first { it.actualUrl == actualUrl }.displayUrl
            } catch (_: Exception) {
                null
            }
        }
        fun getActualUrl(displayUrl: String?): String? {
            return try {
                privilegedPages.first { it.displayUrl == displayUrl }.actualUrl
            } catch (_: Exception) {
                null
            }
        }
        fun shouldShowEmptyUrl(actualUrl: String?): Boolean {
            return try {
                privilegedPages.first { actualUrl!!.contains(it.actualUrl!!) }.showEmptyUrl
            } catch (_: Exception) {
                false
            }
        }
    }

    class PageObject {
        var displayUrl: String? = null
        var showEmptyUrl = false
        var actualUrl: String? = null
    }
}