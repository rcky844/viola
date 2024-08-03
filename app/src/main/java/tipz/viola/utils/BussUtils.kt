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
    private val previewGithub = "https://html-preview.github.io/?url="

    fun splitDomain(str: String?): String {
        if (str.isNullOrEmpty()) return CommonUtils.EMPTY_STRING

        val split = str.replace("\\.".toRegex(), "#")
            .split("#".toRegex(), limit = 2).toTypedArray()
        if (split.size == 1) {
            return split[0]
        }
        val str2 = split[split.size - 2] + "/" + split[split.size - 1]
        Log.d(LOG_TAG, "splitDomain(): str2=$str2")
        return str2
    }

    fun sendAndRequestResponse(view: VWebView, url: String): Boolean {
        if (!url.startsWith(bussPrefix)) return false
        val split = splitDomain(url.replace(bussPrefix, CommonUtils.EMPTY_STRING))

        CoroutineScope(Dispatchers.IO).launch {
            val data = MiniDownloadHelper.startDownload("${apiUrl}/domain/${split}")!!
            val string: String = JSONObject(String(data)).getString("ip")
            CoroutineScope(Dispatchers.Main).launch {
                view.loadUrl(
                    if (string.contains("github.com"))
                        "${previewGithub}${string}/main/index.html" else string
                )
            }
        }
        return true
    }
}