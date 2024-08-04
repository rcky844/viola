// Copyright (c) 2021-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.utils

import android.os.Build
import android.util.Log
import tipz.viola.search.SearchEngineEntries
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.utils.CommonUtils.language

object UrlUtils {
    private const val LOG_TAG = "UrlUtils"

    /**
     * An array used for intent filtering
     */
    val TypeSchemeMatch = arrayOf(
        "text/html", "text/plain", "application/xhtml+xml", "application/vnd.wap.xhtml+xml",
        "http", "https", "ftp", "file"
    )
    const val protocolRegex = "^(?:[a-z+]+:)?//"
    const val httpUrlRegex =
        "${protocolRegex}(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&\\\\=]*)(/.*)?"

    /**
     * Some revisions of Android (before 2018-04-01 SPL) before Android Pie has
     * security flaws in producing correct host name from url string in android.net.Uri,
     * patch it ourselves.
     *
     *
     * Ref: CVE-2017-13274
     *
     * @param url supplied url to check.
     * @return fixed up url
     */
    fun patchUrlForCVEMitigation(url: String): String {
        return if (url.contains("\\") && Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) url.replace(
            "\\",
            "/"
        ) else url
    }

    fun isUriLaunchable(uri: String): Boolean {
        return uri.matches(httpUrlRegex.toRegex()) || uri.startsWith("data:")
    }

    fun toSearchOrValidUrl(pref: SettingsSharedPreference, input: String): String {
        val processedInput = patchUrlForCVEMitigation(input.trim())
        var finalUrl = toValidHttpUrl(pref, processedInput)

        if (finalUrl.isBlank()) {
            finalUrl = SearchEngineEntries.getSearchUrl(
                pref.getString(SettingsKeys.searchName),
                processedInput, language
            )
            Log.d(LOG_TAG, "toSearchOrValidUrl(): at httpUrlRegex, finalUrl=$finalUrl")
        }
        return finalUrl
    }

    fun toValidHttpUrl(pref: SettingsSharedPreference, input: String): String {
        val processedInput = patchUrlForCVEMitigation(input.trim())
        var finalUrl = processedInput
        if (!processedInput.matches("${protocolRegex}.*".toRegex())) { // is relative
            finalUrl = (if (pref.getIntBool(SettingsKeys.enforceHttps)) "https://"
            else "http://") + input
            Log.d(LOG_TAG, "toValidHttpUrl(): at is relative, finalUrl=$finalUrl")
        }
        if (finalUrl.matches(httpUrlRegex.toRegex())) return finalUrl
        else return "" // This means the checks failed
    }
}
