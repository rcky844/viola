// Copyright (c) 2021-2025 Tipz Team
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

    val httpPrefix = "http://"
    val httpsPrefix = "https://"

    // Default for getting generic URL regex
    fun getUriRegex(): Regex = getUriRegex(true, false)
    fun getUriRegex(requireStartSlashes: Boolean): Regex =
        getUriRegex(requireStartSlashes, false)

    fun getUriRegex(requireStartSlashes: Boolean, hasDots: Boolean): Regex {
        var firstPart = "(?:[a-z+]+:)?([\\/]+)"
        if (!requireStartSlashes) firstPart += "?"

        var secondPart = "(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\."
        if (!hasDots) secondPart += "?"

        val thirdPart = "[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&\\\\=]*)(/.*)?"

        return "$firstPart$secondPart$thirdPart".toRegex()
    }

    enum class UriScheme(val prefix: String,
                         val requireStartSlashes: Boolean,
                         val ignoreRegex: Boolean = false) {
        SCHEME_HTTP("http", true),
        SCHEME_HTTPS("https",  true),
        SCHEME_FTP("ftp", true),
        SCHEME_FILE("file", true, true),
        SCHEME_DATA("data", false, true),
        SCHEME_JAVASCRIPT("javascript", false, true),
        SCHEME_ABOUT("about", false, true),
        SCHEME_CHROME("chrome", true, true),
        SCHEME_VIOLA("viola", true, true);

        companion object {
            fun getUriScheme(prefix: String): UriScheme? {
                for (it in entries) {
                    if (it.prefix == prefix) return it
                }
                return null
            }
        }
    }

    fun isUriSupported(uri: String): Boolean {
        val scheme = UriScheme.getUriScheme(uri.substringBefore(":")) ?: return false
        if (scheme.ignoreRegex) return true

        val regex = getUriRegex(scheme.requireStartSlashes)
        return uri.matches(regex)
    }

    object UrlOrSearchValidator {
        var isSearch = false

        fun validate(pref: SettingsSharedPreference, input: String) =
            validate(pref, input, 2)

        fun validate(pref: SettingsSharedPreference, input: String,
                                         maxRuns: Int): String {
            isSearch = false

            // Return if input URI is supported
            // Also, enforce HTTPS on URLs that match
            if (isUriSupported(input)) {
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

                        // Check whether it has dots
                        if (!checkedUrl.matches(getUriRegex(true, true)))
                            checkedUrl = input
                    }
                    2 -> {
                        // If run 0 failed, make it a search url
                        checkedUrl = SearchEngineEntries.getPreferredUrl(
                            pref, SearchEngineEntries.EngineInfoType.SEARCH, input)
                        isSearch = true
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
}
