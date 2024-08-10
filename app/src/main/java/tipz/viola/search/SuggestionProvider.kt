// Copyright (c) 2020-2023 The LineageOS Project
// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.search

import android.content.Context
import android.util.Log
import org.json.JSONArray
import tipz.viola.Application
import tipz.viola.ext.getCharset
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

open class SuggestionProvider(private val mContext: Context) {
    /**
     * Create a URL for the given query in the given language.
     *
     * @param query    the query that was made.
     * @return should return a URL that can be fetched using a GET.
     */
    private fun createQueryUrl(
        query: String
    ): String {
        val settingsPreference = (mContext.applicationContext as Application).settingsPreference
        return SearchEngineEntries.getPreferredSuggestionsUrl(settingsPreference, query)
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
            URLEncoder.encode(rawQuery, encoding)
        } catch (e: UnsupportedEncodingException) {
            Log.e(LOG_TAG, "Unable to encode the URL", e)
            return filter
        }

        // There could be no suggestions for this query, return an empty list.
        val content = downloadSuggestionsForQuery(query)
            ?.replaceFirst(")]}'", "")
            ?: return filter
        try {
            parseResults(content) {
                filter.add(it!!)
                filter.size < 5
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Unable to parse results", e)
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
        query: String
    ): String? {
        try {
            val url = URL(createQueryUrl(query))
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.addRequestProperty(
                "Cache-Control",
                "max-age=$INTERVAL_DAY, max-stale=$INTERVAL_DAY"
            )
            urlConnection.addRequestProperty("Accept-Charset", encoding)
            try {
                val charset = urlConnection.getCharset(encoding)
                urlConnection.inputStream.bufferedReader(charset).use {
                    return it.readText()
                }
            } catch (e: IOException) {
                Log.d(LOG_TAG, "Problem getting search suggestions", e)
            } finally {
                urlConnection.disconnect()
            }
        } catch (ignored: IOException) {
        }
        return null
    }

    companion object {
        private const val LOG_TAG = "SuggestionProvider"
        private val INTERVAL_DAY = TimeUnit.DAYS.toSeconds(1)

        private const val encoding = "UTF-8"

        fun interface ResultCallback {
            fun addResult(suggestion: String?): Boolean
        }
    }
}
