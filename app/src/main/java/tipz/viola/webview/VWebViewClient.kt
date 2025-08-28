// Copyright (c) 2020-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.view.LayoutInflater
import android.webkit.HttpAuthHandler
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.webkit.WebViewClientCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import tipz.viola.R
import tipz.viola.databinding.DialogAuthBinding
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.UrlUtils
import tipz.viola.webview.VWebView.PageLoadState
import tipz.viola.webview.activity.BrowserActivity
import java.io.ByteArrayInputStream

open class VWebViewClient(
    private val activity: VWebViewActivity, private val vWebView: VWebView,
    private val adServersHandler: AdServersClient
) : WebViewClientCompat() {
    private val LOG_TAG = "VWebViewClient"

    private val settingsPreference = vWebView.settingsPreference

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        vWebView.onPageInformationUpdated(PageLoadState.PAGE_STARTED, url)
        if (activity is BrowserActivity) activity.checkHomePageVisibility()
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        vWebView.onPageInformationUpdated(PageLoadState.PAGE_FINISHED, url)
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        vWebView.onPageInformationUpdated(PageLoadState.UPDATE_HISTORY, url)
    }

    @Deprecated("Deprecated in Java")
    override fun onReceivedError(
        view: WebView, errorCode: Int, description: String, failingUrl: String
    ) {
        if (description.contains("ERR_SSL_PROTOCOL_ERROR")) {
            getSslDialog(ERROR_FAILED_SSL_HANDSHAKE)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    vWebView.loadRealUrl(
                        failingUrl.replaceFirst(UrlUtils.httpsPrefix, UrlUtils.httpPrefix))
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create().show()
        }
        vWebView.onPageInformationUpdated(PageLoadState.PAGE_ERROR, failingUrl, description = description)
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView, handler: HttpAuthHandler,
        host: String, realm: String
    ) {
        val binding: DialogAuthBinding = DialogAuthBinding.inflate(LayoutInflater.from(activity))
        val editView = binding.root

        val usernameEditText = binding.usernameEditText
        val passwordEditText = binding.passwordEditText
        binding.message.text = activity.getString(R.string.dialog_auth_message, vWebView.url)

        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.dialog_auth_title)
            .setView(editView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                handler.proceed(usernameEditText.text.toString(), passwordEditText.text.toString())
            }
            .setNegativeButton(android.R.string.cancel, { _, _ ->
                handler.cancel()
            })
            .create().show()
    }

    private fun getSslDialog(error: Int): MaterialAlertDialogBuilder {
        val dialog = MaterialAlertDialogBuilder(activity)
        @StringRes val stringResId = when (error) {
            SslError.SSL_DATE_INVALID -> R.string.ssl_certificate_date_invalid
            SslError.SSL_INVALID -> R.string.ssl_certificate_invalid
            SslError.SSL_EXPIRED -> R.string.ssl_certificate_expired
            SslError.SSL_IDMISMATCH -> R.string.ssl_certificate_idmismatch
            SslError.SSL_NOTYETVALID -> R.string.ssl_certificate_notyetvalid
            SslError.SSL_UNTRUSTED -> R.string.ssl_certificate_untrusted
            ERROR_FAILED_SSL_HANDSHAKE -> R.string.dialog_ssl_failed_handshake_title
            else -> R.string.ssl_certificate_unknown
        }

        dialog.setTitle(R.string.ssl_certificate_error_dialog_title)
            .setMessage(
                activity.resources.getString(
                    R.string.ssl_certificate_error_dialog_message,
                    activity.resources.getString(stringResId)
                )
            )
        return dialog
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        // Handle SSL errors
        val error = vWebView.unsecureURLs.firstOrNull { it.url.toUri().host == url.toUri().host }
        if (error != null) {
            MainScope().launch {
                getSslDialog(error.primaryError)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        run {
                            view.loadUrl(url)
                            vWebView.onSslErrorProceed()
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show()
            }
            return true
        }

        // Do not override loading if URL scheme is supported
        if (UrlUtils.isUriSupported(url)) return false

        // Handle open in app
        vWebView.loadAppLinkUrl(url)
        return true
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        var shouldOverride = shouldOverrideUrlLoading(view, url)
        if (shouldOverride) return true

        if (request.isForMainFrame && vWebView.requestHeaders.isNotEmpty()) {
            vWebView.loadUrl(url)
            shouldOverride = true
        }
        return shouldOverride
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        getSslDialog(error.primaryError)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                run {
                    handler.proceed()
                    vWebView.onSslErrorProceed()
                    vWebView.unsecureURLs.add(error)
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
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
            if (adServersHandler.adServers!!.contains(" ${url.toUri().host}"))
                return WebResourceResponse(
                    "text/plain", "utf-8",
                    ByteArrayInputStream("".toByteArray())
                )
        }

        return super.shouldInterceptRequest(view, url)
    }
}
