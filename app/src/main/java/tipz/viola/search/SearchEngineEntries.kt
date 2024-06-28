/*
 * Copyright (c) 2022-2024 Tipz Team
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
package tipz.viola.search

import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.utils.UrlUtils.patchUrlForCVEMitigation

object SearchEngineEntries {
    private const val queryPlaceholder = "{query}"
    private const val languagePlaceholder = "{language}"
    private val engines = arrayOf(
        EngineObject().apply {
            homePage = "https://www.google.com"
            search = "$homePage/search?q=$queryPlaceholder"
            suggestion = "http://suggestqueries.google.com/complete/search?client=android&oe=utf8&ie=utf8&q=$queryPlaceholder&hl=$languagePlaceholder"
        },
        EngineObject().apply {
            homePage = "https://www.baidu.com"
            search = "$homePage/s?wd=$queryPlaceholder"
            suggestion = "https://suggestion.baidu.com/su?ie=UTF-8&wd=$queryPlaceholder&action=opensearch"
        },
        EngineObject().apply {
            homePage = "https://www.duckduckgo.com"
            search = "$homePage/?q=$queryPlaceholder"
            suggestion = "$homePage/ac/?q=$queryPlaceholder&type=list"
        },
        EngineObject().apply {
            homePage = "https://www.bing.com"
            search = "$homePage/search?q=$queryPlaceholder"
            suggestion = "https://api.bing.com/osjson.aspx?query=$queryPlaceholder&language=$languagePlaceholder"
        },
        EngineObject().apply {
            homePage = "https://search.yahoo.com"
            search = "$homePage/search?p=$queryPlaceholder"
            suggestion = "https://sugg.search.yahoo.net/sg/?output=fxjson&command=$queryPlaceholder"
        },
        EngineObject().apply {
            homePage = "https://www.ecosia.org"
            search = "$homePage/search?q=$queryPlaceholder"
            suggestion = "https://ac.ecosia.org/autocomplete?q=$queryPlaceholder&type=list"
        },
        EngineObject().apply {
            homePage = "https://yandex.com"
            search = "$homePage/search/?text=$queryPlaceholder"
            suggestion = "$homePage/suggest/suggest-ya.cgi?v=4&part=$queryPlaceholder"
        },
        EngineObject().apply {
            homePage = "https://search.brave.com"
            search = "$homePage/search?q=$queryPlaceholder"
            suggestion = "$homePage/api/suggest?q=$queryPlaceholder"
        },
        EngineObject().apply {
            homePage = "https://www.startpage.com"
            search = "$homePage/do/search?query=$queryPlaceholder"
            suggestion = "$homePage/suggestions?q=$queryPlaceholder"
        },
        EngineObject().apply {
            homePage = "https://whoogle.io"
            search = "$homePage/search?q=$queryPlaceholder"
            suggestion = "$homePage/autocomplete?q=$queryPlaceholder"
        },
        EngineObject().apply {
            homePage = "https://swisscows.com"
            search = "$homePage/web?query=$queryPlaceholder"
            suggestion = "https://api.swisscows.com/suggest?query=$queryPlaceholder"
        },
        EngineObject().apply {
            homePage = "https://www.qwant.com"
            search = "$homePage/?q=$queryPlaceholder"
            suggestion = "https://api.qwant.com/v3/suggest?q=$queryPlaceholder"
        },
        EngineObject().apply {
            homePage = "https://www.sogou.com"
            search = "$homePage/web?query=$queryPlaceholder"
            suggestion = "https://sor.html5.qq.com/api/getsug?key=$queryPlaceholder"
        },
        EngineObject().apply {
            homePage = "https://www.so.com"
            search = "$homePage/s?q=$queryPlaceholder"
            suggestion = "https://sug.so.360.cn/suggest?word=$queryPlaceholder"
        },
        EngineObject() /* The object for custom URL */
    )

    val customIndex = engines.size - 1

    fun getHomePageUrl(pref: SettingsSharedPreference?, position: Int): String {
        var url: String? = engines[position].homePage
        if (url.isNullOrEmpty()) url = pref!!.getString(SettingsKeys.defaultHomePage)
        return patchUrlForCVEMitigation(url!!)
    }

    fun getSearchUrl(
            pref: SettingsSharedPreference?,
            position: Int,
            query: String?,
            language: String?
    ): String {
        var url: String? = engines[position].search
        if (url.isNullOrEmpty()) url = pref!!.getString(SettingsKeys.defaultSearch)
        if (query != null) url =
                url!!.replace(queryPlaceholder, query).replace(languagePlaceholder, language!!)
        return patchUrlForCVEMitigation(url!!)
    }

    @JvmStatic
    fun getSuggestionsUrl(
            pref: SettingsSharedPreference?,
            position: Int,
            query: String?,
            language: String?
    ): String {
        var url: String? = engines[position].suggestion
        if (url.isNullOrEmpty()) url = pref!!.getString(SettingsKeys.defaultSuggestions)
        if (query != null && language != null) url = url!!.replace(queryPlaceholder, query).replace(
                languagePlaceholder, language
        )
        return patchUrlForCVEMitigation(url!!)
    }
}