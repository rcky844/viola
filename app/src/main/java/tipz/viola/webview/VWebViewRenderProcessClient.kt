// Copyright (c) 2023-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.content.Context
import android.content.DialogInterface
import android.webkit.WebView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewRenderProcess
import androidx.webkit.WebViewRenderProcessClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.R

class VWebViewRenderProcessClient(mContext: Context, mVWebView: VWebView) :
    WebViewRenderProcessClient() {
    private val mWebViewRenderProcess =
        if (WebViewFeature.isFeatureSupported(WebViewFeature.GET_WEB_VIEW_RENDERER)) WebViewCompat.getWebViewRenderProcess(
            mVWebView
        ) else null

    private var dialog = MaterialAlertDialogBuilder(mContext)
        .setTitle(R.string.dialog_page_unresponsive_title)
        .setMessage(R.string.dialog_page_unresponsive_message)
        .setPositiveButton(R.string.dialog_page_unresponsive_wait, null)
        .setNegativeButton(R.string.dialog_page_unresponsive_terminate) { _: DialogInterface?, _: Int -> mWebViewRenderProcess!!.terminate() }
        .create()

    override fun onRenderProcessUnresponsive(view: WebView, renderer: WebViewRenderProcess?) {
        dialog.show()
    }

    override fun onRenderProcessResponsive(view: WebView, renderer: WebViewRenderProcess?) {
        dialog.dismiss()
    }
}