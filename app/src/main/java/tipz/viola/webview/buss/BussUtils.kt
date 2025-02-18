// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.buss

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import tipz.viola.download.MiniDownloadHelper
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.UrlUtils
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
            var apiUrl = view.settingsPreference.getString(SettingsKeys.bussApiUrl)
            if (apiUrl.trim().isEmpty()) apiUrl = this@BussUtils.apiUrl

            val requestUrl = "$apiUrl/domain/${split}"
            val data = MiniDownloadHelper.startDownload(requestUrl).response
            if (data.isEmpty()) {
                MainScope().launch {
                    // No data returned means it probably isn't registered
                    view.onPageInformationUpdated(
                        PageLoadState.PAGE_ERROR, url, null,
                        "net::ERR_NAME_NOT_RESOLVED")
                }
                return@launch
            }
            val ip: String = JSONObject(String(data)).getString("ip")

            val realUrl = if (ip.contains(githubPrefix))
                "https://raw.githubusercontent.com/" +
                        "${ip.replace(githubPrefix, "")}/main/index.html"
            else UrlUtils.validateUrlOrConvertToSearch(view.settingsPreference, ip, 1)
            MainScope().launch { view.onPageLoadProgressChanged(40) }

            val htmlData = MiniDownloadHelper.startDownload(realUrl).response
            if (htmlData.isEmpty()) {
                MainScope().launch {
                    // For those that can't download data, use a more generic one, since
                    // MiniDownloadHelper can't give us error codes yet
                    view.onPageInformationUpdated(
                        PageLoadState.PAGE_ERROR, url, null,
                        "net::ERR_CONNECTION_FAILED")
                }
                return@launch
            }
            MainScope().launch { view.onPageLoadProgressChanged(60) }

            val parsedHtml = BussHtmlUtils.parseHtml(realUrl, htmlData)
            MainScope().launch { view.onPageLoadProgressChanged(80) }
            Log.d(LOG_TAG, parsedHtml)

            MainScope().launch {
                view.loadDataWithBaseURL(realUrl, parsedHtml,
                    "text/html", "UTF-8", url)
                view.onPageInformationUpdated(PageLoadState.PAGE_FINISHED, url, null)
            }
        }
        return true
    }
}