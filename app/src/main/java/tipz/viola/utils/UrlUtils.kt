/*
 * Copyright (C) 2021-2023 Tipz Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tipz.viola.utils

import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
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
    private const val protocolRegex = "^(?:[a-z+]+:)?//"
    private const val httpUrlRegex =
        "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&\\\\=]*)(/.*)?"

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

    /**
     * URL Checker
     *
     *
     * Checks if URL is valid, if not, make it a search term.
     *
     * @param input the input to check.
     * @return result
     */
    fun toSearchOrValidUrl(settingsPreference: SettingsSharedPreference, input: String): String {
        val processedInput = patchUrlForCVEMitigation(input.trim())
        var finalUrl = processedInput
        if (!processedInput.matches(protocolRegex.toRegex())) { // is relative
            finalUrl = (if (settingsPreference.getIntBool(SettingsKeys.enforceHttps)) "https://"
                else "http://") + input
            Log.d(LOG_TAG, "toSearchOrValidUrl(): at is relative, finalUrl=$finalUrl")
        }
        if (!finalUrl.matches(httpUrlRegex.toRegex())) {
            finalUrl = SearchEngineEntries.getSearchUrl(
                settingsPreference.getString(SettingsKeys.searchName),
                processedInput, language)
            Log.d(LOG_TAG, "toSearchOrValidUrl(): at httpUrlRegex, finalUrl=$finalUrl")
        }
        return finalUrl
    }
}
