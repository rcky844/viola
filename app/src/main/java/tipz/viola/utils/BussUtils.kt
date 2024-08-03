// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import tipz.viola.download.MiniDownloadHelper
import tipz.viola.webview.VWebView

object BussUtils {
    const val LOG_TAG = "BussUtils"

    private val bussPrefix = "buss://"
    private val apiUrl = "https://api.buss.lol"
    private val githubPrefix = "http(s)?://github.com/".toRegex()

    fun sendAndRequestResponse(view: VWebView, url: String): Boolean {
        if (!url.startsWith(bussPrefix)) return false
        val split = url.replace(bussPrefix, "")
            .replace('.', '/')

        CoroutineScope(Dispatchers.IO).launch {
            val data = MiniDownloadHelper.startDownload("${apiUrl}/domain/${split}")!!
            if (data.isEmpty()) return@launch
            val ip: String = JSONObject(String(data)).getString("ip")

            val realUrl = if (ip.contains(githubPrefix))
                "https://raw.githubusercontent.com/" +
                        "${ip.replace(githubPrefix, "")}/main/index.html"
            else ip
            val htmlData = MiniDownloadHelper.startDownload(realUrl)

            val parsedHtml = BussHtmlUtils.parseHtml(realUrl, htmlData)
            Log.d(LOG_TAG, parsedHtml)
            CoroutineScope(Dispatchers.Main).launch {
                view.loadDataWithBaseURL(realUrl, parsedHtml,
                    "text/html", "UTF-8", url)
            }
        }
        return true
    }
}