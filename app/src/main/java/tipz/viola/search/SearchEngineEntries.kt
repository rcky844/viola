// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.search

import android.text.TextUtils
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import java.util.Locale

object SearchEngineEntries {
    private const val queryPlaceholder = "{query}"
    private const val languagePlaceholder = "{language}"

    data class EngineItem(val name: String, val homePage: String? = null,
                           val search: String? = null, val suggestion: String? = null)
    private val engines = arrayOf(
        EngineItem(name = "google", homePage = "https://www.google.com",
            search = "https://www.google.com/search?q=$queryPlaceholder",
            suggestion = "https://www.google.com/complete/search?client=android&oe=utf8&ie=utf8&cp=4&xssi=t&gs_pcrt=undefined&hl=$languagePlaceholder&q=$queryPlaceholder"
        ),
        EngineItem(name = "baidu", homePage = "https://www.baidu.com",
            search = "https://www.baidu.com/s?wd=$queryPlaceholder",
            suggestion = "https://suggestion.baidu.com/su?ie=UTF-8&wd=$queryPlaceholder&action=opensearch"
        ),
        EngineItem(name = "duckduckgo", homePage = "https://www.duckduckgo.com",
            search = "https://www.duckduckgo.com/?q=$queryPlaceholder",
            suggestion = "https://www.duckduckgo.com/ac/?q=$queryPlaceholder&type=list"
        ),
        EngineItem(name = "bing", homePage = "https://www.bing.com",
            search = "https://www.bing.com/search?q=$queryPlaceholder",
            suggestion = "https://api.bing.com/osjson.aspx?query=$queryPlaceholder&language=$languagePlaceholder"
        ),
        EngineItem(name = "yahoo", homePage = "https://search.yahoo.com",
            search = "https://search.yahoo.com/search?p=$queryPlaceholder",
            suggestion = "https://sugg.search.yahoo.net/sg/?output=fxjson&command=$queryPlaceholder"
        ),
        EngineItem(name = "ecosia", homePage = "https://www.ecosia.org",
            search = "https://www.ecosia.org/search?q=$queryPlaceholder",
            suggestion = "https://ac.ecosia.org/autocomplete?q=$queryPlaceholder&type=list"
        ),
        EngineItem(name = "yandex", homePage = "https://yandex.com",
            search = "https://yandex.com/search/?text=$queryPlaceholder",
            suggestion = "https://yandex.com/suggest/suggest-ya.cgi?v=4&part=$queryPlaceholder"
        ),
        EngineItem(name = "brave", homePage = "https://search.brave.com",
            search = "https://search.brave.com/search?q=$queryPlaceholder",
            suggestion = "https://search.brave.com/api/suggest?q=$queryPlaceholder"
        ),
        EngineItem(name = "startpage", homePage = "https://www.startpage.com",
            search = "https://www.startpage.com/do/search?query=$queryPlaceholder",
            suggestion = "https://www.startpage.com/suggestions?q=$queryPlaceholder"
        ),
        EngineItem(name = "whoogle", homePage = "https://whoogle.io",
            search = "https://whoogle.io/search?q=$queryPlaceholder",
            suggestion = "https://whoogle.io/autocomplete?q=$queryPlaceholder",
        ),
        EngineItem(name = "swisscows", homePage = "https://swisscows.com",
            search = "https://swisscows.com/web?query=$queryPlaceholder",
            suggestion = "https://api.swisscows.com/suggest?query=$queryPlaceholder"
        ),
        EngineItem(name = "qwant", homePage = "https://www.qwant.com",
            search = "https://www.qwant.com/?q=$queryPlaceholder",
            suggestion = "https://api.qwant.com/v3/suggest?q=$queryPlaceholder"
        ),
        EngineItem(name = "sogou", homePage = "https://www.sogou.com",
            search = "https://www.sogou.com/web?query=$queryPlaceholder",
            suggestion = "https://sor.html5.qq.com/api/getsug?key=$queryPlaceholder"
        ),
        EngineItem(name = "so360", homePage = "https://www.so.com",
            search = "https://www.so.com/s?q=$queryPlaceholder",
            suggestion = "https://sug.so.360.cn/suggest?word=$queryPlaceholder"
        ),
        EngineItem(name = "frogfind", homePage = "http://frogfind.com",
            search = "http://frogfind.com/?q=$queryPlaceholder",
        ),
        EngineItem(name = "") /* The object for custom URL */
    )

    private const val defaultEngine = 7
    val customIndex = engines.size - 1

    // TODO: Improvements needed
    fun getNameByIndex(index: Int): String = engines[index].name
    fun getIndexByName(name: String): Int = engines.indexOfFirst { it.name == name }
        .takeUnless { it < 0 || it >= engines.size } ?: defaultEngine

    private fun findByName(name: String): EngineItem? = engines.find { it.name == name }

    fun getPreferredHomePageUrl(pref: SettingsSharedPreference): String =
        pref.getString(SettingsKeys.homePageName).takeUnless { it.isEmpty() }?.let {
            findByName(it)!!.homePage.takeUnless { url -> url.isNullOrBlank() } ?: ""
        } ?: pref.getString(SettingsKeys.homePageCustomUrl)

    fun getPreferredSearchUrl(pref: SettingsSharedPreference, query: String): String =
        query.takeUnless { it.isEmpty() }?.let {
            (pref.getString(SettingsKeys.searchName).takeUnless { it.isEmpty() }?.let {
                findByName(it)!!.search.takeUnless { url -> url.isNullOrBlank() } ?: ""
            } ?: pref.getString(SettingsKeys.searchCustomUrl))
                .replace(queryPlaceholder, query).replace(languagePlaceholder, language)
        } ?: ""

    fun getPreferredSuggestionsUrl(pref: SettingsSharedPreference, query: String): String =
        query.takeUnless { it.isEmpty() }?.let {
            (pref.getString(SettingsKeys.suggestionsName).takeUnless { it.isEmpty() }?.let {
                findByName(it)!!.suggestion.takeUnless { url -> url.isNullOrBlank() } ?: ""
            } ?: pref.getString(SettingsKeys.suggestionsCustomUrl))
                .replace(queryPlaceholder, query).replace(languagePlaceholder, language)
        } ?: ""

    // Language
    private const val DEFAULT_LANGUAGE = "en-US"
    val language: String
        get() {
            var language = Locale.getDefault().language
            val country = Locale.getDefault().country
            if (TextUtils.isEmpty(language)) language = DEFAULT_LANGUAGE
            return "$language-$country"
        }
}
