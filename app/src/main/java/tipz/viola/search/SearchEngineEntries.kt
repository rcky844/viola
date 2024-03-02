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
            EngineObject().setHomePage("https://www.startpage.com/")
                    .setSearch("https://startpage.com/do/search?query={query}")
                    .setSuggestion("https://www.startpage.com/suggestions?q={query}"),
            EngineObject().setHomePage("https://whoogle.io/")
                    .setSearch("https://whoogle.io/search?q={query}")
                    .setSuggestion("https://whoogle.io/autocomplete?q={query}"),
            EngineObject().setHomePage("https://swisscows.com/")
                    .setSearch("https://swisscows.com/web?query={query}")
                    .setSuggestion("https://api.swisscows.com/suggest?query={query}"),
            EngineObject().setHomePage("https://www.qwant.com/")
                    .setSearch("https://www.qwant.com/?q={query}")
                    .setSuggestion("https://api.qwant.com/v3/suggest?q={query}"),
            EngineObject().setHomePage("https://www.sogou.com/")
                    .setSearch("https://www.sogou.com/web?query={query}")
                    .setSuggestion("https://sor.html5.qq.com/api/getsug?key={query}"),
            EngineObject().setHomePage("https://www.so.com/")
                    .setSearch("https://www.so.com/s?q={query}")
                    .setSuggestion("https://sug.so.360.cn/suggest?word={query}"),
            EngineObject() /* The object for custom URL */
    )

    fun getHomePageUrl(pref: SettingsSharedPreference?, position: Int): String {
        var url: String? = engines[position].homePage
        if (url!!.isEmpty()) url = pref!!.getString(SettingsKeys.defaultHomePage)
        return cve_2017_13274(url!!)
    }

    fun getSearchUrl(
            pref: SettingsSharedPreference?,
            position: Int,
            query: String?,
            language: String?
    ): String {
        var url: String? = engines[position].search
        if (url!!.isEmpty()) url = pref!!.getString(SettingsKeys.defaultSearch)
        if (query != null) url =
                url!!.replace(queryPlaceholder, query).replace(languagePlaceholder, language!!)
        return cve_2017_13274(url!!)
    }

    @JvmStatic
    fun getSuggestionsUrl(
            pref: SettingsSharedPreference?,
            position: Int,
            query: String?,
            language: String?
    ): String {
        var url: String? = engines[position].suggestion
        if (url!!.isEmpty()) url = pref!!.getString(SettingsKeys.defaultSuggestions)
        if (query != null && language != null) url = url!!.replace(queryPlaceholder, query).replace(
                languagePlaceholder, language
        )
        return cve_2017_13274(url!!)
    }

    fun getEngineListSize(): Int {
        return engines.size
    }

    fun getCustomIndex(): Int {
        return engines.size - 1
    }
}