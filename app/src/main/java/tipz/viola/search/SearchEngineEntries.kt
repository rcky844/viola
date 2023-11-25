/*
 * Copyright (C) 2022-2023 Tipz Team
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

import android.content.SharedPreferences
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsUtils.getPref
import tipz.viola.utils.UrlUtils.cve_2017_13274

object SearchEngineEntries {
    private const val queryPlaceholder = "{query}"
    private const val languagePlaceholder = "{language}"
    private val engines = arrayOf(
        EngineObject().setHomePage("https://www.google.com")
            .setSearch("https://www.google.com/search?q={query}")
            .setSuggestion("http://suggestqueries.google.com/complete/search?client=android&oe=utf8&ie=utf8&q={query}&hl={language}"),
        EngineObject().setHomePage("https://www.baidu.com")
            .setSearch("https://www.baidu.com/s?wd={query}")
            .setSuggestion("http://suggestion.baidu.com/su?ie=UTF-8&wd={query}&action=opensearch"),
        EngineObject().setHomePage("https://www.duckduckgo.com")
            .setSearch("https://www.duckduckgo.com/?q={query}")
            .setSuggestion("https://duckduckgo.com/ac/?q={query}&type=list"),
        EngineObject().setHomePage("https://www.bing.com")
            .setSearch("https://www.bing.com/search?q={query}")
            .setSuggestion("https://api.bing.com/osjson.aspx?query={query}&language={language}"),
        EngineObject().setHomePage("https://search.yahoo.com")
            .setSearch("https://search.yahoo.com/search?p={query}")
            .setSuggestion("https://sugg.search.yahoo.net/sg/?output=fxjson&command={query}"),
        EngineObject().setHomePage("https://www.ecosia.org")
            .setSearch("https://www.ecosia.org/search?q={query}")
            .setSuggestion("https://ac.ecosia.org/autocomplete?q={query}&type=list"),
        EngineObject().setHomePage("https://yandex.com")
            .setSearch("https://yandex.com/search/?text={query}")
            .setSuggestion("https://yandex.com/suggest/suggest-ya.cgi?v=4&part={query}"),
        EngineObject().setHomePage("https://search.brave.com")
            .setSearch("https://search.brave.com/search?q={query}")
            .setSuggestion("https://search.brave.com/api/suggest?q={query}"),
        EngineObject() /* The object for custom URL */
    )

    fun getHomePageUrl(pref: SharedPreferences?, position: Int): String {
        var url: String? = engines[position].homePage
        if (url!!.isEmpty()) url = getPref(pref!!, SettingsKeys.defaultHomePage)
        return cve_2017_13274(url!!)
    }

    fun getSearchUrl(
        pref: SharedPreferences?,
        position: Int,
        query: String?,
        language: String?
    ): String {
        var url: String? = engines[position].search
        if (url!!.isEmpty()) url = getPref(pref!!, SettingsKeys.defaultSearch)
        if (query != null) url =
            url!!.replace(queryPlaceholder, query).replace(languagePlaceholder, language!!)
        return cve_2017_13274(url!!)
    }

    @JvmStatic
    fun getSuggestionsUrl(
        pref: SharedPreferences?,
        position: Int,
        query: String?,
        language: String?
    ): String {
        var url: String? = engines[position].suggestion
        if (url!!.isEmpty()) url = getPref(pref!!, SettingsKeys.defaultSuggestions)
        if (query != null && language != null) url = url!!.replace(queryPlaceholder, query).replace(
            languagePlaceholder, language
        )
        return cve_2017_13274(url!!)
    }
}