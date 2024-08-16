// Copyright (c) 2021-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.utils

import android.util.Log
import tipz.viola.search.SearchEngineEntries
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

object UrlUtils {
    private const val LOG_TAG = "UrlUtils"

    /* Some regex and hardcoded strings */
    val TypeSchemeMatch = arrayOf(
        "text/html", "text/plain", "application/xhtml+xml", "application/vnd.wap.xhtml+xml",
        "http", "https", "ftp", "file"
    )
    val uriRegex =
        ("^(?:[a-z+]+:)?//" +
                "(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&\\\\=]*)(/.*)?")
            .toRegex()
    val supportedUriScheme =
        listOf("http", "https", "ftp", "file", "data",
            "viola", "chrome", "javascript", "about")
    val httpPrefix = "http://"
    val httpsPrefix = "https://"

    fun isUriSupported(uri: String): Boolean {
        return uri.matches(uriRegex)
                && supportedUriScheme.any { uri.matches("$it:(//)?.*".toRegex()) }
    }

    fun validateUrlOrConvertToSearch(pref: SettingsSharedPreference, input: String) =
        validateUrlOrConvertToSearch(pref, input, 2)

    fun validateUrlOrConvertToSearch(pref: SettingsSharedPreference, input: String,
                                     maxRuns: Int): String {
        // Return if input matches URI regex
        // Also, enforce HTTPS on URLs that match
        if (input.matches(uriRegex)) {
            return if (input.startsWith(httpPrefix) && pref.getIntBool(SettingsKeys.enforceHttps))
                input.replaceFirst(httpPrefix, httpsPrefix)
            else input
        }

        // Start processing
        var checkedUrl = input
        var run = 1
        while (!isUriSupported(checkedUrl)) {
            Log.d(LOG_TAG, "toValidHttpUrl(): Uri regex does not match, " +
                    "run=$run, input=$input")
            when (run) {
                1 -> {
                    // Attempt to fix the url by adding in http prefixes
                    checkedUrl = (if (pref.getIntBool(SettingsKeys.enforceHttps))
                        httpsPrefix else httpPrefix) + input
                }
                2 -> {
                    // If run 0 failed, make it a search url
                    checkedUrl = SearchEngineEntries.getPreferredSearchUrl(pref, input)
                }
                else -> {
                    Log.d(LOG_TAG, "toValidHttpUrl(): Unable to convert into valid url!")
                    checkedUrl = "" // Provide empty string on error
                    break
                }
            }
            if (run == maxRuns) break
            run++
        }

        return checkedUrl.trim()
    }
}
