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

import android.content.Context
import android.net.Uri
import android.os.Build
import tipz.viola.Application
import tipz.viola.search.SearchEngineEntries
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.CommonUtils.language

object UrlUtils {
    /**
     * An array used for intent filtering
     */
    val TypeSchemeMatch = arrayOf(
        "text/html", "text/plain", "application/xhtml+xml", "application/vnd.wap.xhtml+xml",
        "http", "https", "ftp", "file"
    )
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
    fun toSearchOrValidUrl(context: Context, input: String): String {
        val settingsPreference = (context.applicationContext as Application).settingsPreference!!
        val trimmedInput = patchUrlForCVEMitigation(input.trim { it <= ' ' })
        var uri = Uri.parse(trimmedInput)
        if (uri.isRelative) {
            uri =
                Uri.parse((if (settingsPreference.getIntBool(SettingsKeys.enforceHttps)) "https://" else "http://") + trimmedInput)
            if (!uri.toString().matches(httpUrlRegex.toRegex())) {
                return SearchEngineEntries.getSearchUrl(
                    settingsPreference,
                    settingsPreference.getInt(SettingsKeys.defaultSearchId),
                    input, language
                )
            }
        }
        return uri.toString()
    }
}