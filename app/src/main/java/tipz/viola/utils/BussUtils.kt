// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.utils

import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
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

        val requestQueue = Volley.newRequestQueue(view.context)
        val stringRequest = StringRequest(
            Request.Method.GET, "${apiUrl}/domain/${split}",
            { response ->
                val string: String = JSONObject(response).getString("ip")
                view.loadUrl(
                    if (string.contains("github.com"))
                        "${previewGithub}${string}/main/index.html" else string
                )
            },
            { error ->
                Log.e(LOG_TAG, "Request failed, error=$error")
            })
        requestQueue.add(stringRequest)
        return true
    }
}