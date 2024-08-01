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

import android.content.Context
import org.json.JSONArray
import tipz.viola.Application
import tipz.viola.utils.CommonUtils
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/*
    "Inspired" by LineageOS' Jelly
 */
open class SuggestionProvider(private val mContext: Context) {
    private val mEncoding: String = encoding
    private val mLanguage: String = language

    /**
     * Create a URL for the given query in the given language.
     *
     * @param query    the query that was made.
     * @param language the locale of the user.
     * @return should return a URL that can be fetched using a GET.
     */
    private fun createQueryUrl(
        query: String,
        language: String
    ): String {
        val settingsPreference = (mContext.applicationContext as Application).settingsPreference
        return SearchEngineEntries.getDefaultSuggestionsUrl(settingsPreference, query, language)
    }

    /**
     * Parse the results of an input stream into a list of [String].
     *
     * @param content  the raw input to parse.
     * @param callback the callback to invoke for each received suggestion
     * @throws Exception throw an exception if anything goes wrong.
     */
    @Throws(Exception::class)
    fun parseResults(
        content: String,
        callback: ResultCallback
    ) {
        val respArray = JSONArray(content)
        val jsonArray = respArray.getJSONArray(1)
        val size = jsonArray.length()
        for (n in 0 until size) {
            val suggestion = jsonArray.getString(n)
            if (!callback.addResult(suggestion)) break
        }
    }

    /**
     * Retrieves the results for a query.
     *
     * @param rawQuery the raw query to retrieve the results for.
     * @return a list of history items for the query.
     */
    fun fetchResults(rawQuery: String): List<String> {
        val filter: MutableList<String> = ArrayList(5)
        val query: String = try {
            URLEncoder.encode(rawQuery, mEncoding)
        } catch (e: UnsupportedEncodingException) {
            return filter
        }
        val content = downloadSuggestionsForQuery(query, mLanguage)
            ?: // There are no suggestions for this query, return an empty list.
            return filter
        try {
            parseResults(content) {
                filter.add(it!!)
                filter.size < 5
            }
        } catch (ignored: Exception) {
        }
        return filter
    }

    /**
     * This method downloads the search suggestions for the specific query.
     * NOTE: This is a blocking operation, do not fetchResults on the UI thread.
     *
     * @param query the query to get suggestions for
     * @return the cache file containing the suggestions
     */
    private fun downloadSuggestionsForQuery(
        query: String,
        language: String
    ): String? {
        try {
            val url = URL(createQueryUrl(query, language))
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.addRequestProperty(
                "Cache-Control",
                "max-age=$INTERVAL_DAY, max-stale=$INTERVAL_DAY"
            )
            urlConnection.addRequestProperty("Accept-Charset", mEncoding)
            try {
                BufferedInputStream(urlConnection.inputStream).use { `in` ->
                    val reader = BufferedReader(InputStreamReader(`in`, getEncoding(urlConnection)))
                    val result = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) result.append(line)
                    return result.toString()
                }
            } finally {
                urlConnection.disconnect()
            }
        } catch (ignored: IOException) {
        }
        return null
    }

    private fun getEncoding(connection: HttpURLConnection): String {
        val contentEncoding = connection.contentEncoding
        if (contentEncoding != null) return contentEncoding
        val contentType = connection.contentType
        for (value in contentType.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            value.trim { it <= ' ' }
            if (value.lowercase().startsWith("charset=")) return value.substring(8)
        }
        return mEncoding
    }

    companion object {
        private val INTERVAL_DAY = TimeUnit.DAYS.toSeconds(1)

        // TODO: Allow changing encoding
        private const val encoding = "UTF-8"
        private val language: String
            get() = CommonUtils.language

        fun interface ResultCallback {
            fun addResult(suggestion: String?): Boolean
        }
    }
}
