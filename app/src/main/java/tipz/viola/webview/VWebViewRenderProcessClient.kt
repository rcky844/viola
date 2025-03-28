// Copyright (c) 2023-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.webkit.WebView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewRenderProcess
import androidx.webkit.WebViewRenderProcessClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.R

class VWebViewRenderProcessClient(vWebView: VWebView) :
    WebViewRenderProcessClient() {
    private val webViewRenderProcess =
        if (WebViewFeature.isFeatureSupported(WebViewFeature.GET_WEB_VIEW_RENDERER))
            WebViewCompat.getWebViewRenderProcess(vWebView)
        else null

    private var dialog = MaterialAlertDialogBuilder(vWebView.context)
        .setTitle(R.string.dialog_page_unresponsive_title)
        .setMessage(R.string.dialog_page_unresponsive_message)
        .setPositiveButton(R.string.dialog_page_unresponsive_wait, null)
        .setNegativeButton(R.string.dialog_page_unresponsive_terminate) { _, _ -> webViewRenderProcess!!.terminate() }
        .create()

    override fun onRenderProcessUnresponsive(view: WebView, renderer: WebViewRenderProcess?) {
        dialog.show()
    }

    override fun onRenderProcessResponsive(view: WebView, renderer: WebViewRenderProcess?) {
        dialog.dismiss()
    }
}