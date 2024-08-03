// Copyright (c) 2020-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.StringRes
import androidx.webkit.WebViewClientCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.Application
import tipz.viola.R
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.UrlUtils
import tipz.viola.webview.VWebView.PageLoadState
import java.io.ByteArrayInputStream
import java.net.URL

open class VWebViewClient(
    private val mContext: Context,
    private val mVWebView: VWebView,
    private val adServersHandler: AdServersClient
) : WebViewClientCompat() {
    private val settingsPreference =
        (mContext.applicationContext as Application).settingsPreference
    private val unsecureURLSet = ArrayList<String>()
    private val unsecureURLErrorSet = ArrayList<SslError>()

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        mVWebView.onPageInformationUpdated(PageLoadState.PAGE_STARTED, url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        mVWebView.onPageInformationUpdated(PageLoadState.PAGE_FINISHED, url, null)
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        mVWebView.onPageInformationUpdated(PageLoadState.UPDATE_HISTORY, url, null)
    }

    @Deprecated("Deprecated in Java")
    override fun onReceivedError(
        view: WebView, errorCode: Int, description: String, failingUrl: String
    ) {
        mVWebView.onPageInformationUpdated(PageLoadState.PAGE_ERROR, failingUrl,
            null, description)
    }

    private fun getSslDialog(error: SslError): MaterialAlertDialogBuilder {
        val dialog = MaterialAlertDialogBuilder(mContext)
        @StringRes var stringResId = when (error.primaryError) {
            SslError.SSL_DATE_INVALID -> R.string.ssl_certificate_date_invalid
            SslError.SSL_INVALID -> R.string.ssl_certificate_invalid
            SslError.SSL_EXPIRED -> R.string.ssl_certificate_expired
            SslError.SSL_IDMISMATCH -> R.string.ssl_certificate_idmismatch
            SslError.SSL_NOTYETVALID -> R.string.ssl_certificate_notyetvalid
            SslError.SSL_UNTRUSTED -> R.string.ssl_certificate_untrusted
            else -> R.string.ssl_certificate_unknown
        }

        dialog.setTitle(R.string.ssl_certificate_error_dialog_title)
            .setMessage(
                mContext.resources.getString(
                    R.string.ssl_certificate_error_dialog_content,
                    mContext.resources.getString(stringResId)
                )
            )
        return dialog
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        if (unsecureURLSet.contains(url)) {
            getSslDialog(unsecureURLErrorSet[unsecureURLSet.indexOf(url)])
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                    run {
                        view.loadUrl(url)
                        mVWebView.onSslErrorProceed()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create().show()
            return true
        }
        if (UrlUtils.isUriLaunchable(url)) return false
        try {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse(UrlUtils.patchUrlForCVEMitigation(url)))
            mContext.startActivity(intent)
        } catch (ignored: ActivityNotFoundException) {
            CommonUtils.showMessage(mContext, R.string.toast_no_app_to_handle)
        }
        return true
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        unsecureURLErrorSet.add(error)
        getSslDialog(error)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                run {
                    handler.proceed()
                    mVWebView.onSslErrorProceed()
                    unsecureURLSet.add(error.url)
                }
            }
            .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
                handler.cancel()
            }
            .create().show()
    }

    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        return false
    }

    @Suppress("DEPRECATION") // Kept as it is easier to handle
    @Deprecated("Deprecated in Java")
    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        if (settingsPreference.getIntBool(SettingsKeys.enableAdBlock)) {
            if (adServersHandler.adServers.isNullOrEmpty()) {
                // TODO: Add dialog to warn users of this issue
                return super.shouldInterceptRequest(view, url)
            }
            if (adServersHandler.adServers!!.contains(" ${URL(url).host}"))
                return WebResourceResponse(
                    "text/plain", "utf-8",
                    ByteArrayInputStream(CommonUtils.EMPTY_STRING.toByteArray())
                )
        }

        return super.shouldInterceptRequest(view, url)
    }
}
