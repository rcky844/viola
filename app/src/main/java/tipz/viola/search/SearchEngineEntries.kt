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
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.UrlUtils.patchUrlForCVEMitigation

object SearchEngineEntries {
    private const val queryPlaceholder = "{query}"
    private const val languagePlaceholder = "{language}"
    private val engines = arrayOf(
        EngineObject().apply {
            name = "google"
            homePage = "https://www.google.com"
            search = "$homePage/search?q=$queryPlaceholder"
            suggestion = "https://suggestqueries.google.com/complete/search?client=android&oe=utf8&ie=utf8&q=$queryPlaceholder&hl=$languagePlaceholder"
        },
        EngineObject().apply {
            name = "baidu"
            homePage = "https://www.baidu.com"
            search = "$homePage/s?wd=$queryPlaceholder"
            suggestion = "https://suggestion.baidu.com/su?ie=UTF-8&wd=$queryPlaceholder&action=opensearch"
        },
        EngineObject().apply {
            name = "duckduckgo"
            homePage = "https://www.duckduckgo.com"
            search = "$homePage/?q=$queryPlaceholder"
            suggestion = "$homePage/ac/?q=$queryPlaceholder&type=list"
        },
        EngineObject().apply {
            name = "bing"
            homePage = "https://www.bing.com"
            search = "$homePage/search?q=$queryPlaceholder"
            suggestion = "https://api.bing.com/osjson.aspx?query=$queryPlaceholder&language=$languagePlaceholder"
        },
        EngineObject().apply {
            name = "yahoo"
            homePage = "https://search.yahoo.com"
            search = "$homePage/search?p=$queryPlaceholder"
            suggestion = "https://sugg.search.yahoo.net/sg/?output=fxjson&command=$queryPlaceholder"
        },
        EngineObject().apply {
            name = "ecosia"
            homePage = "https://www.ecosia.org"
            search = "$homePage/search?q=$queryPlaceholder"
            suggestion = "https://ac.ecosia.org/autocomplete?q=$queryPlaceholder&type=list"
        },
        EngineObject().apply {
            name = "yandex"
            homePage = "https://yandex.com"
            search = "$homePage/search/?text=$queryPlaceholder"
            suggestion = "$homePage/suggest/suggest-ya.cgi?v=4&part=$queryPlaceholder"
        },
        EngineObject().apply {
            name = "brave"
            homePage = "https://search.brave.com"
            search = "$homePage/search?q=$queryPlaceholder"
            suggestion = "$homePage/api/suggest?q=$queryPlaceholder"
        },
        EngineObject().apply {
            name = "startpage"
            homePage = "https://www.startpage.com"
            search = "$homePage/do/search?query=$queryPlaceholder"
            suggestion = "$homePage/suggestions?q=$queryPlaceholder"
        },
        EngineObject().apply {
            name = "whoogle"
            homePage = "https://whoogle.io"
            search = "$homePage/search?q=$queryPlaceholder"
            suggestion = "$homePage/autocomplete?q=$queryPlaceholder"
        },
        EngineObject().apply {
            name = "swisscows"
            homePage = "https://swisscows.com"
            search = "$homePage/web?query=$queryPlaceholder"
            suggestion = "https://api.swisscows.com/suggest?query=$queryPlaceholder"
        },
        EngineObject().apply {
            name = "qwant"
            homePage = "https://www.qwant.com"
            search = "$homePage/?q=$queryPlaceholder"
            suggestion = "https://api.qwant.com/v3/suggest?q=$queryPlaceholder"
        },
        EngineObject().apply {
            name = "sogou"
            homePage = "https://www.sogou.com"
            search = "$homePage/web?query=$queryPlaceholder"
            suggestion = "https://sor.html5.qq.com/api/getsug?key=$queryPlaceholder"
        },
        EngineObject().apply {
            name = "so360"
            homePage = "https://www.so.com"
            search = "$homePage/s?q=$queryPlaceholder"
            suggestion = "https://sug.so.360.cn/suggest?word=$queryPlaceholder"
        },
        EngineObject().apply {
            name = "frogfind"
            homePage = "http://frogfind.com"
            search = "$homePage/?q=$queryPlaceholder"
            suggestion = null
        },
        EngineObject() /* The object for custom URL */
    )

    val customIndex = engines.size - 1

    private fun findObjWithName(name: String): EngineObject? = engines.find {
        it.name == name
    }

    // TODO: Remove
    fun getNameByIndex(index: Int): String = engines[index].name
    fun getIndexByName(name: String): Int = engines.indexOfFirst {
        it.name == name
    }

    fun getHomePageUrl(name: String): String {
        return patchUrlForCVEMitigation(findObjWithName(name)!!.homePage!!)
    }

    fun getDefaultHomeUrl(pref: SettingsSharedPreference?): String {
        val name = pref!!.getString(SettingsKeys.homePageName)
        if (name == CommonUtils.EMPTY_STRING) {
            return pref.getString(SettingsKeys.homePageCustomUrl)
        }
        return getHomePageUrl(name)
    }

    fun getSearchUrl(
        name: String,
        query: String?,
        language: String?
    ): String {
        var url: String = findObjWithName(name)!!.search!!
        if (query != null)
            url = url.replace(queryPlaceholder, query).replace(languagePlaceholder, language!!)
        return patchUrlForCVEMitigation(url)
    }

    fun getDefaultSearchUrl(pref: SettingsSharedPreference?,
                            query: String?, language: String?): String {
        val name = pref!!.getString(SettingsKeys.searchName)
        if (name == CommonUtils.EMPTY_STRING) {
            return pref.getString(SettingsKeys.searchCustomUrl)
        }
        return getSearchUrl(name, query, language)
    }

    fun getSuggestionsUrl(
        name: String,
        query: String?,
        language: String?
    ): String {
        var url: String = findObjWithName(name)!!.suggestion!!
        if (query != null && language != null)
            url = url.replace(queryPlaceholder, query).replace(
                languagePlaceholder, language)
        return patchUrlForCVEMitigation(url)
    }

    fun getDefaultSuggestionsUrl(pref: SettingsSharedPreference?,
                            query: String?, language: String?): String {
        val name = pref!!.getString(SettingsKeys.suggestionsName)
        if (name == CommonUtils.EMPTY_STRING) {
            return pref.getString(SettingsKeys.suggestionsCustomUrl)
        }
        return getSuggestionsUrl(name, query, language)
    }
}
