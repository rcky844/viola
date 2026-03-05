// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.search

import android.content.Context
import android.text.TextUtils
import androidx.annotation.Discouraged
import androidx.core.net.toUri
import tipz.viola.R
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import java.util.Locale

object SearchEngineEntries {
    private const val queryPlaceholder = "{query}"
    private const val languagePlaceholder = "{language}"
    val placeholderList = listOf(queryPlaceholder, languagePlaceholder)

    data class EngineItem(val name: String, val homePage: String? = null,
                           val search: String? = null, val suggestion: String? = null)
    private val engines = arrayOf(
        EngineItem(name = "google", homePage = "https://google.com",
            search = "https://google.com/search?q=$queryPlaceholder",
            suggestion = "https://google.com/complete/search?client=android&oe=utf8&ie=utf8&cp=4&xssi=t&gs_pcrt=undefined&hl=$languagePlaceholder&q=$queryPlaceholder"
        ),
        EngineItem(name = "baidu", homePage = "https://baidu.com",
            search = "https://baidu.com/s?wd=$queryPlaceholder",
            suggestion = "https://suggestion.baidu.com/su?ie=UTF-8&wd=$queryPlaceholder&action=opensearch"
        ),
        EngineItem(name = "duckduckgo", homePage = "https://duckduckgo.com",
            search = "https://duckduckgo.com/?q=$queryPlaceholder",
            suggestion = "https://duckduckgo.com/ac/?q=$queryPlaceholder&type=list"
        ),
        EngineItem(name = "bing", homePage = "https://bing.com",
            search = "https://bing.com/search?q=$queryPlaceholder",
            suggestion = "https://api.bing.com/osjson.aspx?query=$queryPlaceholder&language=$languagePlaceholder"
        ),
        EngineItem(name = "yahoo", homePage = "https://yahoo.com",
            search = "https://search.yahoo.com/search?p=$queryPlaceholder",
            suggestion = "https://sugg.search.yahoo.net/sg/?output=fxjson&command=$queryPlaceholder"
        ),
        EngineItem(name = "aol", homePage = "https://aol.com",
            search = "https://search.aol.com/search?q=$queryPlaceholder",
            suggestion = "https://search.aol.com/sugg/gossip/gossip-us-ura/?output=fxjson&command=$queryPlaceholder"
        ),
        EngineItem(name = "ecosia", homePage = "https://ecosia.org",
            search = "https://ecosia.org/search?q=$queryPlaceholder",
            suggestion = "https://ac.ecosia.org/autocomplete?q=$queryPlaceholder&type=list"
        ),
        EngineItem(name = "yandex", homePage = "https://yandex.com",
            search = "https://yandex.com/search/?text=$queryPlaceholder",
            suggestion = "https://yandex.com/suggest/suggest-ya.cgi?v=4&part=$queryPlaceholder&uil=$languagePlaceholder"
        ),
        EngineItem(name = "brave", homePage = "https://search.brave.com",
            search = "https://search.brave.com/search?q=$queryPlaceholder",
            suggestion = "https://search.brave.com/api/suggest?q=$queryPlaceholder"
        ),
        EngineItem(name = "startpage", homePage = "https://startpage.com",
            search = "https://startpage.com/do/search?query=$queryPlaceholder",
            suggestion = "https://startpage.com/suggestions?q=$queryPlaceholder"
        ),
        EngineItem(name = "presearch", homePage = "https://presearch.com",
            search = "https://presearch.com/search?q=$queryPlaceholder",
            suggestion = "https://rt53.literallysafe.com/getSuggestions?q=$queryPlaceholder"
        ),
        EngineItem(name = "swisscows", homePage = "https://swisscows.com",
            search = "https://swisscows.com/web?query=$queryPlaceholder",
            suggestion = "https://api.swisscows.com/suggest?query=$queryPlaceholder"
        ),
        EngineItem(name = "qwant", homePage = "https://qwant.com",
            search = "https://qwant.com/?q=$queryPlaceholder",
            suggestion = "https://qwant.com/v3/suggest?q=$queryPlaceholder"
        ),
        EngineItem(name = "sogou", homePage = "https://sogou.com",
            search = "https://sogou.com/web?query=$queryPlaceholder",
            suggestion = "https://sor.html5.qq.com/api/getsug?key=$queryPlaceholder"
        ),
        EngineItem(name = "so360", homePage = "https://so.com",
            search = "https://so.com/s?q=$queryPlaceholder",
            suggestion = "https://sug.so.360.cn/suggest?word=$queryPlaceholder"
        ),
        EngineItem(
            name = "frogfind", homePage = "http://frogfind.com",
            search = "http://frogfind.com/?q=$queryPlaceholder",
        ),
        EngineItem(name = "") /* The object for custom URL */
    )
    const val defaultEngineName = "brave"

    enum class EngineInfoType {
        HOMEPAGE, SEARCH, SUGGESTION
    }

    private fun findByName(name: String): EngineItem? = engines.find { it.name == name }
    private fun getHostUrl(item: EngineItem): String? = item.homePage?.toUri()?.host
    private fun checkEngineInfoType(item: EngineItem, type: EngineInfoType): Boolean =
        when (type) {
            EngineInfoType.HOMEPAGE -> item.homePage
            EngineInfoType.SEARCH -> item.search
            EngineInfoType.SUGGESTION -> item.suggestion
        } != null || item.name == ""

    fun getEngineNameList(type: EngineInfoType): Array<String> {
        val array = mutableListOf<String>()
        engines.forEach { if (checkEngineInfoType(it, type)) array.add(it.name) }
        return array.toTypedArray()
    }

    fun getEngineDisplayList(context: Context, type: EngineInfoType): Array<String> {
        val displayList = context.resources.getStringArray(R.array.search_entries)
        val array = mutableListOf<String>()
        engines.forEachIndexed { i, it ->
            if (checkEngineInfoType(engines[i], type)) {
                val hostUrl = getHostUrl(it)
                val display = displayList[i]
                array.add(if (hostUrl.isNullOrEmpty()) display else "$display ($hostUrl)")
            }
        }
        return array.toTypedArray()
    }

    fun getPreferredUrl(pref: SettingsSharedPreference,
                        type: EngineInfoType,
                        query: String = ""): String {
        if (type != EngineInfoType.HOMEPAGE && query.isEmpty()) return ""

        val namePref = when (type) {
            EngineInfoType.HOMEPAGE -> SettingsKeys.homePageName
            EngineInfoType.SEARCH -> SettingsKeys.searchName
            EngineInfoType.SUGGESTION -> SettingsKeys.suggestionsName
        }

        val customPref = when (type) {
            EngineInfoType.HOMEPAGE -> SettingsKeys.homePageCustomUrl
            EngineInfoType.SEARCH -> SettingsKeys.searchCustomUrl
            EngineInfoType.SUGGESTION -> SettingsKeys.suggestionsCustomUrl
        }

        val url = pref.getString(namePref).takeUnless { it.isEmpty() }?.let {
            findByName(it)!!.let {
                when (type) {
                    EngineInfoType.HOMEPAGE -> it.homePage
                    EngineInfoType.SEARCH -> it.search
                    EngineInfoType.SUGGESTION -> it.suggestion
                }
            }.takeUnless { url -> url.isNullOrBlank() } ?: ""
        } ?: pref.getString(customPref)

        return if (type == EngineInfoType.HOMEPAGE) url
        else url.replace(queryPlaceholder, query).replace(languagePlaceholder, language)
    }

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
