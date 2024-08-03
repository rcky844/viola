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
import tipz.viola.webview.VWebView.PageLoadState

object BussUtils {
    const val LOG_TAG = "BussUtils"

    private val bussPrefix = "buss://"
    private val apiUrl = "https://api.buss.lol"
    private val githubPrefix = "http(s)?://github.com/".toRegex()

    fun sendAndRequestResponse(view: VWebView, url: String): Boolean {
        if (!url.startsWith(bussPrefix)) return false
        val split = url.replace(bussPrefix, "")
            .replace('.', '/')
        view.onPageInformationUpdated(PageLoadState.PAGE_STARTED, url, null)
        view.onPageLoadProgressChanged(20)

        CoroutineScope(Dispatchers.IO).launch {
            val data = MiniDownloadHelper.startDownload("${apiUrl}/domain/${split}")!!
            if (data.isEmpty()) return@launch
            val ip: String = JSONObject(String(data)).getString("ip")

            val realUrl = if (ip.contains(githubPrefix))
                "https://raw.githubusercontent.com/" +
                        "${ip.replace(githubPrefix, "")}/main/index.html"
            else ip
            CoroutineScope(Dispatchers.Main).launch { view.onPageLoadProgressChanged(40) }

            val htmlData = MiniDownloadHelper.startDownload(realUrl)
            CoroutineScope(Dispatchers.Main).launch { view.onPageLoadProgressChanged(60) }

            val parsedHtml = BussHtmlUtils.parseHtml(realUrl, htmlData)
            CoroutineScope(Dispatchers.Main).launch { view.onPageLoadProgressChanged(80) }
            Log.d(LOG_TAG, parsedHtml)

            CoroutineScope(Dispatchers.Main).launch {
                view.loadDataWithBaseURL(realUrl, parsedHtml,
                    "text/html", "UTF-8", url)
                view.onPageInformationUpdated(PageLoadState.PAGE_FINISHED, url, null)
            }
        }
        return true
    }
}