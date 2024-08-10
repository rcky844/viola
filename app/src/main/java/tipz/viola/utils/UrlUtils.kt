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
    val nonStandardUri = listOf("viola", "chrome", "file", "javascript", "about")

    fun isUriLaunchable(uri: String): Boolean {
        return uri.matches(uriRegex) || uri.startsWith("data:")
    }

    fun validateUrlOrConvertToSearch(pref: SettingsSharedPreference, input: String) =
        validateUrlOrConvertToSearch(pref, input, 2)

    fun validateUrlOrConvertToSearch(pref: SettingsSharedPreference, input: String,
                                     maxRuns: Int): String {
        // Check for any non-standard schemes
        if (nonStandardUri.any{ input.matches("$it:(//)?.*".toRegex()) }) return input

        // Start processing
        var checkedUrl = input
        var run = 1
        while (!checkedUrl.matches(uriRegex)) {
            Log.d(LOG_TAG, "toValidHttpUrl(): Uri regex does not match, " +
                    "run=$run, input=$input")
            when (run) {
                1 -> {
                    // Attempt to fix the url by adding in http prefixes
                    checkedUrl = (if (pref.getIntBool(SettingsKeys.enforceHttps))
                        "https://" else "http://") + input
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
