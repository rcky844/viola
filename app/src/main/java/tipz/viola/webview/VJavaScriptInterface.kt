// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.webkit.JavascriptInterface
import tipz.build.info.BuildInfo
import tipz.viola.download.providers.InternalDownloadProvider

class VJavaScriptInterface(private val activity: VWebViewActivity) {
    @JavascriptInterface
    fun getProductBuildTag(): String = BuildInfo().getProductBuildTag() ?: ""

    // Internal DownloadProvider
    @JavascriptInterface
    fun getBase64FromBlobData(downloadPath: String, uriString: String, mimeType: String) =
        InternalDownloadProvider.getBase64FromBlobData(activity, downloadPath, uriString, mimeType)

    companion object {
        val LOG_TAG = "VJavaScriptInterface"
        val INTERFACE_NAME = "ViolaBrowser"
    }
}